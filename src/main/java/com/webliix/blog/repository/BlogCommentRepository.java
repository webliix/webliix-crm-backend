package com.webliix.blog.repository;

import com.webliix.blog.entity.BlogComment;
import com.webliix.blog.enums.BlogCommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogCommentRepository extends JpaRepository<BlogComment, Long> {

    List<BlogComment> findByPostIdAndStatusOrderByCreatedAtAsc(Long postId, BlogCommentStatus status);

    List<BlogComment> findByPostIdAndParentIdIsNullAndStatusOrderByCreatedAtDesc(Long postId, BlogCommentStatus status);

    List<BlogComment> findByParentIdAndStatusOrderByCreatedAtAsc(Long parentId, BlogCommentStatus status);

    List<BlogComment> findByPostIdOrderByCreatedAtDesc(Long postId);

    Page<BlogComment> findByStatus(BlogCommentStatus status, Pageable pageable);

    long countByStatus(BlogCommentStatus status);

    long countByPostIdAndStatus(Long postId, BlogCommentStatus status);
}
