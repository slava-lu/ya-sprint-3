package com.example.blog.model;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Likes {
    private Long id;
    private Long postId;
    private LocalDateTime createdAt;
}
