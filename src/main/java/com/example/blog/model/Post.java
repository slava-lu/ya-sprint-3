package com.example.blog.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private int commentsCount;
    private List<String> textParts = new ArrayList<>();

    private List<Tag> tags;
    private List<Comment> comments;

    public String getTextPreview() {
        return content.length() <= 300
                ? content
                : content.substring(0,300) + "…";
    }

    public String getTagsAsText() {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return tags.stream()
                .map(Tag::getName)
                .collect(Collectors.joining(" "));
    }

    public String getText() {
        return content == null ? "" : content;
    }
}