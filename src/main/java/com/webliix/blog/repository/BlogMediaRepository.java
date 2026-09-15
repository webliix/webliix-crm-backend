package com.webliix.blog.repository;

import com.webliix.blog.entity.BlogMedia;
import com.webliix.blog.enums.BlogResourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogMediaRepository extends JpaRepository<BlogMedia, Long> {

    Optional<BlogMedia> findByPublicId(String publicId);

    List<BlogMedia> findByPostId(Long postId);

    Page<BlogMedia> findByResourceType(BlogResourceType resourceType, Pageable pageable);
}
