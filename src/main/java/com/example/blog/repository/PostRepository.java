package com.example.blog.repository;

import com.example.blog.model.Post;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PostRepository {
    Page<Post> findAll(String search, int pageNumber, int pageSize);
    Post findById(Long id);
    void save(Post post);
    void update(Post post);
    void deleteById(Long id);
}
