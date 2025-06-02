package com.example.blog.service;

import com.example.blog.model.Post;
import com.example.blog.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    private Post post;

    @BeforeEach
    void setUp() {
        post = new Post();
        post.setId(1L);
        post.setTitle("Test Post");
        post.setContent("Content");
        post.setLikesCount(0);
    }

    @Test
    void incrementLikes_shouldIncreaseLikesByOne() {
        when(postRepository.findById(1L)).thenReturn(post);

        postService.incrementLikes(1L);

        assertThat(post.getLikesCount()).isEqualTo(1);
        verify(postRepository).update(post);
    }

    @Test
    void decrementLikes_shouldDecreaseLikesByOne() {
        post.setLikesCount(2);
        when(postRepository.findById(1L)).thenReturn(post);

        postService.decrementLikes(1L);

        assertThat(post.getLikesCount()).isEqualTo(1);
        verify(postRepository).update(post);
    }

    @Test
    void saveComment_shouldCallRepositoryWithCorrectArguments() {
        postService.saveComment(1L, "Nice post!");

        verify(postRepository).saveComment(1L, "Nice post!");
    }

    @Test
    void deleteComment_shouldCallRepository() {
        postService.deleteComment(99L);

        verify(postRepository).deleteComment(99L);
    }
}
