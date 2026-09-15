package com.webliix.blog.service;

import com.webliix.blog.dto.BlogMediaResponse;
import com.webliix.blog.enums.BlogResourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface BlogMediaService {

    BlogMediaResponse uploadMedia(MultipartFile file, String folder, String altText, String caption, Long postId) throws Exception;

    void deleteMedia(Long id) throws Exception;

    Page<BlogMediaResponse> getAllMedia(BlogResourceType type, Pageable pageable);
}
