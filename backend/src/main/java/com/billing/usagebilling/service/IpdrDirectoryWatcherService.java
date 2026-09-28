package com.billing.usagebilling.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.billing.usagebilling.dto.IpdrXmlIngestionResult;

@Service
@ConditionalOnProperty(name = "ipdr.auto-ingest.enabled", havingValue = "true", matchIfMissing = true)
public class IpdrDirectoryWatcherService {

    private static final Logger log = LoggerFactory.getLogger(IpdrDirectoryWatcherService.class);

    private final MediationAndRatingService ratingService;

    @Value("${ipdr.input.directory:c:/NetworkCapstoneProject/simulator/output/ipdr}")
    private String inputDirectory;

    @Value("${ipdr.archive.directory:c:/NetworkCapstoneProject/simulator/output/ipdr_archive}")
    private String archiveDirectory;

    public IpdrDirectoryWatcherService(MediationAndRatingService ratingService) {
        this.ratingService = ratingService;
    }

    /**
     * Periodically check for new XML files written by the standalone simulator.
     */
    @Scheduled(fixedDelayString = "${ipdr.poll.interval.ms:5000}", initialDelay = 5000)
    public void scanAndIngestXmlFiles() {
        try {
            IpdrXmlIngestionResult result = ratingService.ingestXmlFilesFromDirectory(inputDirectory, archiveDirectory);
            if (result.getTotalFilesProcessed() > 0) {
                log.info("[IPDR Directory Watcher] Ingested {} XML files ({} records accepted, {} records rejected).",
                        result.getTotalFilesProcessed(),
                        result.getAcceptedRecords(),
                        result.getRejectedRecords());
            }
        } catch (Exception ex) {
            log.error("[IPDR Directory Watcher] Error during directory scan: {}", ex.getMessage());
        }
    }
}
