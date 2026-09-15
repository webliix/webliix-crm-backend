package com.webliix.blog.repository;

import com.webliix.blog.entity.BlogPost;
import com.webliix.blog.enums.BlogPostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    Optional<BlogPost> findBySlug(String slug);

    Optional<BlogPost> findBySlugAndStatus(String slug, BlogPostStatus status);

    Optional<BlogPost> findByIdAndStatus(Long id, BlogPostStatus status);

    Page<BlogPost> findByStatus(BlogPostStatus status, Pageable pageable);

    Page<BlogPost> findByCategoryIgnoreCaseAndStatus(String category, BlogPostStatus status, Pageable pageable);

    List<BlogPost> findByIsFeaturedTrueAndStatusOrderByPublishedAtDesc(BlogPostStatus status);

    List<BlogPost> findByScheduledPublishAtBeforeAndStatus(LocalDateTime time, BlogPostStatus status);

    Page<BlogPost> findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCaseOrCategoryContainingIgnoreCaseOrTagsContainingIgnoreCase(
            String title, String summary, String category, String tags, Pageable pageable
    );

    @Query("SELECT b FROM BlogPost b WHERE b.status = :status AND b.id <> :postId AND (LOWER(b.category) = LOWER(:category) OR LOWER(b.tags) LIKE LOWER(CONCAT('%', :firstTag, '%'))) ORDER BY b.publishedAt DESC")
    List<BlogPost> findRelatedPosts(
            @Param("status") BlogPostStatus status,
            @Param("postId") Long postId,
            @Param("category") String category,
            @Param("firstTag") String firstTag,
            Pageable pageable
    );

    long countByStatus(BlogPostStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE BlogPost b SET b.viewsCount = b.viewsCount + 1 WHERE b.id = :id")
    void incrementViews(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE BlogPost b SET b.likesCount = b.likesCount + 1 WHERE b.id = :id")
    void incrementLikes(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE BlogPost b SET b.commentsCount = b.commentsCount + 1 WHERE b.id = :id")
    void incrementCommentsCount(@Param("id") Long id);
}
