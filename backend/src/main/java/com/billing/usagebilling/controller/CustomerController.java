package com.billing.usagebilling.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.billing.usagebilling.dto.BillDto;
import com.billing.usagebilling.dto.CustomerHomeDto;
import com.billing.usagebilling.dto.PaymentRequest;
import com.billing.usagebilling.dto.PaymentResponse;
import com.billing.usagebilling.dto.PlanChangeRequest;
import com.billing.usagebilling.dto.PlanChangeResponse;
import com.billing.usagebilling.dto.PlanDto;
import com.billing.usagebilling.dto.UsageReportResponse;
import com.billing.usagebilling.service.CustomerService;
import com.billing.usagebilling.service.PlanService;

@RestController
@RequestMapping("/api/customer")
@CrossOrigin(origins = "*")
public class CustomerController {

    private final CustomerService customerService;
    private final PlanService planService;

    public CustomerController(CustomerService customerService, PlanService planService) {
        this.customerService = customerService;
        this.planService = planService;
    }

    /**
     * SRS US15: Customer Home / Current Usage Details
     */
    @GetMapping("/home")
    public ResponseEntity<CustomerHomeDto> getCustomerHome(@RequestParam String username) {
        return ResponseEntity.ok(customerService.getCustomerHome(username));
    }

    /**
     * SRS US16: Bill Details & Transaction History
     */
    @GetMapping("/bills")
    public ResponseEntity<List<BillDto>> getCustomerBills(@RequestParam String username) {
        return ResponseEntity.ok(customerService.getCustomerBills(username));
    }

    @GetMapping("/bills/search")
    public ResponseEntity<List<BillDto>> searchCustomerBills(
            @RequestParam String username,
            @RequestParam(required = false, defaultValue = "") String query) {
        return ResponseEntity.ok(customerService.searchCustomerBills(username, query));
    }

    @GetMapping("/bills/{billId}")
    public ResponseEntity<BillDto> getCustomerBill(
            @PathVariable Long billId,
            @RequestParam String username) {
        return ResponseEntity.ok(customerService.getCustomerBillById(username, billId));
    }

    /**
     * SRS US18: Pending Bills
     */
    @GetMapping("/bills/pending")
    public ResponseEntity<List<BillDto>> getPendingBills(@RequestParam String username) {
        return ResponseEntity.ok(customerService.getPendingBills(username));
    }

    /**
     * SRS US17: Payment
     */
    @PostMapping("/bills/{billId}/pay")
    public ResponseEntity<PaymentResponse> payBill(
            @PathVariable Long billId,
            @RequestParam String username,
            @RequestBody(required = false) PaymentRequest request) {
        String paymentMode = (request != null && request.getPaymentMode() != null) 
                ? request.getPaymentMode() 
                : "Online";
        return ResponseEntity.ok(customerService.payBill(username, billId, paymentMode));
    }

    /**
     * Available Active Plans for Customer Change Plan selection
     */
    @GetMapping("/plans")
    public ResponseEntity<List<PlanDto>> getActivePlans() {
        return ResponseEntity.ok(planService.getActivePlans());
    }

    /**
     * SRS US19: Change Plan
     */
    @PostMapping("/change-plan")
    public ResponseEntity<PlanChangeResponse> changePlan(
            @RequestParam String username,
            @RequestBody PlanChangeRequest request) {
        return ResponseEntity.ok(customerService.changePlan(username, request.getNewPlanId()));
    }

    /**
     * SRS US20: User Report
     */
    @GetMapping("/report")
    public ResponseEntity<UsageReportResponse> getUserReport(
            @RequestParam String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(customerService.getUserReport(username, fromDate, toDate));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }
}
