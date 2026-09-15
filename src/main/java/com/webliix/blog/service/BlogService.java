package com.webliix.blog.service;

import com.webliix.blog.dto.*;
import com.webliix.blog.enums.BlogCommentStatus;
import com.webliix.blog.enums.BlogPostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BlogService {

    BlogPostResponse createPost(CreateBlogPostRequest request);

    BlogPostResponse updatePost(Long id, CreateBlogPostRequest request);

    BlogPostResponse getPostById(Long id);

    BlogPostResponse getPostBySlug(String slug, boolean isPublicAccess, String clientIp, String userAgent);

    Page<BlogPostResponse> getAllPosts(BlogPostStatus status, Pageable pageable);

    Page<BlogPostResponse> getPublishedPosts(String category, Pageable pageable);

    Page<BlogPostResponse> searchPosts(String keyword, Pageable pageable);

    List<BlogPostResponse> getFeaturedPosts();

    List<BlogPostResponse> getRelatedPosts(Long postId, int limit);

    void deletePost(Long id);

    void setPostStatus(Long id, BlogPostStatus status);

    boolean likePost(Long id, String clientIp, String userAgent);

    BlogLikeResponse likePostByIdentifier(String identifier, String clientIp, String userAgent);

    boolean recordView(Long id, String clientIp, String userAgent);

    boolean recordViewByIdentifier(String identifier, String clientIp, String userAgent);

    BlogStatisticsResponse getStatistics();

    // Comments
    BlogCommentResponse addComment(Long postId, CreateBlogCommentRequest request, String clientIp, String userAgent);

    BlogCommentResponse addCommentByIdentifier(String identifier, CreateBlogCommentRequest request, String clientIp, String userAgent);

    List<BlogCommentResponse> getThreadedComments(Long postId, boolean publicOnly);

    List<BlogCommentResponse> getThreadedCommentsByIdentifier(String identifier, boolean publicOnly);

    Page<BlogCommentResponse> getAllCommentsForAdmin(BlogCommentStatus status, Pageable pageable);

    BlogCommentResponse updateCommentStatus(Long commentId, BlogCommentStatus status);

    void deleteComment(Long commentId);

    // Categories & Tags
    List<BlogCategoryResponse> getAllCategories();

    BlogCategoryResponse createCategory(CreateBlogCategoryRequest request);

    void deleteCategory(Long id);

    List<BlogTagResponse> getAllTags();

    BlogTagResponse createTag(CreateBlogTagRequest request);

    // Dynamic Sitemap
    String generateSitemapXml();
}
