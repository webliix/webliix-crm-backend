package com.webliix.security.repository;

import com.webliix.security.entity.UserPermissionOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPermissionOverrideRepository extends JpaRepository<UserPermissionOverride, Long> {
    List<UserPermissionOverride> findByUserId(Long userId);
    Optional<UserPermissionOverride> findByUserIdAndPermissionId(Long userId, Long permissionId);

    @Modifying
    @Query("DELETE FROM UserPermissionOverride upo WHERE upo.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM UserPermissionOverride upo WHERE upo.user.id = :userId AND upo.permission.id = :permissionId")
    void deleteByUserIdAndPermissionId(@Param("userId") Long userId, @Param("permissionId") Long permissionId);
}
