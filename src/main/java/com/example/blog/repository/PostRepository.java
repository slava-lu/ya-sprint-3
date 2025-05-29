package com.example.blog.repository;

import com.example.blog.model.Post;
import org.springframework.data.domain.Page;


public interface PostRepository {
    Page<Post> findAll(String search, int pageNumber, int pageSize);
    Post findById(Long id);
    void save(Post post);
    void update(Post post);
    void deleteById(Long id);

    void saveComment(Long postId, String text);
    void updateComment(Long commentId, String text);
    void deleteComment(Long commentId);
}
