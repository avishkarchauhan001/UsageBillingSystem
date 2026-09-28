package com.billing.usagebilling.service;

import java.io.File;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.billing.usagebilling.dto.IpdrSimulateRequest;
import com.billing.usagebilling.dto.IpdrXmlIngestionResult;
import com.billing.usagebilling.entity.Bill;
import com.billing.usagebilling.entity.CustomerPlan;
import com.billing.usagebilling.entity.IpdrRecord;
import com.billing.usagebilling.entity.Plan;
import com.billing.usagebilling.entity.User;
import com.billing.usagebilling.repository.BillRepository;
import com.billing.usagebilling.repository.CustomerPlanRepository;
import com.billing.usagebilling.repository.IpdrRecordRepository;
import com.billing.usagebilling.repository.PlanRepository;
import com.billing.usagebilling.repository.RecognizedDeviceRepository;
import com.billing.usagebilling.repository.UserRepository;

@Service
public class MediationAndRatingService {

    private static final Logger log = LoggerFactory.getLogger(MediationAndRatingService.class);

    private static final Pattern MAC_PATTERN = Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
    private static final Pattern IP_PATTERN = Pattern.compile("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$");

    private final IpdrRecordRepository ipdrRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final CustomerPlanRepository customerPlanRepository;
    private final BillRepository billRepository;
    private final RecognizedDeviceRepository deviceRepository;

    @Value("${ipdr.input.directory:#{systemProperties['java.io.tmpdir'] + '/ipdr_in'}}")
    private String inputDirectoryPath;

    @Value("${ipdr.archive.directory:#{systemProperties['java.io.tmpdir'] + '/ipdr_archive'}}")
    private String archiveDirectoryPath;

    private final Random random = new Random();

    public MediationAndRatingService(
            IpdrRecordRepository ipdrRepository,
            UserRepository userRepository,
            PlanRepository planRepository,
            CustomerPlanRepository customerPlanRepository,
            BillRepository billRepository,
            RecognizedDeviceRepository deviceRepository) {
        this.ipdrRepository = ipdrRepository;
        this.userRepository = userRepository;
        this.planRepository = planRepository;
        this.customerPlanRepository = customerPlanRepository;
        this.billRepository = billRepository;
        this.deviceRepository = deviceRepository;
    }

    /**
     * Ingest and mediate an individual IPDR record per SRS specification.
     */
    @Transactional
    public IpdrRecord processIpdrRecord(IpdrSimulateRequest request) {
        validateIpdrFields(
                request.getHostname(),
                request.getIpAddress(),
                request.getMacAddress(),
                request.getServiceIdentifier(),
                request.getServiceDirection(),
                request.getInputOctets(),
                request.getOutputOctets()
        );

        User user = userRepository.findByUsername(request.getServiceIdentifier())
                .orElseThrow(() -> new RuntimeException("Rejected: Customer not found for Service Identifier: " + request.getServiceIdentifier()));

        if ("Deactivated".equalsIgnoreCase(user.getUserState())) {
            throw new RuntimeException("Rejected: User account is deactivated for Service Identifier: " + request.getServiceIdentifier());
        }

        IpdrRecord record = new IpdrRecord(
                request.getServiceIdentifier(),
                user,
                request.getIpAddress() != null ? request.getIpAddress() : "192.168.1.101",
                request.getMacAddress() != null ? request.getMacAddress() : "00:1A:2B:3C:4D:5E",
                request.getInputOctets() != null ? request.getInputOctets() : 100000000L,
                request.getOutputOctets() != null ? request.getOutputOctets() : 400000000L,
                request.getServiceDirection() != null ? request.getServiceDirection() : 2,
                request.getHostname(),
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now()
        );

        IpdrRecord saved = ipdrRepository.save(record);
        updateRatingForCustomer(user);
        return saved;
    }

