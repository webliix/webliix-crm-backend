package com.webliix.careers.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobOpeningDto {
    private Long id;
    private String title;
    private String department;
    private String type;
    private String location;
    private String experience;
    private String description;
    private String requirements;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
