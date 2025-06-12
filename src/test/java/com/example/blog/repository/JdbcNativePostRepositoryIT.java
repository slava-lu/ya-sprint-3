package com.example.blog.repository;

import com.example.blog.model.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class JdbcNativePostRepositoryIT {

    @Autowired
    private JdbcNativePostRepository postRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Post post1;
    private Post post2;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM post_tags");
        jdbcTemplate.update("DELETE FROM comments");
        jdbcTemplate.update("DELETE FROM posts");

        post1 = new Post();
        post1.setTitle("Post 1");
        post1.setImageUrl("http://image1.com");
        post1.setContent("Content for post 1");
        post1.setLikesCount(0);

        post2 = new Post();
        post2.setTitle("Post 2");
        post2.setImageUrl("http://image2.com");
        post2.setContent("Content for post 2");
        post2.setLikesCount(5);
    }

    @Test
    void savePost_shouldPersistData() {
        postRepository.save(post1);
        assertThat(post1.getId()).isNotNull();

        Post fetched = postRepository.findById(post1.getId());
        assertThat(fetched.getTitle()).isEqualTo("Post 1");
    }

    @Test
    void findAll_shouldReturnPagedPosts() {
        postRepository.save(post1);
        postRepository.save(post2);

        var page = postRepository.findAll(null, 0, 10);
        List<Post> posts = page.getContent();

        assertThat(posts).hasSize(2);
        assertThat(posts).extracting(Post::getTitle)
                .containsExactlyInAnyOrder("Post 1", "Post 2");
    }
}
