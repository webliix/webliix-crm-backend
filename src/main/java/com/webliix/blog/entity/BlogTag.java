package com.webliix.blog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "blog_tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
