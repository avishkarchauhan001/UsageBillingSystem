package com.billing.usagebilling.controller;

import java.util.List;
import java.util.Map;

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

import com.billing.usagebilling.dto.IpdrSimulateRequest;
import com.billing.usagebilling.entity.IpdrRecord;
import com.billing.usagebilling.repository.IpdrRecordRepository;
import com.billing.usagebilling.service.MediationAndRatingService;

@RestController
@RequestMapping("/api/ipdr")
@CrossOrigin(origins = "*")
public class IpdrController {

    private final MediationAndRatingService ratingService;
    private final IpdrRecordRepository ipdrRepository;

    public IpdrController(MediationAndRatingService ratingService, IpdrRecordRepository ipdrRepository) {
        this.ratingService = ratingService;
        this.ipdrRepository = ipdrRepository;
    }

    /**
     * Mediate an incoming IPDR session record
     */
    @PostMapping("/ingest")
    public ResponseEntity<IpdrRecord> ingestIpdr(@RequestBody IpdrSimulateRequest request) {
        return new ResponseEntity<>(ratingService.processIpdrRecord(request), HttpStatus.CREATED);
    }

    /**
     * Standalone Simulator trigger (SRS US16 / 3.3.6)
     */
    @PostMapping("/simulate/{username}")
    public ResponseEntity<Map<String, Object>> simulateTraffic(
            @PathVariable String username,
            @RequestParam(defaultValue = "3") int count) {
        int simulated = ratingService.simulateUsageForCustomer(username, count);
        return ResponseEntity.ok(Map.of(
                "message", "Successfully simulated " + simulated + " IPDR sessions for " + username,
                "sessionsSimulated", simulated
        ));
    }

    /**
     * View IPDR records for customer
     */
    @GetMapping("/records/{username}")
    public ResponseEntity<List<IpdrRecord>> getRecords(@PathVariable String username) {
        return ResponseEntity.ok(ipdrRepository.findByServiceIdentifierOrderBySessionStartDesc(username));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }
}
