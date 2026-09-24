package com.billing.usagebilling;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.billing.usagebilling.dto.BillDto;
import com.billing.usagebilling.dto.CustomerHomeDto;
import com.billing.usagebilling.dto.LoginRequest;
import com.billing.usagebilling.dto.LoginResponse;
import com.billing.usagebilling.dto.PaymentResponse;
import com.billing.usagebilling.dto.PlanChangeResponse;
import com.billing.usagebilling.dto.PlanDto;
import com.billing.usagebilling.dto.UsageReportResponse;
import com.billing.usagebilling.entity.User;
import com.billing.usagebilling.repository.UserRepository;
import com.billing.usagebilling.service.CustomerService;
import com.billing.usagebilling.service.MediationAndRatingService;
import com.billing.usagebilling.service.PlanService;
import com.billing.usagebilling.service.UserService;

@SpringBootTest
public class CustomerModuleIntegrationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PlanService planService;

    @Autowired
    private MediationAndRatingService ratingService;

    @Autowired
    private UserRepository userRepository;

    // 1. AUTHENTICATION & DEACTIVATION TESTS
    @Test
    public void testAuthenticationSuccess() {
        LoginResponse response = userService.authenticate(new LoginRequest("customer1", "Customer@123"));
        assertNotNull(response);
        assertEquals("customer1", response.getUsername());
        assertEquals("CUSTOMER", response.getRole());
    }

    @Test
    public void testAuthenticationInvalidCredentials() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            userService.authenticate(new LoginRequest("customer1", "WrongPassword!"));
        });
        assertEquals("Wrong username or password", ex.getMessage());
    }

    @Test
    @Transactional
    public void testDeactivatedUserCannotLogin() {
        // Find customer2 and deactivate
        User customer2 = userRepository.findByUsername("customer2").orElseThrow();
        customer2.setUserState("Deactivated");
        userRepository.save(customer2);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            userService.authenticate(new LoginRequest("customer2", "Customer@123"));
        });
        assertEquals("User is deactivated", ex.getMessage());
    }

    // 2. CUSTOMER HOME & USAGE TESTS
    @Test
    public void testCustomerHomeDetails() {
        CustomerHomeDto home = customerService.getCustomerHome("customer1");
        assertNotNull(home);
        assertEquals("customer1", home.getUsername());
        assertNotNull(home.getPackageName());
        assertTrue(home.getUsageInGb() >= 0.0);
        assertTrue(home.getTotalAmountUsd().doubleValue() > 0.0);
        assertNotNull(home.getBillingStartDate());
        assertNotNull(home.getBillingEndDate());
    }

    // 3. BILLS & ISOLATION TESTS
    @Test
    public void testCustomerBillsRetrievalAndSearch() {
        List<BillDto> bills = customerService.getCustomerBills("customer1");
        assertNotNull(bills);
        assertFalse(bills.isEmpty());

        // Test search
        List<BillDto> searchResult = customerService.searchCustomerBills("customer1", "T");
        assertFalse(searchResult.isEmpty());
    }

    @Test
    public void testCustomerDataIsolationCrossCustomerAccessRejected() {
        List<BillDto> customer1Bills = customerService.getCustomerBills("customer1");
        assertFalse(customer1Bills.isEmpty());
        Long billId = customer1Bills.get(0).getId();

        // Customer2 attempts to access Customer1's bill
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            customerService.getCustomerBillById("customer2", billId);
        });
        assertTrue(ex.getMessage().contains("Access denied"));
    }

    // 4. PENDING BILLS & PAYMENT TESTS
    @Test
    @Transactional
    public void testPaymentAndPendingBillsWorkflow() {
        // Ensure customer has a pending bill
        User user = userRepository.findByUsername("customer1").orElseThrow();
        ratingService.updateRatingForCustomer(user);

        List<BillDto> pendingBefore = customerService.getPendingBills("customer1");
        if (!pendingBefore.isEmpty()) {
            BillDto bill = pendingBefore.get(0);

            // Customer2 cannot pay Customer1's bill
            assertThrows(RuntimeException.class, () -> {
                customerService.payBill("customer2", bill.getId(), "UPI");
            });

            // Customer1 pays their bill
            PaymentResponse paymentResponse = customerService.payBill("customer1", bill.getId(), "Net banking");
            assertNotNull(paymentResponse);
            assertEquals("SUCCESS", paymentResponse.getPaymentStatus());
            assertEquals("Net banking", paymentResponse.getPaymentMode());

            // Re-paying already paid bill fails
            assertThrows(RuntimeException.class, () -> {
                customerService.payBill("customer1", bill.getId(), "UPI");
            });
        }
    }

    // 5. CHANGE PLAN (SRS US19: Delayed Activation After Current Plan Expiry)
    @Test
    @Transactional
    public void testChangePlanActivatesAfterExpiry() {
        List<PlanDto> activePlans = planService.getActivePlans();
        assertTrue(activePlans.size() >= 2);

        CustomerHomeDto homeBefore = customerService.getCustomerHome("customer1");
        PlanDto newPlan = activePlans.stream()
                .filter(p -> !p.getPackageName().equalsIgnoreCase(homeBefore.getPackageName()))
                .findFirst().orElseThrow();

        PlanChangeResponse changeResponse = customerService.changePlan("customer1", newPlan.getId());
        assertNotNull(changeResponse);
        assertEquals("SCHEDULED", changeResponse.getStatus());
        assertEquals(homeBefore.getBillingEndDate(), changeResponse.getScheduledActivationDate());

        // Verify current plan is still active on home
        CustomerHomeDto homeAfter = customerService.getCustomerHome("customer1");
        assertEquals(homeBefore.getPackageName(), homeAfter.getPackageName());
        assertTrue(homeAfter.isHasScheduledPlan());
        assertEquals(newPlan.getPackageName(), homeAfter.getScheduledPackageName());
        assertEquals(homeBefore.getBillingEndDate(), homeAfter.getScheduledActivationDate());
    }

    // 6. USER REPORT TESTS
    @Test
    public void testUserReport() {
        LocalDate from = LocalDate.now().minusDays(10);
        LocalDate to = LocalDate.now();

        UsageReportResponse report = customerService.getUserReport("customer1", from, to);
        assertNotNull(report);
        assertEquals("customer1", report.getUsername());
        assertNotNull(report.getDailyUsage());
        assertEquals(11, report.getDailyUsage().size()); // 10 days + today
    }

    @Test
    public void testUserReportInvalidDatesRejected() {
        LocalDate from = LocalDate.now();
        LocalDate to = LocalDate.now().minusDays(5);

        assertThrows(RuntimeException.class, () -> {
            customerService.getUserReport("customer1", from, to);
        });
    }

    // 7. FORGOT PASSWORD TESTS (SRS US21)
    @Test
    public void testForgotPasswordWorkflow() {
        // Unknown user
        assertThrows(RuntimeException.class, () -> {
            userService.getSecurityQuestion("non_existent_user_999");
        });

        // Valid user security question
        String q = userService.getSecurityQuestion("customer1");
        assertEquals("What is your pet name?", q);

        // Incorrect answer
        assertThrows(RuntimeException.class, () -> {
            userService.verifySecurityAnswer("customer1", "WrongAnswer123");
        });

        // Correct answer verification
        assertTrue(userService.verifySecurityAnswer("customer1", "Fluffy"));

        // Reset password
        userService.resetPassword("customer1", "Fluffy", "NewCustomerPass@123");

        // Verify login works with new password
        LoginResponse login = userService.authenticate(new LoginRequest("customer1", "NewCustomerPass@123"));
        assertNotNull(login);

        // Restore password for other tests
        userService.resetPassword("customer1", "Fluffy", "Customer@123");
    }

    // 8. IPDR MEDIATION & RATING INTEGRATION
    @Test
    @Transactional
    public void testIpdrTrafficSimulationAndRatingUpdate() {
        int simulated = ratingService.simulateUsageForCustomer("customer1", 3);
        assertEquals(3, simulated);

        CustomerHomeDto home = customerService.getCustomerHome("customer1");
        assertNotNull(home);
        assertTrue(home.getUsageInGb() > 0.0);
    }
}
