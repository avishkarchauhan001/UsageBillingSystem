package com.billing.usagebilling.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import com.billing.usagebilling.dto.IpdrXmlIngestionResult;
import com.billing.usagebilling.entity.IpdrRecord;
import com.billing.usagebilling.repository.IpdrRecordRepository;
import com.billing.usagebilling.repository.RecognizedDeviceRepository;
import com.billing.usagebilling.service.MediationAndRatingService;

@RestController
@RequestMapping("/api/ipdr")
@CrossOrigin(origins = "*")
public class IpdrController {

    private final MediationAndRatingService ratingService;
    private final IpdrRecordRepository ipdrRepository;
    private final RecognizedDeviceRepository deviceRepository;

    public IpdrController(
            MediationAndRatingService ratingService,
            IpdrRecordRepository ipdrRepository,
            RecognizedDeviceRepository deviceRepository) {
        this.ratingService = ratingService;
        this.ipdrRepository = ipdrRepository;
        this.deviceRepository = deviceRepository;
    }

    /**
     * Mediate an incoming IPDR session record (JSON)
     */
    @PostMapping("/ingest")
    public ResponseEntity<IpdrRecord> ingestIpdr(@RequestBody IpdrSimulateRequest request) {
        return new ResponseEntity<>(ratingService.processIpdrRecord(request), HttpStatus.CREATED);
    }

    /**
     * Ingest raw IPDR XML document (SRS US16 canonical XML intake)
     */
    @PostMapping(value = "/upload-xml", consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE, MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<IpdrXmlIngestionResult> uploadIpdrXml(@RequestBody String xmlContent) {
        IpdrXmlIngestionResult result = ratingService.processIpdrXmlContent(xmlContent);
        return ResponseEntity.ok(result);
    }

    /**
     * Poll and process all XML files produced by the Standalone Simulator in the output directory
     */
    @PostMapping("/poll-simulator")
    public ResponseEntity<IpdrXmlIngestionResult> pollSimulatorDirectory(
            @RequestParam(required = false) String inputDir,
            @RequestParam(required = false) String archiveDir) {
        IpdrXmlIngestionResult result = ratingService.ingestXmlFilesFromDirectory(inputDir, archiveDir);
        return ResponseEntity.ok(result);
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
     * SRS US16 / Customer Ingest Usage: Simulates session traffic, applies rating,
     * and downloads canonical IPDR XML file.
     */
    @GetMapping(value = "/simulate-and-download/{username}", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> simulateAndDownloadXml(@PathVariable String username) {
        String xml = ratingService.simulateUsageAndGenerateXml(username);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ipdr_usage_" + username + ".xml\"")
                .header(org.springframework.http.HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
                .contentType(MediaType.APPLICATION_XML)
                .body(xml);
    }

    @PostMapping(value = "/simulate-and-download/{username}", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> simulateAndDownloadXmlPost(@PathVariable String username) {
        return simulateAndDownloadXml(username);
    }

    /**
     * View IPDR records for customer
     */
    @GetMapping("/records/{username}")
    public ResponseEntity<List<IpdrRecord>> getRecords(@PathVariable String username) {
        return ResponseEntity.ok(ipdrRepository.findByServiceIdentifierOrderBySessionStartDesc(username));
    }

    /**
     * System status for IPDR and recognized devices
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "activeRecognizedDevices", deviceRepository.findByStatus("ACTIVE").size(),
                "totalIpdrRecordsStored", ipdrRepository.count()
        ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }
}
