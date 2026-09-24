package com.billing.usagebilling.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.billing.usagebilling.entity.Plan;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findByPackageName(String packageName);
    boolean existsByPackageName(String packageName);
    List<Plan> findByPlanState(String planState);
    List<Plan> findByPlanStateOrderByMonthlyChargeUsdAsc(String planState);
}