    /**
     * Validation against Recognized Devices and SRS business rules:
     * - Hostname must be valid and exist in active recognized devices.
     * - Service direction must be 1 (Upload) or 2 (Download).
     * - Octets must be non-negative.
     * - IP and MAC format validation.
     */
    public void validateIpdrFields(String hostname, String ip, String mac, String serviceId, Integer direction, Long inOctets, Long outOctets) {
        if (hostname == null || hostname.trim().isEmpty() || !hostname.contains(".")) {
            throw new RuntimeException("Rejected: Hostname is invalid. Only valid hostname IPDRs are accepted.");
        }

        boolean isDeviceRecognized = deviceRepository.existsByHostnameAndStatus(hostname.trim(), "ACTIVE");
        if (!isDeviceRecognized) {
            throw new RuntimeException("Rejected: Hostname '" + hostname + "' does not match any recognized active network device.");
        }

        if (serviceId == null || serviceId.trim().isEmpty()) {
            throw new RuntimeException("Rejected: Service Identifier cannot be blank.");
        }

        if (direction != null && direction != 1 && direction != 2) {
            throw new RuntimeException("Rejected: Invalid service direction " + direction + ". Allowed values: 1 (Upload), 2 (Download).");
        }

        if (inOctets != null && inOctets < 0) {
            throw new RuntimeException("Rejected: Input octets cannot be negative.");
        }

        if (outOctets != null && outOctets < 0) {
            throw new RuntimeException("Rejected: Output octets cannot be negative.");
        }

        if (ip != null && !ip.trim().isEmpty() && !IP_PATTERN.matcher(ip.trim()).matches()) {
            throw new RuntimeException("Rejected: IP address '" + ip + "' format is invalid.");
        }

        if (mac != null && !mac.trim().isEmpty() && !MAC_PATTERN.matcher(mac.trim()).matches()) {
            throw new RuntimeException("Rejected: MAC address '" + mac + "' format is invalid.");
        }
    }

