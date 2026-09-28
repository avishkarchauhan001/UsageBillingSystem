package com.billing.usagebilling.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.billing.usagebilling.dto.BillDto;
import com.billing.usagebilling.dto.CustomerHomeDto;
import com.billing.usagebilling.dto.PaymentResponse;
import com.billing.usagebilling.dto.PlanChangeResponse;
import com.billing.usagebilling.dto.UsageReportItemDto;
import com.billing.usagebilling.dto.UsageReportResponse;
import com.billing.usagebilling.entity.Bill;
import com.billing.usagebilling.entity.CustomerPlan;
import com.billing.usagebilling.entity.IpdrRecord;
import com.billing.usagebilling.entity.Payment;
import com.billing.usagebilling.entity.Plan;
import com.billing.usagebilling.entity.User;
import com.billing.usagebilling.repository.BillRepository;
import com.billing.usagebilling.repository.CustomerPlanRepository;
import com.billing.usagebilling.repository.IpdrRecordRepository;
import com.billing.usagebilling.repository.PaymentRepository;
import com.billing.usagebilling.repository.PlanRepository;
import com.billing.usagebilling.repository.UserRepository;

@Service
public class CustomerService {

    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final CustomerPlanRepository customerPlanRepository;
    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final IpdrRecordRepository ipdrRepository;
    private final MediationAndRatingService ratingService;

    public CustomerService(
            UserRepository userRepository,
            PlanRepository planRepository,
            CustomerPlanRepository customerPlanRepository,
            BillRepository billRepository,
            PaymentRepository paymentRepository,
            IpdrRecordRepository ipdrRepository,
            MediationAndRatingService ratingService) {
        this.userRepository = userRepository;
        this.planRepository = planRepository;
        this.customerPlanRepository = customerPlanRepository;
        this.billRepository = billRepository;
        this.paymentRepository = paymentRepository;
        this.ipdrRepository = ipdrRepository;
        this.ratingService = ratingService;
    }

