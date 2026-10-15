package com.webliix.security.repository;

import com.webliix.security.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByActiveTrueOrderBySortOrderAsc();
    List<MenuItem> findAllByOrderBySortOrderAsc();
    Optional<MenuItem> findByKey(String key);
    boolean existsByKey(String key);
}
