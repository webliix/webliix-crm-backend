package com.webliix.blog.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.webliix.blog.dto.BlogMediaResponse;
import com.webliix.blog.entity.BlogMedia;
import com.webliix.blog.enums.BlogResourceType;
import com.webliix.blog.repository.BlogMediaRepository;
import com.webliix.blog.service.BlogMediaService;
import com.webliix.security.repository.UserRepository;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import com.webliix.storage.dto.UploadFileResponse;
import com.webliix.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryMediaServiceImpl implements BlogMediaService {

    private final Cloudinary cloudinary;
    private final BlogMediaRepository mediaRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/webp", "image/gif", "image/svg+xml", "image/avif"
    );

    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList(
            "video/mp4", "video/webm", "video/ogg", "video/quicktime"
    );

    private static final long MAX_IMAGE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final long MAX_VIDEO_SIZE = 200 * 1024 * 1024; // 200MB

    @Override
    @Transactional
    public BlogMediaResponse uploadMedia(MultipartFile file, String folder, String altText, String caption, Long postId) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String contentType = file.getContentType();
        boolean isVideo = contentType != null && contentType.startsWith("video");
        boolean isImage = contentType != null && contentType.startsWith("image");

        if (!isImage && !isVideo) {
            throw new IllegalArgumentException("Unsupported file format: " + contentType);
        }

        if (isImage) {
            if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
                throw new IllegalArgumentException("Invalid image type: " + contentType);
            }
            if (file.getSize() > MAX_IMAGE_SIZE) {
                throw new IllegalArgumentException("Image file exceeds max limit of 50MB");
            }
        }

        if (isVideo) {
            if (!ALLOWED_VIDEO_TYPES.contains(contentType)) {
                throw new IllegalArgumentException("Invalid video type: " + contentType);
            }
            if (file.getSize() > MAX_VIDEO_SIZE) {
                throw new IllegalArgumentException("Video file exceeds max limit of 200MB");
            }
        }

        String targetFolder = "webliix/blog/" + (folder != null && !folder.isBlank() ? folder : (isVideo ? "videos" : "content"));
        String resourceTypeStr = isVideo ? "video" : "image";

        String publicId;
        String secureUrl;
        String format = contentType != null && contentType.contains("/") ? contentType.substring(contentType.indexOf('/') + 1) : "bin";
        Integer width = null;
        Integer height = null;
        Long bytes = file.getSize();

        // Attempt Cloudinary upload
        try {
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", targetFolder,
                    "resource_type", resourceTypeStr,
                    "use_filename", true,
                    "unique_filename", true,
                    "overwrite", false
            );

            log.info("Uploading media to Cloudinary: folder={}, resourceType={}, size={}", targetFolder, resourceTypeStr, file.getSize());
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            publicId = (String) uploadResult.get("public_id");
            secureUrl = (String) uploadResult.get("secure_url");
            if (uploadResult.get("format") != null) {
                format = (String) uploadResult.get("format");
            }
            if (uploadResult.get("width") != null) {
                width = ((Number) uploadResult.get("width")).intValue();
            }
            if (uploadResult.get("height") != null) {
                height = ((Number) uploadResult.get("height")).intValue();
            }
            if (uploadResult.get("bytes") != null) {
                bytes = ((Number) uploadResult.get("bytes")).longValue();
            }
            log.info("Cloudinary upload successful: {}", secureUrl);
        } catch (Exception ex) {
            log.warn("Cloudinary upload failed ({}). Falling back to local storage provider...", ex.getMessage());
            UploadFileResponse localRes = storageService.uploadFile(file, 1L, "BLOG_" + (isVideo ? "VIDEOS" : "COVERS"), postId);
            publicId = "local-" + localRes.getFileId();
            secureUrl = localRes.getUrl();
        }

        Long currentUserId = resolveCurrentUserId();

        BlogMedia media = BlogMedia.builder()
                .publicId(publicId)
                .secureUrl(secureUrl)
                .resourceType(isVideo ? BlogResourceType.VIDEO : BlogResourceType.IMAGE)
                .format(format)
                .width(width)
                .height(height)
                .fileSize(bytes)
                .altText(altText != null ? altText : file.getOriginalFilename())
                .caption(caption)
                .folder(targetFolder)
                .uploadedBy(currentUserId)
                .postId(postId)
                .createdAt(LocalDateTime.now())
                .build();

        BlogMedia saved = mediaRepository.save(media);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteMedia(Long id) throws Exception {
        BlogMedia media = mediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + id));

        if (media.getPublicId() != null && !media.getPublicId().startsWith("local-")) {
            try {
                String resourceTypeStr = media.getResourceType() == BlogResourceType.VIDEO ? "video" : "image";
                cloudinary.uploader().destroy(media.getPublicId(), ObjectUtils.asMap("resource_type", resourceTypeStr));
            } catch (Exception ex) {
                log.warn("Cloudinary destroy warning for {}: {}", media.getPublicId(), ex.getMessage());
            }
        }

        mediaRepository.delete(media);
    }

    @Override
    public Page<BlogMediaResponse> getAllMedia(BlogResourceType type, Pageable pageable) {
        if (type != null) {
            return mediaRepository.findByResourceType(type, pageable).map(this::toResponse);
        }
        return mediaRepository.findAll(pageable).map(this::toResponse);
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

    private BlogMediaResponse toResponse(BlogMedia media) {
        return BlogMediaResponse.builder()
                .id(media.getId())
                .publicId(media.getPublicId())
                .secureUrl(media.getSecureUrl())
                .resourceType(media.getResourceType())
                .format(media.getFormat())
                .width(media.getWidth())
                .height(media.getHeight())
                .fileSize(media.getFileSize())
                .altText(media.getAltText())
                .caption(media.getCaption())
                .folder(media.getFolder())
                .postId(media.getPostId())
                .createdAt(media.getCreatedAt())
                .build();
    }
}