    private User getCustomerUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        if ("Deactivated".equalsIgnoreCase(user.getUserState())) {
            throw new RuntimeException("User is deactivated");
        }
        return user;
    }

    /**
     * Check and activate any scheduled plan changes whose activation date has arrived.
     */
    @Transactional
    public void processScheduledPlanActivations(User user) {
        LocalDate today = LocalDate.now();
        CustomerPlan activePlan = customerPlanRepository
                .findFirstByUserIdAndStatusOrderByStartDateDesc(user.getId(), "ACTIVE")
                .orElse(null);

        if (activePlan != null && today.isAfter(activePlan.getExpiryDate())) {
            List<CustomerPlan> scheduledPlans = customerPlanRepository
                    .findByUserIdAndStatus(user.getId(), "SCHEDULED");
            for (CustomerPlan scheduled : scheduledPlans) {
                if (!today.isBefore(scheduled.getStartDate())) {
                    activePlan.setStatus("EXPIRED");
                    customerPlanRepository.save(activePlan);

                    scheduled.setStatus("ACTIVE");
                    customerPlanRepository.save(scheduled);
                    activePlan = scheduled;
                    break;
                }
            }
        }
    }

    @Transactional
    public CustomerHomeDto getCustomerHome(String username) {
        User user = getCustomerUser(username);
        processScheduledPlanActivations(user);

        // Ensure active plan exists
        CustomerPlan activePlan = customerPlanRepository
                .findFirstByUserIdAndStatusOrderByStartDateDesc(user.getId(), "ACTIVE")
                .orElse(null);

        if (activePlan == null) {
            Plan defaultPlan = planRepository.findByPackageName("MomNDad")
                    .orElseGet(() -> planRepository.findAll().stream().findFirst()
                            .orElseThrow(() -> new RuntimeException("No plans available in the system")));
            LocalDate start = LocalDate.now().withDayOfMonth(1);
            LocalDate expiry = start.plusMonths(1);
            activePlan = customerPlanRepository.save(new CustomerPlan(user, defaultPlan, "ACTIVE", start, expiry));
        }

        // Check for any scheduled plan
        CustomerPlan scheduledPlan = customerPlanRepository
                .findByUserIdAndStatus(user.getId(), "SCHEDULED")
                .stream().findFirst().orElse(null);

        // Ensure current rating and bill are up-to-date
        Bill currentBill = ratingService.updateRatingForCustomer(user);

        CustomerHomeDto dto = new CustomerHomeDto();
        dto.setUsername(user.getUsername());
        dto.setPlanId(activePlan.getPlan().getId());
        dto.setPackageName(activePlan.getPlan().getPackageName());
        dto.setBillingStartDate(activePlan.getStartDate());
        dto.setBillingEndDate(activePlan.getExpiryDate());

        if (currentBill != null) {
            dto.setUsageInGb(currentBill.getUsageInGb());
            dto.setRemainingDataMb(currentBill.getRemainingDataMb());
            dto.setDataAllowanceGb(currentBill.getDataAllowanceGb());
            dto.setDataAfterLimitGb(currentBill.getDataAfterLimitGb());
            dto.setMonthlyChargeUsd(currentBill.getBaseChargeUsd());
            dto.setExcessChargeUsd(currentBill.getExcessChargeUsd());
            dto.setTotalAmountUsd(currentBill.getTotalAmountUsd());
            dto.setCurrentBillId(currentBill.getId());
            dto.setCurrentBillNumber(currentBill.getBillNumber());
        } else {
            dto.setUsageInGb(0.0);
            dto.setRemainingDataMb(activePlan.getPlan().getDataAllowanceGb() * 1024.0);
            dto.setDataAllowanceGb(activePlan.getPlan().getDataAllowanceGb());
            dto.setDataAfterLimitGb(0.0);
            dto.setMonthlyChargeUsd(activePlan.getPlan().getMonthlyChargeUsd());
            dto.setExcessChargeUsd(BigDecimal.ZERO);
            dto.setTotalAmountUsd(activePlan.getPlan().getMonthlyChargeUsd());
        }

        long pendingCount = billRepository.countByUserIdAndStatus(user.getId(), "PENDING");
        dto.setPendingBillsCount(pendingCount);

        if (scheduledPlan != null) {
            dto.setHasScheduledPlan(true);
            dto.setScheduledPackageName(scheduledPlan.getPlan().getPackageName());
            dto.setScheduledActivationDate(scheduledPlan.getStartDate());
        } else {
            dto.setHasScheduledPlan(false);
        }

        return dto;
    }

    public List<BillDto> getCustomerBills(String username) {
        User user = getCustomerUser(username);
        return billRepository.findByUserIdOrderByGeneratedDateDesc(user.getId())
                .stream()
                .map(this::toBillDto)
                .collect(Collectors.toList());
    }

    public List<BillDto> searchCustomerBills(String username, String query) {
        User user = getCustomerUser(username);
        if (query == null || query.trim().isEmpty()) {
            return getCustomerBills(username);
        }
        return billRepository.findByUserIdAndBillNumberContainingIgnoreCase(user.getId(), query.trim())
                .stream()
                .map(this::toBillDto)
                .collect(Collectors.toList());
    }

    public BillDto getCustomerBillById(String username, Long billId) {
        User user = getCustomerUser(username);
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill with ID " + billId + " not found"));

        if (!bill.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: You cannot view another customer's bill");
        }
        return toBillDto(bill);
    }

    public List<BillDto> getPendingBills(String username) {
        User user = getCustomerUser(username);
        return billRepository.findByUserIdAndStatusOrderByGeneratedDateDesc(user.getId(), "PENDING")
                .stream()
                .map(this::toBillDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentResponse payBill(String username, Long billId, String paymentMode) {
        User user = getCustomerUser(username);
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill with ID " + billId + " not found"));

        // Enforce customer isolation
        if (!bill.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: You cannot pay another customer's bill");
        }

        // Validate not already paid
        if ("PAID".equalsIgnoreCase(bill.getStatus())) {
            throw new RuntimeException("Bill " + bill.getBillNumber() + " is already paid");
        }

        // Validate payment mode
        if (paymentMode == null || paymentMode.trim().isEmpty()) {
            paymentMode = "Online";
        }

        String txnRef = "TXN-" + LocalDate.now().toString().replace("-", "") + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = new Payment(
                bill,
                user,
                bill.getTotalAmountUsd(),
                paymentMode,
                txnRef,
                "SUCCESS"
        );
        Payment savedPayment = paymentRepository.save(payment);

        // Update bill state
        bill.setStatus("PAID");
        bill.setPaymentMode(paymentMode);
        bill.setPaidDate(LocalDateTime.now());
        bill.setRemark("Paid via " + paymentMode + " (Txn: " + txnRef + ")");
        billRepository.save(bill);

        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(savedPayment.getId());
        response.setBillId(bill.getId());
        response.setBillNumber(bill.getBillNumber());
        response.setAmountPaid(bill.getTotalAmountUsd());
        response.setPaymentMode(paymentMode);
        response.setTransactionReference(txnRef);
        response.setPaymentStatus("SUCCESS");
        response.setPaymentDate(savedPayment.getPaymentDate());
        response.setMessage("Payment of $" + bill.getTotalAmountUsd() + " completed successfully via " + paymentMode + "!");

        return response;
    }

    /**
     * SRS US19: Change Plan
     * "When a customer changes to a new plan, the new plan should be activated only after the current plan expires."
     */
    @Transactional
    public PlanChangeResponse changePlan(String username, Long newPlanId) {
        User user = getCustomerUser(username);
        Plan newPlan = planRepository.findById(newPlanId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        if (!"Activated".equalsIgnoreCase(newPlan.getPlanState())) {
            throw new RuntimeException("Selected plan is not currently available for subscription");
        }

        CustomerPlan currentActivePlan = customerPlanRepository
                .findFirstByUserIdAndStatusOrderByStartDateDesc(user.getId(), "ACTIVE")
                .orElse(null);

        if (currentActivePlan == null) {
            // No current active plan: activate immediately
            LocalDate start = LocalDate.now();
            LocalDate expiry = start.plusMonths(1);
            CustomerPlan cp = new CustomerPlan(user, newPlan, "ACTIVE", start, expiry);
            customerPlanRepository.save(cp);
            return new PlanChangeResponse(
                    "None",
                    expiry,
                    newPlan.getPackageName(),
                    start,
                    "ACTIVE",
                    "Plan " + newPlan.getPackageName() + " activated immediately."
            );
        }

        if (currentActivePlan.getPlan().getId().equals(newPlan.getId())) {
            throw new RuntimeException("You are already subscribed to the " + newPlan.getPackageName() + " plan");
        }

        // New plan activates when current plan expires
        LocalDate scheduledStart = currentActivePlan.getExpiryDate();
        LocalDate scheduledExpiry = scheduledStart.plusMonths(1);

        // Check if there is already a scheduled plan change, and update it
        CustomerPlan existingScheduled = customerPlanRepository
                .findByUserIdAndStatus(user.getId(), "SCHEDULED")
                .stream().findFirst().orElse(null);

        if (existingScheduled != null) {
            existingScheduled.setPlan(newPlan);
            existingScheduled.setStartDate(scheduledStart);
            existingScheduled.setExpiryDate(scheduledExpiry);
            customerPlanRepository.save(existingScheduled);
        } else {
            CustomerPlan scheduledPlan = new CustomerPlan(user, newPlan, "SCHEDULED", scheduledStart, scheduledExpiry);
            customerPlanRepository.save(scheduledPlan);
        }

        return new PlanChangeResponse(
                currentActivePlan.getPlan().getPackageName(),
                currentActivePlan.getExpiryDate(),
                newPlan.getPackageName(),
                scheduledStart,
                "SCHEDULED",
                "Plan change successfully scheduled! Your current plan (" 
                        + currentActivePlan.getPlan().getPackageName() 
                        + ") will remain active until " + currentActivePlan.getExpiryDate() 
                        + ". Your new plan (" + newPlan.getPackageName() 
                        + ") will automatically activate on " + scheduledStart + "."
        );
    }

    /**
     * SRS US20: User Report
     * Visual graphical representation of usage between From Date and To Date.
     */
    public UsageReportResponse getUserReport(String username, LocalDate fromDate, LocalDate toDate) {
        User user = getCustomerUser(username);

        if (toDate == null) {
            toDate = LocalDate.now();
        }
        if (fromDate == null) {
            fromDate = toDate.minusDays(14);
        }

        if (fromDate.isAfter(toDate)) {
            throw new RuntimeException("From Date cannot be after To Date");
        }

        LocalDateTime startDt = fromDate.atStartOfDay();
        LocalDateTime endDt = toDate.atTime(23, 59, 59);

        List<IpdrRecord> userRecords = ipdrRepository
                .findByUserIdAndSessionStartBetweenOrderBySessionStartAsc(user.getId(), startDt, endDt);
        List<IpdrRecord> sidRecords = ipdrRepository
                .findByServiceIdentifierOrderBySessionStartDesc(user.getUsername());

        Set<Long> seenIds = new HashSet<>();
        List<IpdrRecord> records = new ArrayList<>();
        for (IpdrRecord r : userRecords) {
            if (r.getId() != null) seenIds.add(r.getId());
            records.add(r);
        }
        for (IpdrRecord r : sidRecords) {
            if (r.getId() != null && !seenIds.contains(r.getId())) {
                if (r.getSessionStart() != null && !r.getSessionStart().isBefore(startDt) && !r.getSessionStart().isAfter(endDt)) {
                    seenIds.add(r.getId());
                    records.add(r);
                }
            }
        }

        // Group by Date
        Map<LocalDate, long[]> dailyOctets = new HashMap<>(); // [0]=upload, [1]=download

        // Initialize all dates in range with 0 so chart shows full range
        LocalDate curr = fromDate;
        while (!curr.isAfter(toDate)) {
            dailyOctets.put(curr, new long[]{0L, 0L});
            curr = curr.plusDays(1);
        }

        for (IpdrRecord r : records) {
            LocalDate rDate = r.getSessionStart().toLocalDate();
            long[] bytes = dailyOctets.computeIfAbsent(rDate, k -> new long[]{0L, 0L});
            bytes[0] += (r.getInputOctets() != null ? r.getInputOctets() : 0L);
            bytes[1] += (r.getOutputOctets() != null ? r.getOutputOctets() : 0L);
        }

        List<UsageReportItemDto> items = new ArrayList<>();
        double totalAllUploadGb = 0.0;
        double totalAllDownloadGb = 0.0;

        LocalDate iter = fromDate;
        while (!iter.isAfter(toDate)) {
            long[] bytes = dailyOctets.get(iter);
            double upMb = Math.round((bytes[0] / (1024.0 * 1024.0)) * 10.0) / 10.0;
            double downMb = Math.round((bytes[1] / (1024.0 * 1024.0)) * 10.0) / 10.0;
            double totalMb = Math.round((upMb + downMb) * 10.0) / 10.0;
            double totalGb = Math.round((totalMb / 1024.0) * 1000.0) / 1000.0;

            totalAllUploadGb += (bytes[0] / (1024.0 * 1024.0 * 1024.0));
            totalAllDownloadGb += (bytes[1] / (1024.0 * 1024.0 * 1024.0));

            items.add(new UsageReportItemDto(iter, upMb, downMb, totalMb, totalGb));
            iter = iter.plusDays(1);
        }

        UsageReportResponse response = new UsageReportResponse();
        response.setUsername(username);
        response.setFromDate(fromDate);
        response.setToDate(toDate);
        response.setTotalUploadGb(Math.round(totalAllUploadGb * 1000.0) / 1000.0);
        response.setTotalDownloadGb(Math.round(totalAllDownloadGb * 1000.0) / 1000.0);
        response.setTotalUsageGb(Math.round((totalAllUploadGb + totalAllDownloadGb) * 1000.0) / 1000.0);
        response.setDailyUsage(items);

        return response;
    }

    private BillDto toBillDto(Bill bill) {
        BillDto dto = new BillDto();
        dto.setId(bill.getId());
        dto.setBillNumber(bill.getBillNumber());
        dto.setPlanId(bill.getPlan() != null ? bill.getPlan().getId() : null);
        dto.setPlanName(bill.getPlanName());
        dto.setBillingStartDate(bill.getBillingStartDate());
        dto.setBillingEndDate(bill.getBillingEndDate());
        dto.setTotalUsageBytes(bill.getTotalUsageBytes());
        dto.setUsageInGb(bill.getUsageInGb());
        dto.setDataAllowanceGb(bill.getDataAllowanceGb());
        dto.setRemainingDataMb(bill.getRemainingDataMb());
        dto.setDataAfterLimitGb(bill.getDataAfterLimitGb());
        dto.setBaseChargeUsd(bill.getBaseChargeUsd());
        dto.setExcessChargeUsd(bill.getExcessChargeUsd());
        dto.setTotalAmountUsd(bill.getTotalAmountUsd());
        dto.setStatus(bill.getStatus());
        dto.setPaymentMode(bill.getPaymentMode());
        dto.setRemark(bill.getRemark());
        dto.setGeneratedDate(bill.getGeneratedDate());
        dto.setDueDate(bill.getDueDate());
        dto.setPaidDate(bill.getPaidDate());
        return dto;
    }
}
