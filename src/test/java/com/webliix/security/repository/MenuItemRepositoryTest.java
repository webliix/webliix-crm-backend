package com.webliix.security.repository;

import com.webliix.security.entity.MenuItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MenuItemRepositoryTest {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Test
    void testMenuItemKeyColumnMappingAndQueries() {
        MenuItem item = MenuItem.builder()
                .key("test-analytics")
                .label("Test Analytics")
                .path("/analytics")
                .icon("BarChartOutlined")
                .module("Reports")
                .requiredPermission("REPORTS_FINANCE_VIEW")
                .sortOrder(50)
                .active(true)
                .build();

        MenuItem saved = menuItemRepository.save(item);
        assertNotNull(saved.getId());
        assertEquals("test-analytics", saved.getKey());

        Optional<MenuItem> found = menuItemRepository.findByKey("test-analytics");
        assertTrue(found.isPresent());
        assertEquals("Test Analytics", found.get().getLabel());
        assertEquals("REPORTS_FINANCE_VIEW", found.get().getRequiredPermission());

        assertTrue(menuItemRepository.existsByKey("test-analytics"));
        assertFalse(menuItemRepository.existsByKey("non-existent-key"));

        List<MenuItem> activeItems = menuItemRepository.findByActiveTrueOrderBySortOrderAsc();
        assertFalse(activeItems.isEmpty());
    }
}