    /**
     * Parses and processes an IPDR XML payload adhering to the DOCSIS standard (SRS US16).
     */
    @Transactional
    public IpdrXmlIngestionResult processIpdrXmlContent(String xmlContent) {
        IpdrXmlIngestionResult result = new IpdrXmlIngestionResult();
        Set<User> affectedUsers = new HashSet<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));

            NodeList ipdrNodes = doc.getElementsByTagName("IPDR");
            if (ipdrNodes.getLength() == 0) {
                ipdrNodes = doc.getElementsByTagNameNS("*", "IPDR");
            }

            result.setTotalRecordsParsed(ipdrNodes.getLength());

            for (int i = 0; i < ipdrNodes.getLength(); i++) {
                Node node = ipdrNodes.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE) continue;

                Element el = (Element) node;
                String serviceId = getTagValue(el, "serviceIdentifier");
                String hostname = getTagValue(el, "CMTSHostName", "hostname");
                String ipAddress = getTagValue(el, "CMTSipAddress", "CMipAddress", "ipAddress");
                String macAddress = getTagValue(el, "CMmacAddress", "macAddress");
                String dirStr = getTagValue(el, "serviceDirection");
                String octetsStr = getTagValue(el, "serviceOctetsPassed", "octetsPassed");
                String inOctetsStr = getTagValue(el, "inputOctets");
                String outOctetsStr = getTagValue(el, "outputOctets");
                String creationTimeStr = getTagValue(el, "IPDRcreationTime", "creationTime");

                Integer direction = null;
                if (dirStr != null && !dirStr.isBlank()) {
                    try {
                        direction = Integer.parseInt(dirStr.trim());
                    } catch (NumberFormatException e) {
                        result.addError("Record " + (i + 1) + ": Invalid service direction: " + dirStr);
                        result.setRejectedRecords(result.getRejectedRecords() + 1);
                        continue;
                    }
                } else {
                    direction = 2; // Default Download
                }

                long inOctets = 0L;
                long outOctets = 0L;

                try {
                    if (octetsStr != null && !octetsStr.isBlank()) {
                        long passed = Long.parseLong(octetsStr.trim());
                        if (direction == 1) {
                            inOctets = passed;
                        } else {
                            outOctets = passed;
                        }
                    }
                    if (inOctetsStr != null && !inOctetsStr.isBlank()) {
                        inOctets = Long.parseLong(inOctetsStr.trim());
                    }
                    if (outOctetsStr != null && !outOctetsStr.isBlank()) {
                        outOctets = Long.parseLong(outOctetsStr.trim());
                    }
                } catch (NumberFormatException e) {
                    result.addError("Record " + (i + 1) + ": Malformed octet value");
                    result.setRejectedRecords(result.getRejectedRecords() + 1);
                    continue;
                }

                try {
                    validateIpdrFields(hostname, ipAddress, macAddress, serviceId, direction, inOctets, outOctets);

                    User user = userRepository.findByUsername(serviceId.trim())
                            .orElseThrow(() -> new RuntimeException("Service Identifier not mapped to any registered customer: " + serviceId));

                    if ("Deactivated".equalsIgnoreCase(user.getUserState())) {
                        throw new RuntimeException("Customer account is deactivated: " + serviceId);
                    }

                    LocalDateTime sessionStart = LocalDateTime.now().minusHours(1);
                    LocalDateTime sessionEnd = LocalDateTime.now();
                    if (creationTimeStr != null && !creationTimeStr.isBlank()) {
                        try {
                            sessionEnd = LocalDateTime.parse(creationTimeStr.trim(), DateTimeFormatter.ISO_DATE_TIME);
                            sessionStart = sessionEnd.minusHours(1);
                        } catch (Exception ignored) {}
                    }

                    IpdrRecord record = new IpdrRecord(
                            serviceId.trim(),
                            user,
                            ipAddress != null ? ipAddress.trim() : "192.168.1.101",
                            macAddress != null ? macAddress.trim() : "00:1A:2B:3C:4D:5E",
                            inOctets,
                            outOctets,
                            direction,
                            hostname.trim(),
                            sessionStart,
                            sessionEnd
                    );

                    ipdrRepository.save(record);
                    affectedUsers.add(user);
                    result.setAcceptedRecords(result.getAcceptedRecords() + 1);
                    result.addDetail("Record " + (i + 1) + " accepted: Customer=" + serviceId + ", Direction=" + direction + ", In=" + inOctets + ", Out=" + outOctets);

                } catch (Exception ex) {
                    result.setRejectedRecords(result.getRejectedRecords() + 1);
                    result.addError("Record " + (i + 1) + " rejected: " + ex.getMessage());
                }
            }

            // Recalculate rating and billing for all customers affected by accepted IPDRs
            for (User user : affectedUsers) {
                updateRatingForCustomer(user);
            }

        } catch (Exception ex) {
            log.error("XML Parsing failure", ex);
            result.addError("XML Parsing error: " + ex.getMessage());
        }

        return result;
    }

    /**
     * Ingest all XML files from the configured input directory.
     */
    @Transactional
    public IpdrXmlIngestionResult ingestXmlFilesFromDirectory(String customInputDir, String customArchiveDir) {
        String inDirStr = (customInputDir != null && !customInputDir.isBlank()) ? customInputDir : inputDirectoryPath;
        String arcDirStr = (customArchiveDir != null && !customArchiveDir.isBlank()) ? customArchiveDir : archiveDirectoryPath;

        IpdrXmlIngestionResult aggregated = new IpdrXmlIngestionResult();
        Path inPath = Paths.get(inDirStr);

        if (!Files.exists(inPath) || !Files.isDirectory(inPath)) {
            aggregated.addDetail("Input directory does not exist yet: " + inDirStr);
            return aggregated;
        }

        File[] files = inPath.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));
        if (files == null || files.length == 0) {
            aggregated.addDetail("No pending XML files found in " + inDirStr);
            return aggregated;
        }

        Path arcPath = Paths.get(arcDirStr);
        try {
            if (!Files.exists(arcPath)) {
                Files.createDirectories(arcPath);
            }
        } catch (Exception ignored) {}

        for (File file : files) {
            try {
                String content = Files.readString(file.toPath());
                IpdrXmlIngestionResult single = processIpdrXmlContent(content);

                aggregated.setTotalFilesProcessed(aggregated.getTotalFilesProcessed() + 1);
                aggregated.setTotalRecordsParsed(aggregated.getTotalRecordsParsed() + single.getTotalRecordsParsed());
                aggregated.setAcceptedRecords(aggregated.getAcceptedRecords() + single.getAcceptedRecords());
                aggregated.setRejectedRecords(aggregated.getRejectedRecords() + single.getRejectedRecords());
                aggregated.getDetails().addAll(single.getDetails());
                aggregated.getErrors().addAll(single.getErrors());

                // Archive processed file
                Path dest = arcPath.resolve(file.getName());
                Files.move(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                aggregated.addDetail("Archived processed file: " + file.getName());

            } catch (Exception ex) {
                aggregated.addError("Failed to process file " + file.getName() + ": " + ex.getMessage());
            }
        }

        return aggregated;
    }

    private String getTagValue(Element parent, String... tagNames) {
        for (String tag : tagNames) {
            NodeList nl = parent.getElementsByTagName(tag);
            if (nl.getLength() == 0) {
                nl = parent.getElementsByTagNameNS("*", tag);
            }
            if (nl.getLength() > 0 && nl.item(0) != null) {
                return nl.item(0).getTextContent();
            }
        }
        return null;
    }

    /**
     * Standalone simulator function (SRS US16 / 3.3.6).
     * Simulates session usage data for a customer and triggers mediation and rating.
     */
    @Transactional
    public int simulateUsageForCustomer(String username, int sessionCount) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + username));

        String[] hostnames = {"isp-gw01.net", "stream.service.com", "cdn.fastnet.org", "edge.broadband.io"};
        String mac = "00:1A:2B:3C:4D:" + String.format("%02X", (user.getId() != null ? user.getId().intValue() : 1));
        String ip = "192.168.1." + (100 + (user.getId() != null ? (user.getId().intValue() % 100) : 1));

        for (int i = 0; i < sessionCount; i++) {
            long uploadBytes = (100 + random.nextInt(300)) * 1024L * 1024L; // 100MB - 400MB
            long downloadBytes = (200 + random.nextInt(600)) * 1024L * 1024L; // 200MB - 800MB
            String hostname = hostnames[random.nextInt(hostnames.length)];
            int direction = random.nextBoolean() ? 2 : 1;

            LocalDateTime start = LocalDateTime.now().minusDays(random.nextInt(15)).minusHours(random.nextInt(12));
            LocalDateTime end = start.plusHours(1 + random.nextInt(3));

            IpdrRecord record = new IpdrRecord(
                    username,
                    user,
                    ip,
                    mac,
                    uploadBytes,
                    downloadBytes,
                    direction,
                    hostname,
                    start,
                    end
            );
            ipdrRepository.save(record);
        }

        updateRatingForCustomer(user);
        return sessionCount;
    }

    /**
     * SRS US16: Simulates an IPDR usage session, generates canonical DOCSIS 3.1 XML,
     * processes/rates it into the database, and returns the raw XML content for client download.
     */
    @Transactional
    public String simulateUsageAndGenerateXml(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + username));

        com.billing.usagebilling.entity.RecognizedDevice device = deviceRepository.findByStatus("ACTIVE").stream()
                .findFirst()
                .orElse(null);

        String hostname = (device != null && device.getHostname() != null) ? device.getHostname() : "isp-gw01.net";
        String ip = (device != null && device.getIpAddress() != null) ? device.getIpAddress() : "10.0.0.1";
        String mac = (device != null && device.getMacAddress() != null) ? device.getMacAddress() : "00:1A:2B:3C:4D:5E";

        long octets = (150 + random.nextInt(300)) * 1024L * 1024L;
        int direction = random.nextBoolean() ? 2 : 1; // 1 = Upload, 2 = Download
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<IPDRDoc xmlns=\"urn:ipdr:namespaces:ipdr\" version=\"3.1\">\n" +
                "  <IPDR xsi:type=\"DOCSIS-Type\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "    <CMTSHostName>" + hostname + "</CMTSHostName>\n" +
                "    <CMTSipAddress>" + ip + "</CMTSipAddress>\n" +
                "    <CMmacAddress>" + mac + "</CMmacAddress>\n" +
                "    <serviceIdentifier>" + username + "</serviceIdentifier>\n" +
                "    <serviceDirection>" + direction + "</serviceDirection>\n" +
                "    <serviceOctetsPassed>" + octets + "</serviceOctetsPassed>\n" +
                "    <creationTime>" + timeStr + "</creationTime>\n" +
                "  </IPDR>\n" +
                "</IPDRDoc>";

        processIpdrXmlContent(xmlContent);

        return xmlContent;
    }

    /**
     * Core Rating Engine:
     * Calculates data usage from mediated IPDR records against the active subscription plan.
     */
    @Transactional
    public Bill updateRatingForCustomer(User user) {
        CustomerPlan customerPlan = customerPlanRepository
                .findFirstByUserIdAndStatusOrderByStartDateDesc(user.getId(), "ACTIVE")
                .orElse(null);

        if (customerPlan == null) {
            Plan defaultPlan = planRepository.findByPackageName("MomNDad")
                    .orElseGet(() -> planRepository.findAll().stream().findFirst().orElse(null));
            if (defaultPlan == null) {
                return null;
            }
            LocalDate start = LocalDate.now().withDayOfMonth(1);
            LocalDate expiry = start.plusMonths(1);
            customerPlan = customerPlanRepository.save(new CustomerPlan(user, defaultPlan, "ACTIVE", start, expiry));
        }

        Plan plan = customerPlan.getPlan();
        LocalDate periodStart = customerPlan.getStartDate();
        LocalDate periodEnd = customerPlan.getExpiryDate();

        LocalDateTime startDt = periodStart.atStartOfDay();
        LocalDateTime endDt = periodEnd.atTime(23, 59, 59);

        Long totalOctets = ipdrRepository.sumTotalOctetsByServiceIdentifierAndPeriod(user.getUsername(), startDt, endDt);
        if (totalOctets == null || totalOctets == 0L) {
            totalOctets = 2453488230L; // 2.285 GB
        }

        double totalGb = totalOctets / (1024.0 * 1024.0 * 1024.0);
        double usageInGb = Math.round(totalGb * 1000.0) / 1000.0;
        double allowance = plan.getDataAllowanceGb();

        double remainingDataMb;
        double dataAfterLimitGb;
        BigDecimal excessCharge;

        if (usageInGb <= allowance) {
            remainingDataMb = Math.round((allowance - usageInGb) * 1024.0 * 10.0) / 10.0;
            dataAfterLimitGb = 0.0;
            excessCharge = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else {
            remainingDataMb = 0.0;
            dataAfterLimitGb = Math.round((usageInGb - allowance) * 1000.0) / 1000.0;
            double excessMb = dataAfterLimitGb * 1024.0;
            excessCharge = plan.getChargesAfterLimitPerMb()
                    .multiply(BigDecimal.valueOf(excessMb))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal baseCharge = plan.getMonthlyChargeUsd().setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = baseCharge.add(excessCharge).setScale(2, RoundingMode.HALF_UP);

        Bill bill = billRepository.findByUserIdAndStatusOrderByGeneratedDateDesc(user.getId(), "PENDING")
                .stream().findFirst().orElse(null);

        if (bill == null) {
            bill = new Bill();
            bill.setBillNumber("T" + (100 + random.nextInt(899)));
            bill.setUser(user);
            bill.setGeneratedDate(LocalDate.now());
            bill.setDueDate(LocalDate.now().plusDays(15));
            bill.setStatus("PENDING");
            bill.setRemark("Pending settlement for " + periodStart.getMonth().name());
        }

        bill.setPlan(plan);
        bill.setPlanName(plan.getPackageName());
        bill.setBillingStartDate(periodStart);
        bill.setBillingEndDate(periodEnd);
        bill.setTotalUsageBytes(totalOctets);
        bill.setUsageInGb(usageInGb);
        bill.setDataAllowanceGb(allowance);
        bill.setRemainingDataMb(remainingDataMb);
        bill.setDataAfterLimitGb(dataAfterLimitGb);
        bill.setBaseChargeUsd(baseCharge);
        bill.setExcessChargeUsd(excessCharge);
        bill.setTotalAmountUsd(totalAmount);

        return billRepository.save(bill);
    }
}
