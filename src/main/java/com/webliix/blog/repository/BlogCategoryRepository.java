package com.webliix.blog.repository;

import com.webliix.blog.entity.BlogCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlogCategoryRepository extends JpaRepository<BlogCategory, Long> {

    Optional<BlogCategory> findBySlug(String slug);

    Optional<BlogCategory> findByNameIgnoreCase(String name);

    boolean existsBySlug(String slug);
}
