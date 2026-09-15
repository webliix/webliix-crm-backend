package com.webliix.blog.controller;

import com.webliix.blog.dto.*;
import com.webliix.blog.enums.BlogCommentStatus;
import com.webliix.blog.enums.BlogPostStatus;
import com.webliix.blog.enums.BlogResourceType;
import com.webliix.blog.service.BlogMediaService;
import com.webliix.blog.service.BlogService;
import com.webliix.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/blogs")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;
    private final BlogMediaService mediaService;

    @PostMapping
    public ResponseEntity<ApiResponse<BlogPostResponse>> createPost(@Valid @RequestBody CreateBlogPostRequest request) {
        BlogPostResponse response = blogService.createPost(request);
        return ResponseEntity.ok(ApiResponse.<BlogPostResponse>builder()
                .success(true)
                .message("Blog post created successfully")
                .data(response)
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BlogPostResponse>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody CreateBlogPostRequest request
    ) {
        BlogPostResponse response = blogService.updatePost(id, request);
        return ResponseEntity.ok(ApiResponse.<BlogPostResponse>builder()
                .success(true)
                .message("Blog post updated successfully")
                .data(response)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BlogPostResponse>> getPost(@PathVariable Long id) {
        BlogPostResponse response = blogService.getPostById(id);
        return ResponseEntity.ok(ApiResponse.<BlogPostResponse>builder()
                .success(true)
                .message("Blog post fetched")
                .data(response)
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BlogPostResponse>>> getAllPosts(
            @RequestParam(required = false) BlogPostStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<BlogPostResponse> response = blogService.getAllPosts(status, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<BlogPostResponse>>builder()
                .success(true)
                .message("Blog posts fetched")
                .data(response)
                .build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> setPostStatus(
            @PathVariable Long id,
            @RequestParam BlogPostStatus status
    ) {
        blogService.setPostStatus(id, status);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Post status updated to " + status)
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        blogService.deletePost(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Blog post deleted")
                .build());
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<BlogStatisticsResponse>> getStatistics() {
        BlogStatisticsResponse response = blogService.getStatistics();
        return ResponseEntity.ok(ApiResponse.<BlogStatisticsResponse>builder()
                .success(true)
                .message("Blog statistics fetched")
                .data(response)
                .build());
    }

    // Media Upload via Cloudinary
    @PostMapping("/media/upload")
    public ResponseEntity<ApiResponse<BlogMediaResponse>> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "content") String folder,
            @RequestParam(value = "altText", required = false) String altText,
            @RequestParam(value = "caption", required = false) String caption,
            @RequestParam(value = "postId", required = false) Long postId
    ) throws Exception {
        BlogMediaResponse response = mediaService.uploadMedia(file, folder, altText, caption, postId);
        return ResponseEntity.ok(ApiResponse.<BlogMediaResponse>builder()
                .success(true)
                .message("Media uploaded successfully to Cloudinary")
                .data(response)
                .build());
    }

    @DeleteMapping("/media/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMedia(@PathVariable Long id) throws Exception {
        mediaService.deleteMedia(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Media deleted successfully")
                .build());
    }

    @GetMapping("/media")
    public ResponseEntity<ApiResponse<Page<BlogMediaResponse>>> getMedia(
            @RequestParam(required = false) BlogResourceType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BlogMediaResponse> response = mediaService.getAllMedia(type, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<BlogMediaResponse>>builder()
                .success(true)
                .message("Media assets fetched")
                .data(response)
                .build());
    }

    // Comment Moderation
    @GetMapping("/comments")
    public ResponseEntity<ApiResponse<Page<BlogCommentResponse>>> getCommentsForModeration(
            @RequestParam(required = false) BlogCommentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BlogCommentResponse> response = blogService.getAllCommentsForAdmin(status, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<BlogCommentResponse>>builder()
                .success(true)
                .message("Comments fetched for moderation")
                .data(response)
                .build());
    }

    @PutMapping("/comments/{id}/status")
    public ResponseEntity<ApiResponse<BlogCommentResponse>> updateCommentStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCommentStatusRequest request
    ) {
        BlogCommentResponse response = blogService.updateCommentStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.<BlogCommentResponse>builder()
                .success(true)
                .message("Comment status updated")
                .data(response)
                .build());
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long id) {
        blogService.deleteComment(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Comment deleted")
                .build());
    }

    // Categories & Tags
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<BlogCategoryResponse>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.<List<BlogCategoryResponse>>builder()
                .success(true)
                .message("Categories fetched")
                .data(blogService.getAllCategories())
                .build());
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<BlogCategoryResponse>> createCategory(@Valid @RequestBody CreateBlogCategoryRequest req) {
        return ResponseEntity.ok(ApiResponse.<BlogCategoryResponse>builder()
                .success(true)
                .message("Category created")
                .data(blogService.createCategory(req))
                .build());
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        blogService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Category deleted").build());
    }

    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<BlogTagResponse>>> getTags() {
        return ResponseEntity.ok(ApiResponse.<List<BlogTagResponse>>builder()
                .success(true)
                .message("Tags fetched")
                .data(blogService.getAllTags())
                .build());
    }

    @PostMapping("/tags")
    public ResponseEntity<ApiResponse<BlogTagResponse>> createTag(@Valid @RequestBody CreateBlogTagRequest req) {
        return ResponseEntity.ok(ApiResponse.<BlogTagResponse>builder()
                .success(true)
                .message("Tag created")
                .data(blogService.createTag(req))
                .build());
    }
}
