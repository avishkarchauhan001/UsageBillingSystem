package com.billing.usagebilling.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.billing.usagebilling.entity.CustomerPlan;

@Repository
public interface CustomerPlanRepository extends JpaRepository<CustomerPlan, Long> {
    Optional<CustomerPlan> findFirstByUserIdAndStatusOrderByStartDateDesc(Long userId, String status);
    List<CustomerPlan> findByUserIdOrderByStartDateDesc(Long userId);
    List<CustomerPlan> findByUserIdAndStatus(Long userId, String status);
    List<CustomerPlan> findByStatusAndExpiryDateBefore(String status, LocalDate date);
    List<CustomerPlan> findByStatusAndStartDateLessThanEqual(String status, LocalDate date);
    long countByPlanIdAndStatus(Long planId, String status);
    List<CustomerPlan> findByPlanIdAndStatus(Long planId, String status);
}
