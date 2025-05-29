package com.example.blog.service;

import com.example.blog.model.Post;
import com.example.blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

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
    public void incrementLikes(Long id) {
        Post p = findById(id);
        p.setLikesCount(p.getLikesCount() + 1);
        update(p);
    }

    public void decrementLikes(Long id) {
        Post p = findById(id);
        p.setLikesCount(p.getLikesCount() - 1);
        update(p);
    }

    public void saveComment(Long postId, String text) {
        postRepository.saveComment(postId, text);
    }

    public void updateComment(Long commentId, String text) {
        postRepository.updateComment(commentId, text);
    }

    public void deleteComment(Long commentId) {
        postRepository.deleteComment(commentId);
    }
}
