package com.webliix.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NavigationItemDto {
    private String key;
    private String label;
    private String path;
    private String icon;
    private String module;
    private String requiredPermission;
    private Integer sortOrder;
    private String parentKey;

    @Builder.Default
    private List<NavigationItemDto> children = new ArrayList<>();
}
