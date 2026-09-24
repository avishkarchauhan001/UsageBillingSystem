package com.billing.usagebilling.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.billing.usagebilling.entity.IpdrRecord;

@Repository
public interface IpdrRecordRepository extends JpaRepository<IpdrRecord, Long> {
    List<IpdrRecord> findByServiceIdentifierOrderBySessionStartDesc(String serviceIdentifier);
    List<IpdrRecord> findByUserIdOrderBySessionStartDesc(Long userId);
    List<IpdrRecord> findByServiceIdentifierAndSessionStartBetween(String serviceIdentifier, LocalDateTime start, LocalDateTime end);
    List<IpdrRecord> findByUserIdAndSessionStartBetweenOrderBySessionStartAsc(Long userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(i.inputOctets + i.outputOctets), 0) FROM IpdrRecord i WHERE i.serviceIdentifier = :serviceIdentifier AND i.sessionStart >= :start AND i.sessionStart < :end")
    Long sumTotalOctetsByServiceIdentifierAndPeriod(@Param("serviceIdentifier") String serviceIdentifier, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
