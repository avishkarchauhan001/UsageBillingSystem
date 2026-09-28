package com.billing.usagebilling.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.billing.usagebilling.entity.RecognizedDevice;

@Repository
public interface RecognizedDeviceRepository extends JpaRepository<RecognizedDevice, Long> {

    List<RecognizedDevice> findByStatus(String status);

    Optional<RecognizedDevice> findFirstByIpAddressAndStatus(String ipAddress, String status);

    Optional<RecognizedDevice> findFirstByMacAddressAndStatus(String macAddress, String status);

    Optional<RecognizedDevice> findFirstByHostnameAndStatus(String hostname, String status);

    boolean existsByIpAddressAndStatus(String ipAddress, String status);

    boolean existsByMacAddressAndStatus(String macAddress, String status);

    boolean existsByHostnameAndStatus(String hostname, String status);
}
