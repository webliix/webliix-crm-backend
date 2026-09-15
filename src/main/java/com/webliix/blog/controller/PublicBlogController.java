package com.webliix.blog.controller;

import com.webliix.blog.dto.*;
import com.webliix.blog.service.BlogService;
import com.webliix.shared.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/public/blogs", "/api/v1/public/blogs/"})
@RequiredArgsConstructor
public class PublicBlogController {

    private final BlogService blogService;

    @GetMapping({"", "/"})
    public ResponseEntity<ApiResponse<Page<BlogPostResponse>>> getPublishedBlogs(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<BlogPostResponse> response = blogService.getPublishedPosts(category, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<BlogPostResponse>>builder()
                .success(true)
                .message("Published blogs fetched")
                .data(response)
                .build());
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<BlogPostResponse>>> getFeaturedBlogs() {
        return ResponseEntity.ok(ApiResponse.<List<BlogPostResponse>>builder()
                .success(true)
                .message("Featured blogs fetched")
                .data(blogService.getFeaturedPosts())
                .build());
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<BlogPostResponse>>> searchBlogs(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<BlogPostResponse> response = blogService.searchPosts(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<BlogPostResponse>>builder()
                .success(true)
                .message("Search results")
                .data(response)
                .build());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<BlogPostResponse>> getBlogBySlug(
            @PathVariable String slug,
            HttpServletRequest request
    ) {
        String clientIp = extractClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        BlogPostResponse response = blogService.getPostBySlug(slug, true, clientIp, userAgent);
        return ResponseEntity.ok(ApiResponse.<BlogPostResponse>builder()
                .success(true)
                .message("Blog fetched")
                .data(response)
                .build());
    }

    @GetMapping("/{id}/related")
    public ResponseEntity<ApiResponse<List<BlogPostResponse>>> getRelatedBlogs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "3") int limit
    ) {
        List<BlogPostResponse> response = blogService.getRelatedPosts(id, limit);
        return ResponseEntity.ok(ApiResponse.<List<BlogPostResponse>>builder()
                .success(true)
                .message("Related articles fetched")
                .data(response)
                .build());
    }

    @PostMapping({"/{identifier}/like", "/slug/{identifier}/like"})
    public ResponseEntity<ApiResponse<BlogLikeResponse>> likeBlog(
            @PathVariable String identifier,
            HttpServletRequest request
    ) {
        String clientIp = extractClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        BlogLikeResponse response = blogService.likePostByIdentifier(identifier, clientIp, userAgent);
        return ResponseEntity.ok(ApiResponse.<BlogLikeResponse>builder()
                .success(true)
                .message(response.getMessage())
                .data(response)
                .build());
    }

    @PostMapping({"/{identifier}/view", "/slug/{identifier}/view"})
    public ResponseEntity<ApiResponse<Boolean>> recordView(
            @PathVariable String identifier,
            HttpServletRequest request
    ) {
        String clientIp = extractClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        boolean recorded = blogService.recordViewByIdentifier(identifier, clientIp, userAgent);
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .success(true)
                .message(recorded ? "View recorded" : "View already counted")
                .data(recorded)
                .build());
    }

    @PostMapping({"/{identifier}/comments", "/slug/{identifier}/comments"})
    public ResponseEntity<ApiResponse<BlogCommentResponse>> addComment(
            @PathVariable String identifier,
            @Valid @RequestBody CreateBlogCommentRequest commentRequest,
            HttpServletRequest request
    ) {
        String clientIp = extractClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        BlogCommentResponse response = blogService.addCommentByIdentifier(identifier, commentRequest, clientIp, userAgent);
        return ResponseEntity.ok(ApiResponse.<BlogCommentResponse>builder()
                .success(true)
                .message("Comment posted successfully")
                .data(response)
                .build());
    }

    @GetMapping({"/{identifier}/comments", "/slug/{identifier}/comments"})
    public ResponseEntity<ApiResponse<List<BlogCommentResponse>>> getComments(@PathVariable String identifier) {
        List<BlogCommentResponse> response = blogService.getThreadedCommentsByIdentifier(identifier, true);
        return ResponseEntity.ok(ApiResponse.<List<BlogCommentResponse>>builder()
                .success(true)
                .message("Threaded comments fetched")
                .data(response)
                .build());
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<BlogCategoryResponse>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.<List<BlogCategoryResponse>>builder()
                .success(true)
                .message("Categories fetched")
                .data(blogService.getAllCategories())
                .build());
    }

    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<BlogTagResponse>>> getTags() {
        return ResponseEntity.ok(ApiResponse.<List<BlogTagResponse>>builder()
                .success(true)
                .message("Tags fetched")
                .data(blogService.getAllTags())
                .build());
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> getSitemap() {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(blogService.generateSitemapXml());
    }

    private String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(",")) {
            return xfHeader != null ? xfHeader : request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
