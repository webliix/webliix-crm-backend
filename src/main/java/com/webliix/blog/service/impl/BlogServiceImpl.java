package com.webliix.blog.service.impl;

import com.webliix.blog.dto.*;
import com.webliix.blog.entity.BlogCategory;
import com.webliix.blog.entity.BlogComment;
import com.webliix.blog.entity.BlogPost;
import com.webliix.blog.entity.BlogTag;
import com.webliix.blog.enums.BlogCommentStatus;
import com.webliix.blog.enums.BlogPostStatus;
import com.webliix.blog.repository.BlogCategoryRepository;
import com.webliix.blog.repository.BlogCommentRepository;
import com.webliix.blog.repository.BlogPostRepository;
import com.webliix.blog.repository.BlogTagRepository;
import com.webliix.blog.service.BlogEngagementService;
import com.webliix.blog.service.BlogSanitizerService;
import com.webliix.blog.service.BlogService;
import com.webliix.security.repository.UserRepository;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogPostRepository postRepository;
    private final BlogCommentRepository commentRepository;
    private final BlogCategoryRepository categoryRepository;
    private final BlogTagRepository tagRepository;
    private final BlogSanitizerService sanitizerService;
    private final BlogEngagementService engagementService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BlogPostResponse createPost(CreateBlogPostRequest request) {
        String slug = generateSlug(request.getSlug(), request.getTitle());
        BlogPostStatus status = request.getStatus() != null ? request.getStatus() : BlogPostStatus.DRAFT;
        String sanitizedContent = sanitizerService.sanitize(request.getContent());
        int readingTime = calculateReadingTime(sanitizedContent);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime publishedAt = null;
        if (status == BlogPostStatus.PUBLISHED) {
            publishedAt = now;
        }

        Long currentUserId = resolveCurrentUserId();

        String seoTitle = request.getSeoTitle() != null && !request.getSeoTitle().isBlank() ? request.getSeoTitle() : request.getTitle();
        String seoDescription = request.getSeoDescription() != null && !request.getSeoDescription().isBlank() ? request.getSeoDescription() : request.getSummary();
        String canonicalUrl = request.getCanonicalUrl() != null && !request.getCanonicalUrl().isBlank() ? request.getCanonicalUrl() : "https://webliix.com/blog/" + slug;
        String ogImage = request.getOgImageUrl() != null && !request.getOgImageUrl().isBlank() ? request.getOgImageUrl() : request.getCoverImageUrl();

        BlogPost post = BlogPost.builder()
                .title(request.getTitle())
                .slug(slug)
                .summary(request.getSummary())
                .content(sanitizedContent)
                .coverImageUrl(request.getCoverImageUrl())
                .coverImageAlt(request.getCoverImageAlt())
                .coverImageCaption(request.getCoverImageCaption())
                .authorName(request.getAuthorName() != null ? request.getAuthorName() : "Webliix Editorial")
                .authorId(currentUserId)
                .category(request.getCategory() != null ? request.getCategory() : "Engineering & Technology")
                .tags(request.getTags())
                .status(status)
                .isFeatured(Boolean.TRUE.equals(request.getIsFeatured()))
                .viewsCount(0L)
                .likesCount(0L)
                .commentsCount(0L)
                .readingTimeMinutes(request.getReadingTimeMinutes() != null ? request.getReadingTimeMinutes() : readingTime)
                .seoTitle(seoTitle)
                .seoDescription(seoDescription)
                .canonicalUrl(canonicalUrl)
                .ogImageUrl(ogImage)
                .enableAds(request.getEnableAds() != null ? request.getEnableAds() : true)
                .adSenseClientId(request.getAdSenseClientId())
                .topAdSlotId(request.getTopAdSlotId())
                .inlineAdSlotId(request.getInlineAdSlotId())
                .bottomAdSlotId(request.getBottomAdSlotId())
                .adFormat(request.getAdFormat() != null ? request.getAdFormat() : "auto")
                .coverImageAlignment(request.getCoverImageAlignment() != null ? request.getCoverImageAlignment() : "center")
                .coverImageAspectRatio(request.getCoverImageAspectRatio() != null ? request.getCoverImageAspectRatio() : "16:9")
                .scheduledPublishAt(request.getScheduledPublishAt())
                .publishedAt(publishedAt)
                .createdAt(now)
                .updatedAt(now)
                .build();

        BlogPost saved = postRepository.save(post);

        // Auto-create category/tags if new
        autoRegisterCategoryAndTags(saved.getCategory(), saved.getTags());

        return toResponse(saved, false);
    }

    @Override
    @Transactional
    public BlogPostResponse updatePost(Long id, CreateBlogPostRequest request) {
        BlogPost post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found with id: " + id));

        post.setTitle(request.getTitle());
        if (request.getSlug() != null && !request.getSlug().isBlank() && !request.getSlug().equalsIgnoreCase(post.getSlug())) {
            post.setSlug(generateSlug(request.getSlug(), request.getTitle()));
        }
        post.setSummary(request.getSummary());
        post.setContent(sanitizerService.sanitize(request.getContent()));
        post.setCoverImageUrl(request.getCoverImageUrl());
        post.setCoverImageAlt(request.getCoverImageAlt());
        post.setCoverImageCaption(request.getCoverImageCaption());
        if (request.getAuthorName() != null) post.setAuthorName(request.getAuthorName());
        if (request.getCategory() != null) post.setCategory(request.getCategory());
        post.setTags(request.getTags());
        if (request.getIsFeatured() != null) post.setIsFeatured(request.getIsFeatured());

        if (request.getStatus() != null) {
            if (request.getStatus() == BlogPostStatus.PUBLISHED && post.getPublishedAt() == null) {
                post.setPublishedAt(LocalDateTime.now());
            }
            post.setStatus(request.getStatus());
        }

        post.setScheduledPublishAt(request.getScheduledPublishAt());
        post.setReadingTimeMinutes(
                request.getReadingTimeMinutes() != null ? request.getReadingTimeMinutes() : calculateReadingTime(post.getContent())
        );

        post.setSeoTitle(request.getSeoTitle() != null ? request.getSeoTitle() : post.getTitle());
        post.setSeoDescription(request.getSeoDescription() != null ? request.getSeoDescription() : post.getSummary());
        post.setCanonicalUrl(request.getCanonicalUrl() != null ? request.getCanonicalUrl() : "https://webliix.com/blog/" + post.getSlug());
        post.setOgImageUrl(request.getOgImageUrl() != null ? request.getOgImageUrl() : post.getCoverImageUrl());

        if (request.getEnableAds() != null) post.setEnableAds(request.getEnableAds());
        if (request.getAdSenseClientId() != null) post.setAdSenseClientId(request.getAdSenseClientId());
        if (request.getTopAdSlotId() != null) post.setTopAdSlotId(request.getTopAdSlotId());
        if (request.getInlineAdSlotId() != null) post.setInlineAdSlotId(request.getInlineAdSlotId());
        if (request.getBottomAdSlotId() != null) post.setBottomAdSlotId(request.getBottomAdSlotId());
        if (request.getAdFormat() != null) post.setAdFormat(request.getAdFormat());
        if (request.getCoverImageAlignment() != null) post.setCoverImageAlignment(request.getCoverImageAlignment());
        if (request.getCoverImageAspectRatio() != null) post.setCoverImageAspectRatio(request.getCoverImageAspectRatio());

        post.setUpdatedAt(LocalDateTime.now());

        BlogPost saved = postRepository.save(post);
        autoRegisterCategoryAndTags(saved.getCategory(), saved.getTags());

        return toResponse(saved, false);
    }

    @Override
    public BlogPostResponse getPostById(Long id) {
        BlogPost post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found with id: " + id));
        return toResponse(post, true);
    }

    @Override
    @Transactional
    public BlogPostResponse getPostBySlug(String slugOrId, boolean isPublicAccess, String clientIp, String userAgent) {
        BlogPost post = null;
        if (slugOrId != null && slugOrId.matches("\\d+")) {
            try {
                Long id = Long.parseLong(slugOrId);
                post = isPublicAccess
                        ? postRepository.findByIdAndStatus(id, BlogPostStatus.PUBLISHED).orElse(null)
                        : postRepository.findById(id).orElse(null);
            } catch (NumberFormatException ignored) {}
        }
        if (post == null) {
            post = isPublicAccess
                    ? postRepository.findBySlugAndStatus(slugOrId, BlogPostStatus.PUBLISHED).orElse(null)
                    : postRepository.findBySlug(slugOrId).orElse(null);
        }
        if (post == null) {
            throw new ResourceNotFoundException("Article not found: " + slugOrId);
        }

        // Record deduplicated view
        if (isPublicAccess && engagementService.recordView(post.getId(), clientIp, userAgent)) {
            post.setViewsCount((post.getViewsCount() != null ? post.getViewsCount() : 0L) + 1);
        }

        return toResponse(post, isPublicAccess);
    }

    @Override
    public Page<BlogPostResponse> getAllPosts(BlogPostStatus status, Pageable pageable) {
        if (status != null) {
            return postRepository.findByStatus(status, pageable).map(p -> toResponse(p, false));
        }
        return postRepository.findAll(pageable).map(p -> toResponse(p, false));
    }

    @Override
    public Page<BlogPostResponse> getPublishedPosts(String category, Pageable pageable) {
        if (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) {
            return postRepository.findByCategoryIgnoreCaseAndStatus(category, BlogPostStatus.PUBLISHED, pageable)
                    .map(p -> toResponse(p, false));
        }
        return postRepository.findByStatus(BlogPostStatus.PUBLISHED, pageable)
                .map(p -> toResponse(p, false));
    }

    @Override
    public Page<BlogPostResponse> searchPosts(String keyword, Pageable pageable) {
        return postRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCaseOrCategoryContainingIgnoreCaseOrTagsContainingIgnoreCase(
                keyword, keyword, keyword, keyword, pageable
        ).map(p -> toResponse(p, false));
    }

    @Override
    public List<BlogPostResponse> getFeaturedPosts() {
        return postRepository.findByIsFeaturedTrueAndStatusOrderByPublishedAtDesc(BlogPostStatus.PUBLISHED)
                .stream().map(p -> toResponse(p, false)).toList();
    }

    @Override
    public List<BlogPostResponse> getRelatedPosts(Long postId, int limit) {
        BlogPost post = postRepository.findById(postId).orElse(null);
        if (post == null) return Collections.emptyList();

        String firstTag = "";
        if (post.getTags() != null && !post.getTags().isBlank()) {
            firstTag = post.getTags().split(",")[0].trim();
        }

        Pageable pageable = PageRequest.of(0, limit);
        return postRepository.findRelatedPosts(BlogPostStatus.PUBLISHED, postId, post.getCategory(), firstTag, pageable)
                .stream().map(p -> toResponse(p, false)).toList();
    }

    @Override
    @Transactional
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Blog post not found with id: " + id);
        }
        postRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void setPostStatus(Long id, BlogPostStatus status) {
        BlogPost post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found with id: " + id));

        post.setStatus(status);
        if (status == BlogPostStatus.PUBLISHED && post.getPublishedAt() == null) {
            post.setPublishedAt(LocalDateTime.now());
        }
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);
    }

    @Override
    @Transactional
    public boolean likePost(Long id, String clientIp, String userAgent) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Blog post not found with id: " + id);
        }
        return engagementService.recordLike(id, clientIp, userAgent);
    }

    @Override
    @Transactional
    public BlogLikeResponse likePostByIdentifier(String identifier, String clientIp, String userAgent) {
        BlogPost post = null;
        if (identifier != null && identifier.matches("\\d+")) {
            try {
                Long id = Long.parseLong(identifier);
                post = postRepository.findById(id).orElse(null);
            } catch (NumberFormatException ignored) {}
        }
        if (post == null) {
            post = postRepository.findBySlug(identifier)
                    .orElseThrow(() -> new ResourceNotFoundException("Blog post not found with identifier: " + identifier));
        }

        // Always increment likes in DB
        postRepository.incrementLikes(post.getId());

        // Refetch entity after clearAutomatically flushes persistence context
        BlogPost refreshed = postRepository.findById(post.getId()).orElse(post);
        long currentLikes = refreshed.getLikesCount() != null ? refreshed.getLikesCount() : 0L;

        return BlogLikeResponse.builder()
                .postId(refreshed.getId())
                .slug(refreshed.getSlug())
                .likesCount(currentLikes)
                .recorded(true)
                .message("Blog liked successfully")
                .build();
    }

    @Override
    public boolean recordView(Long id, String clientIp, String userAgent) {
        if (!postRepository.existsById(id)) return false;
        return engagementService.recordView(id, clientIp, userAgent);
    }

    @Override
    public boolean recordViewByIdentifier(String identifier, String clientIp, String userAgent) {
        BlogPost post = null;
        if (identifier != null && identifier.matches("\\d+")) {
            try {
                Long id = Long.parseLong(identifier);
                post = postRepository.findById(id).orElse(null);
            } catch (NumberFormatException ignored) {}
        }
        if (post == null) {
            post = postRepository.findBySlug(identifier).orElse(null);
        }
        if (post == null) return false;
        return engagementService.recordView(post.getId(), clientIp, userAgent);
    }

    @Override
    public BlogStatisticsResponse getStatistics() {
        long total = postRepository.count();
        long published = postRepository.countByStatus(BlogPostStatus.PUBLISHED);
        long draft = postRepository.countByStatus(BlogPostStatus.DRAFT);
        long totalViews = postRepository.findAll().stream().mapToLong(p -> p.getViewsCount() != null ? p.getViewsCount() : 0L).sum();
        long totalLikes = postRepository.findAll().stream().mapToLong(p -> p.getLikesCount() != null ? p.getLikesCount() : 0L).sum();
        long totalComments = commentRepository.count();

        return BlogStatisticsResponse.builder()
                .totalPosts(total)
                .publishedPosts(published)
                .draftPosts(draft)
                .totalViews(totalViews)
                .totalLikes(totalLikes)
                .totalComments(totalComments)
                .build();
    }

    @Override
    @Transactional
    public BlogCommentResponse addComment(Long postId, CreateBlogCommentRequest request, String clientIp, String userAgent) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Blog post not found with id: " + postId);
        }

        String authorName = (request.getAuthorName() != null && !request.getAuthorName().isBlank())
                ? request.getAuthorName().trim()
                : "Anonymous Reader";

        String rawContent = request.getContent() != null ? request.getContent().trim() : "";
        String cleanContent = sanitizerService.sanitize(rawContent);
        if ((cleanContent == null || cleanContent.isBlank()) && !rawContent.isBlank()) {
            cleanContent = rawContent;
        }

        BlogComment comment = BlogComment.builder()
                .postId(postId)
                .parentId(request.getParentId())
                .authorName(authorName)
                .authorEmail(request.getAuthorEmail() != null ? request.getAuthorEmail().trim() : null)
                .authorWebsite(request.getAuthorWebsite() != null ? request.getAuthorWebsite().trim() : null)
                .content(cleanContent)
                .status(BlogCommentStatus.APPROVED)
                .ipAddress(clientIp)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        BlogComment saved = commentRepository.save(comment);
        postRepository.incrementCommentsCount(postId);

        return toCommentResponse(saved);
    }

    @Override
    public List<BlogCommentResponse> getThreadedComments(Long postId, boolean publicOnly) {
        List<BlogComment> allComments = publicOnly
                ? commentRepository.findByPostIdAndStatusOrderByCreatedAtAsc(postId, BlogCommentStatus.APPROVED)
                : commentRepository.findByPostIdOrderByCreatedAtDesc(postId);

        Map<Long, List<BlogComment>> repliesMap = new HashMap<>();
        List<BlogComment> rootComments = new ArrayList<>();

        for (BlogComment c : allComments) {
            if (c.getParentId() == null || c.getParentId() == 0L) {
                rootComments.add(c);
            } else {
                repliesMap.computeIfAbsent(c.getParentId(), k -> new ArrayList<>()).add(c);
            }
        }

        return rootComments.stream().map(root -> buildCommentTree(root, repliesMap)).toList();
    }

    private BlogCommentResponse buildCommentTree(BlogComment comment, Map<Long, List<BlogComment>> repliesMap) {
        BlogCommentResponse response = toCommentResponse(comment);
        List<BlogComment> children = repliesMap.getOrDefault(comment.getId(), Collections.emptyList());
        if (!children.isEmpty()) {
            response.setReplies(children.stream().map(child -> buildCommentTree(child, repliesMap)).toList());
        } else {
            response.setReplies(new ArrayList<>());
        }
        return response;
    }

    @Override
    @Transactional
    public BlogCommentResponse addCommentByIdentifier(String identifier, CreateBlogCommentRequest request, String clientIp, String userAgent) {
        BlogPost post = null;
        if (identifier != null && identifier.matches("\\d+")) {
            try {
                Long id = Long.parseLong(identifier);
                post = postRepository.findById(id).orElse(null);
            } catch (NumberFormatException ignored) {}
        }
        if (post == null) {
            post = postRepository.findBySlug(identifier)
                    .orElseThrow(() -> new ResourceNotFoundException("Blog post not found with identifier: " + identifier));
        }
        return addComment(post.getId(), request, clientIp, userAgent);
    }

    @Override
    public List<BlogCommentResponse> getThreadedCommentsByIdentifier(String identifier, boolean publicOnly) {
        BlogPost post = null;
        if (identifier != null && identifier.matches("\\d+")) {
            try {
                Long id = Long.parseLong(identifier);
                post = postRepository.findById(id).orElse(null);
            } catch (NumberFormatException ignored) {}
        }
        if (post == null) {
            post = postRepository.findBySlug(identifier).orElse(null);
        }
        if (post == null) return Collections.emptyList();
        return getThreadedComments(post.getId(), publicOnly);
    }

    @Override
    public Page<BlogCommentResponse> getAllCommentsForAdmin(BlogCommentStatus status, Pageable pageable) {
        if (status != null) {
            return commentRepository.findByStatus(status, pageable).map(this::toCommentResponse);
        }
        return commentRepository.findAll(pageable).map(this::toCommentResponse);
    }

    @Override
    @Transactional
    public BlogCommentResponse updateCommentStatus(Long commentId, BlogCommentStatus status) {
        BlogComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
        comment.setStatus(status);
        comment.setUpdatedAt(LocalDateTime.now());
        BlogComment saved = commentRepository.save(comment);
        return toCommentResponse(saved);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment not found with id: " + commentId);
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public List<BlogCategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream().map(c -> {
            long count = postRepository.findByCategoryIgnoreCaseAndStatus(c.getName(), BlogPostStatus.PUBLISHED, PageRequest.of(0, 1)).getTotalElements();
            return BlogCategoryResponse.builder()
                    .id(c.getId())
                    .name(c.getName())
                    .slug(c.getSlug())
                    .description(c.getDescription())
                    .imageUrl(c.getImageUrl())
                    .postCount(count)
                    .createdAt(c.getCreatedAt())
                    .build();
        }).toList();
    }

    @Override
    @Transactional
    public BlogCategoryResponse createCategory(CreateBlogCategoryRequest request) {
        String slug = request.getSlug() != null && !request.getSlug().isBlank()
                ? request.getSlug().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9-]", "-")
                : request.getName().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9-]", "-");

        BlogCategory category = BlogCategory.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .createdAt(LocalDateTime.now())
                .build();

        BlogCategory saved = categoryRepository.save(category);
        return BlogCategoryResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .slug(saved.getSlug())
                .description(saved.getDescription())
                .imageUrl(saved.getImageUrl())
                .postCount(0L)
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public List<BlogTagResponse> getAllTags() {
        return tagRepository.findAll().stream().map(t -> BlogTagResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .slug(t.getSlug())
                .postCount(0L)
                .createdAt(t.getCreatedAt())
                .build()).toList();
    }

    @Override
    @Transactional
    public BlogTagResponse createTag(CreateBlogTagRequest request) {
        String slug = request.getSlug() != null && !request.getSlug().isBlank()
                ? request.getSlug().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9-]", "-")
                : request.getName().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9-]", "-");

        BlogTag tag = BlogTag.builder()
                .name(request.getName())
                .slug(slug)
                .createdAt(LocalDateTime.now())
                .build();

        BlogTag saved = tagRepository.save(tag);
        return BlogTagResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .slug(saved.getSlug())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    // Auto-publishing Cron Job
    @Scheduled(cron = "0 * * * * *") // Every minute
    @Transactional
    public void publishScheduledPosts() {
        LocalDateTime now = LocalDateTime.now();
        List<BlogPost> scheduled = postRepository.findByScheduledPublishAtBeforeAndStatus(now, BlogPostStatus.SCHEDULED);
        for (BlogPost p : scheduled) {
            log.info("Auto-publishing scheduled blog post: id={}, title='{}'", p.getId(), p.getTitle());
            p.setStatus(BlogPostStatus.PUBLISHED);
            p.setPublishedAt(now);
            p.setUpdatedAt(now);
            postRepository.save(p);
        }
    }

    // Dynamic XML Sitemap Generator
    @Override
    public String generateSitemapXml() {
        List<BlogPost> published = postRepository.findByStatus(BlogPostStatus.PUBLISHED, PageRequest.of(0, 1000)).getContent();
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        xml.append("  <url>\n");
        xml.append("    <loc>https://webliix.com/blog</loc>\n");
        xml.append("    <changefreq>daily</changefreq>\n");
        xml.append("    <priority>0.9</priority>\n");
        xml.append("  </url>\n");

        DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE;
        for (BlogPost p : published) {
            xml.append("  <url>\n");
            xml.append("    <loc>https://webliix.com/blog/").append(p.getSlug()).append("</loc>\n");
            if (p.getUpdatedAt() != null) {
                xml.append("    <lastmod>").append(p.getUpdatedAt().format(dtf)).append("</lastmod>\n");
            }
            xml.append("    <changefreq>weekly</changefreq>\n");
            xml.append("    <priority>0.8</priority>\n");
            xml.append("  </url>\n");
        }

        xml.append("</urlset>");
        return xml.toString();
    }

    private void autoRegisterCategoryAndTags(String categoryName, String tagsStr) {
        if (categoryName != null && !categoryName.isBlank()) {
            if (categoryRepository.findByNameIgnoreCase(categoryName.trim()).isEmpty()) {
                createCategory(CreateBlogCategoryRequest.builder().name(categoryName.trim()).build());
            }
        }
        if (tagsStr != null && !tagsStr.isBlank()) {
            String[] tags = tagsStr.split(",");
            for (String t : tags) {
                String clean = t.trim();
                if (!clean.isEmpty() && tagRepository.findByNameIgnoreCase(clean).isEmpty()) {
                    createTag(CreateBlogTagRequest.builder().name(clean).build());
                }
            }
        }
    }

    private String generateSlug(String customSlug, String title) {
        String base = (customSlug != null && !customSlug.isBlank()) ? customSlug : title;
        String slug = base.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        if (slug.isBlank()) {
            slug = "post-" + System.currentTimeMillis();
        }

        String finalSlug = slug;
        int count = 1;
        while (postRepository.findBySlug(finalSlug).isPresent()) {
            finalSlug = slug + "-" + count;
            count++;
        }
        return finalSlug;
    }

    private int calculateReadingTime(String content) {
        if (content == null || content.isBlank()) return 1;
        String[] words = content.split("\\s+");
        return Math.max(1, (int) Math.ceil(words.length / 200.0));
    }

    private Long resolveCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            return userRepository.findByEmail(userDetails.getUsername())
                    .map(user -> user.getId())
                    .orElse(null);
        }
        return null;
    }

    private BlogPostResponse toResponse(BlogPost post, boolean loadComments) {
        List<BlogCommentResponse> comments = loadComments
                ? getThreadedComments(post.getId(), true)
                : null;

        return BlogPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .summary(post.getSummary())
                .content(post.getContent())
                .coverImageUrl(post.getCoverImageUrl())
                .coverImageAlt(post.getCoverImageAlt())
                .coverImageCaption(post.getCoverImageCaption())
                .authorName(post.getAuthorName())
                .authorId(post.getAuthorId())
                .category(post.getCategory())
                .tags(post.getTags())
                .status(post.getStatus())
                .isFeatured(post.getIsFeatured())
                .viewsCount(post.getViewsCount() != null ? post.getViewsCount() : 0L)
                .likesCount(post.getLikesCount() != null ? post.getLikesCount() : 0L)
                .commentsCount(post.getCommentsCount() != null ? post.getCommentsCount() : 0L)
                .readingTimeMinutes(post.getReadingTimeMinutes())
                .seoTitle(post.getSeoTitle())
                .seoDescription(post.getSeoDescription())
                .canonicalUrl(post.getCanonicalUrl())
                .ogImageUrl(post.getOgImageUrl())
                .enableAds(post.getEnableAds() != null ? post.getEnableAds() : true)
                .adSenseClientId(post.getAdSenseClientId())
                .topAdSlotId(post.getTopAdSlotId())
                .inlineAdSlotId(post.getInlineAdSlotId())
                .bottomAdSlotId(post.getBottomAdSlotId())
                .adFormat(post.getAdFormat() != null ? post.getAdFormat() : "auto")
                .coverImageAlignment(post.getCoverImageAlignment() != null ? post.getCoverImageAlignment() : "center")
                .coverImageAspectRatio(post.getCoverImageAspectRatio() != null ? post.getCoverImageAspectRatio() : "16:9")
                .scheduledPublishAt(post.getScheduledPublishAt())
                .publishedAt(post.getPublishedAt())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .comments(comments)
                .build();
    }

    private BlogCommentResponse toCommentResponse(BlogComment comment) {
        return BlogCommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .parentId(comment.getParentId())
                .authorName(comment.getAuthorName())
                .authorEmail(comment.getAuthorEmail())
                .authorWebsite(comment.getAuthorWebsite())
                .content(comment.getContent())
                .status(comment.getStatus())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .replies(new ArrayList<>())
                .build();
    }
}
