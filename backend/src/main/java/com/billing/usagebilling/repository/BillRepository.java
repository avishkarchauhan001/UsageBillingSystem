package com.billing.usagebilling.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.billing.usagebilling.entity.Bill;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByUserIdOrderByGeneratedDateDesc(Long userId);
    List<Bill> findByUserIdAndStatusOrderByGeneratedDateDesc(Long userId, String status);
    Optional<Bill> findByBillNumber(String billNumber);
    Optional<Bill> findByIdAndUserId(Long id, Long userId);
    Optional<Bill> findByBillNumberAndUserId(String billNumber, Long userId);
    List<Bill> findByUserIdAndBillNumberContainingIgnoreCase(Long userId, String query);
    List<Bill> findByUserIdAndBillingStartDateBetween(Long userId, LocalDate fromDate, LocalDate toDate);
    long countByUserIdAndStatus(Long userId, String status);
}
