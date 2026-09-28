package com.billing.usagebilling;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import com.billing.usagebilling.controller.OperatorController;
import com.billing.usagebilling.dto.IpdrXmlIngestionResult;
import com.billing.usagebilling.dto.PlanDto;
import com.billing.usagebilling.dto.PlanReportDto;
import com.billing.usagebilling.entity.Bill;
import com.billing.usagebilling.entity.Plan;
import com.billing.usagebilling.repository.BillRepository;
import com.billing.usagebilling.repository.PlanRepository;
import com.billing.usagebilling.service.MediationAndRatingService;
import com.billing.usagebilling.service.PlanService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OperatorAndSimulatorIntegrationTests {

    @Autowired
    private PlanService planService;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private OperatorController operatorController;

    @Autowired
    private MediationAndRatingService ratingService;

    @Autowired
    private BillRepository billRepository;

    @Test
    @Order(1)
    public void testGetActivePlansOnly() {
        List<PlanDto> activePlans = planService.getActivePlans();
        assertNotNull(activePlans);
        assertFalse(activePlans.isEmpty());
        for (PlanDto p : activePlans) {
            assertEquals("Activated", p.getPlanState(), "Only activated plans should be returned");
        }
    }

    @Test
    @Order(2)
    public void testPlanPagination() {
        Page<PlanDto> page = planService.getActivePlansPaginated(0, 10);
        assertNotNull(page);
        assertTrue(page.getContent().size() <= 10, "Page size must be at most 10 per SRS US11");
    }

    @Test
    @Order(3)
    public void testAddPlanValidationAndActivation() {
        // 1. Negative: Special characters in package name
        PlanDto invalidPkg = new PlanDto(null, "Super@Plan#1", 10.0, new BigDecimal("25.00"), new BigDecimal("0.0050"), null);
        RuntimeException ex1 = assertThrows(RuntimeException.class, () -> planService.createPlan(invalidPkg));
        assertTrue(ex1.getMessage().contains("alphanumeric"));

        // 2. Negative: Non-positive data allowance
        PlanDto invalidData = new PlanDto(null, "ZeroDataPlan", 0.0, new BigDecimal("25.00"), new BigDecimal("0.0050"), null);
        assertThrows(RuntimeException.class, () -> planService.createPlan(invalidData));

        // 3. Positive: Valid new plan
        String uniquePkg = "GamerSpeed" + System.currentTimeMillis();
        PlanDto validDto = new PlanDto(null, uniquePkg, 15.0, new BigDecimal("25.00"), new BigDecimal("0.0040"), null);
        PlanDto created = planService.createPlan(validDto);

        assertNotNull(created.getId());
        assertEquals(uniquePkg, created.getPackageName());
        assertEquals("Activated", created.getPlanState(), "New plan state must be automatically Activated");

        // Verify it appears in active plans
        List<PlanDto> active = planService.getActivePlans();
        assertTrue(active.stream().anyMatch(p -> p.getId().equals(created.getId())));
    }

    @Test
    @Order(4)
    public void testEditPlan() {
        String pkgName = "EditTestPlan" + System.currentTimeMillis();
        PlanDto created = planService.createPlan(new PlanDto(null, pkgName, 12.0, new BigDecimal("22.00"), new BigDecimal("0.0030"), null));

        // Edit details
        String updatedPkg = pkgName + "v2";
        PlanDto updatePayload = new PlanDto(created.getId(), updatedPkg, 16.0, new BigDecimal("26.00"), new BigDecimal("0.0025"), "Activated");
        PlanDto updated = planService.updatePlan(created.getId(), updatePayload);

        assertEquals(updatedPkg, updated.getPackageName());
        assertEquals(16.0, updated.getDataAllowanceGb());
        assertEquals(new BigDecimal("26.00"), updated.getMonthlyChargeUsd());
    }

    @Test
    @Order(5)
    public void testDeactivatePlanLogicalOnly() {
        String pkgName = "DeactTestPlan" + System.currentTimeMillis();
        PlanDto created = planService.createPlan(new PlanDto(null, pkgName, 8.0, new BigDecimal("18.00"), new BigDecimal("0.0060"), null));
        Long planId = created.getId();

        // Deactivate
        planService.deactivatePlan(planId);

        // Verify plan record STILL exists in database (NOT deleted)
        Plan inDb = planRepository.findById(planId).orElse(null);
        assertNotNull(inDb, "Plan record must not be physically deleted from database");
        assertEquals("Deactivated", inDb.getPlanState());

        // Verify it is NOT returned in active plans
        List<PlanDto> active = planService.getActivePlans();
        assertFalse(active.stream().anyMatch(p -> p.getId().equals(planId)), "Deactivated plan must not appear in active plans");
    }

    @Test
    @Order(6)
    public void testOperatorReportRealData() {
        List<PlanReportDto> report = planService.getPlanSalesReport();
        assertNotNull(report);
        assertFalse(report.isEmpty());

        for (PlanReportDto r : report) {
            assertNotNull(r.getPackageName());
            assertTrue(r.getSubscriberCount() >= 0);
            assertTrue(r.getTotalUsageGb() >= 0.0);
            assertNotNull(r.getTotalRevenueUsd());
        }
    }

    @Test
    @Order(7)
    public void testOperatorRoleAuthorization() {
        // Customer role trying to access Operator API -> 403 Forbidden
        assertThrows(SecurityException.class, () -> {
            operatorController.getActivePlans(true, "CUSTOMER");
        });

        // Operator role allowed
        ResponseEntity<List<PlanDto>> response = operatorController.getActivePlans(true, "OPERATOR");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(8)
    @Transactional
    public void testIpdrXmlIngestionValid() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <IPDRDoc xmlns="http://www.ipdr.org/namespaces/ipdr" version="3.1">
                <IPDR>
                    <CMTSHostName>isp-gw01.net</CMTSHostName>
                    <CMTSipAddress>192.168.1.1</CMTSipAddress>
                    <CMmacAddress>00:1A:2B:3C:4D:5E</CMmacAddress>
                    <CMipAddress>192.168.1.101</CMipAddress>
                    <serviceIdentifier>customer1</serviceIdentifier>
                    <serviceDirection>2</serviceDirection>
                    <serviceOctetsPassed>524288000</serviceOctetsPassed>
                    <IPDRcreationTime>2026-09-28T22:00:00</IPDRcreationTime>
                </IPDR>
            </IPDRDoc>
            """;

        IpdrXmlIngestionResult result = ratingService.processIpdrXmlContent(xml);
        assertNotNull(result);
        assertEquals(1, result.getAcceptedRecords(), "Valid record must be accepted");
        assertEquals(0, result.getRejectedRecords());

        // Verify customer1 pending bill is rated and updated
        List<Bill> pending = billRepository.findByUserIdAndStatusOrderByGeneratedDateDesc(3L, "PENDING");
        assertFalse(pending.isEmpty());
        Bill bill = pending.get(0);
        assertTrue(bill.getTotalUsageBytes() > 0);
        assertTrue(bill.getUsageInGb() > 0);
    }

    @Test
    @Order(9)
    public void testIpdrXmlIngestionNegativeRejections() {
        // 1. Unknown customer
        String xmlUnknownCustomer = """
            <IPDRDoc version="3.1">
                <IPDR>
                    <CMTSHostName>isp-gw01.net</CMTSHostName>
                    <serviceIdentifier>non_existent_user_9999</serviceIdentifier>
                    <serviceDirection>2</serviceDirection>
                    <serviceOctetsPassed>1000000</serviceOctetsPassed>
                </IPDR>
            </IPDRDoc>
            """;
        IpdrXmlIngestionResult res1 = ratingService.processIpdrXmlContent(xmlUnknownCustomer);
        assertEquals(1, res1.getRejectedRecords());

        // 2. Unrecognized device hostname
        String xmlUnknownHost = """
            <IPDRDoc version="3.1">
                <IPDR>
                    <CMTSHostName>rogue-unauthorized-server.com</CMTSHostName>
                    <serviceIdentifier>customer1</serviceIdentifier>
                    <serviceDirection>2</serviceDirection>
                    <serviceOctetsPassed>1000000</serviceOctetsPassed>
                </IPDR>
            </IPDRDoc>
            """;
        IpdrXmlIngestionResult res2 = ratingService.processIpdrXmlContent(xmlUnknownHost);
        assertEquals(1, res2.getRejectedRecords());

        // 3. Invalid service direction (e.g. 5)
        String xmlBadDir = """
            <IPDRDoc version="3.1">
                <IPDR>
                    <CMTSHostName>isp-gw01.net</CMTSHostName>
                    <serviceIdentifier>customer1</serviceIdentifier>
                    <serviceDirection>5</serviceDirection>
                    <serviceOctetsPassed>1000000</serviceOctetsPassed>
                </IPDR>
            </IPDRDoc>
            """;
        IpdrXmlIngestionResult res3 = ratingService.processIpdrXmlContent(xmlBadDir);
        assertEquals(1, res3.getRejectedRecords());

        // 4. Negative octets
        String xmlNegOctets = """
            <IPDRDoc version="3.1">
                <IPDR>
                    <CMTSHostName>isp-gw01.net</CMTSHostName>
                    <serviceIdentifier>customer1</serviceIdentifier>
                    <serviceDirection>1</serviceDirection>
                    <serviceOctetsPassed>-5000</serviceOctetsPassed>
                </IPDR>
            </IPDRDoc>
            """;
        IpdrXmlIngestionResult res4 = ratingService.processIpdrXmlContent(xmlNegOctets);
        assertEquals(1, res4.getRejectedRecords());
    }

    @Test
    @Order(10)
    public void testDirectoryIngestionAndArchiving() throws Exception {
        Path testDir = Paths.get("target/test-ipdr-in");
        Path archiveDir = Paths.get("target/test-ipdr-arch");
        Files.createDirectories(testDir);
        Files.createDirectories(archiveDir);

        String sampleXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <IPDRDoc version="3.1">
                <IPDR>
                    <CMTSHostName>isp-gw01.net</CMTSHostName>
                    <CMTSipAddress>192.168.1.1</CMTSipAddress>
                    <CMmacAddress>00:1A:2B:3C:4D:5E</CMmacAddress>
                    <serviceIdentifier>customer1</serviceIdentifier>
                    <serviceDirection>2</serviceDirection>
                    <serviceOctetsPassed>104857600</serviceOctetsPassed>
                </IPDR>
            </IPDRDoc>
            """;

        File testFile = testDir.resolve("test_session.xml").toFile();
        Files.writeString(testFile.toPath(), sampleXml);

        IpdrXmlIngestionResult dirResult = ratingService.ingestXmlFilesFromDirectory(testDir.toString(), archiveDir.toString());
        assertEquals(1, dirResult.getTotalFilesProcessed());
        assertEquals(1, dirResult.getAcceptedRecords());

        // Verify file was archived
        assertFalse(testFile.exists(), "Original file should have been moved");
        File archivedFile = archiveDir.resolve("test_session.xml").toFile();
        assertTrue(archivedFile.exists(), "File should now exist in archive directory");
    }
}
