package com.webliix.security.repository;

import com.webliix.security.entity.UserApiToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserApiTokenRepository extends JpaRepository<UserApiToken, Long> {
    List<UserApiToken> findByUserIdOrderByCreatedAtDesc(Long userId);
}
