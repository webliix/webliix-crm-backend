package com.webliix.monitoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.webliix.monitoring.entity.TenantMetrics;

@Repository
public interface TenantMetricsRepository extends JpaRepository<TenantMetrics, Long> {
}
