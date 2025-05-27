package com.example.blog.service;

import com.example.blog.model.Post;
import com.example.blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public Page<Post> findAll(String search, int pageNumber, int pageSize) {
        return postRepository.findAll(search, pageNumber, pageSize);
    }

    public Post findById(Long id) {
        return postRepository.findById(id);
    }

    public void save(Post post) {
        postRepository.save(post);
    }

    public void update(Post post) {
        postRepository.update(post);
    }

    public void deleteById(Long id) {
        postRepository.deleteById(id);
    }
}
