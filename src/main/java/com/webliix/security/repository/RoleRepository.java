package com.webliix.security.repository;

import com.webliix.security.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    Optional<Role> findByNameIgnoreCase(String name);

    boolean existsByName(String name);

    boolean existsByNameIgnoreCase(String name);

    long countByIsSuperAdminTrueAndActiveTrue();

    List<Role> findAllByActiveTrue();

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.id = :roleId")
    long countUsersAssignedToRole(@Param("roleId") Long roleId);
}
