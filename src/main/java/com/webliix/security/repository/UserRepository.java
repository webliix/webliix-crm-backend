package com.webliix.security.repository;

import com.webliix.security.entity.Role;
import com.webliix.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRolesContaining(Role role);

    List<User> findByRolesContaining(Role role);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.id = :roleId")
    List<User> findByRoleId(@Param("roleId") Long roleId);

    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE (r.isSuperAdmin = true OR r.name = 'SUPER_ADMIN') AND u.enabled = true")
    long countActiveSuperAdmins();
}
