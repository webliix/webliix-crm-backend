package com.webliix.blog.dto;

import com.webliix.blog.enums.BlogCommentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommentStatusRequest {

    @NotNull(message = "Status is required")
    private BlogCommentStatus status;
}
