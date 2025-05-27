package com.example.blog.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Post {
    private Long id;
    private String title;
    private String imageUrl;
    private String content;
    private int likesCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<Tag> tags;
    private List<Comment> comments;

    public String getTextPreview() {
        return content.length() <= 300
                ? content
                : content.substring(0,300) + "…";
    }
}