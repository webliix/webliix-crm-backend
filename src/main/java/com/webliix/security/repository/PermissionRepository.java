package com.webliix.security.repository;

import com.webliix.security.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByCode(String code);
    List<Permission> findByModule(String module);
    List<Permission> findAllByOrderByModuleAscCodeAsc();
    boolean existsByCode(String code);
}
