package com.webliix.tenant.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.webliix.tenant.entity.Tenant;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByName(String name);
    Optional<Tenant> findBySubdomain(String subdomain);
}
