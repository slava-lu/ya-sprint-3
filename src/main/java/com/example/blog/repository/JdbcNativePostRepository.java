package com.example.blog.repository;

import com.example.blog.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcNativePostRepository implements PostRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcNativePostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Page<Post> findAll(String search, int pageNumber, int pageSize) {
        // Base SELECT
        String baseSql =
                "FROM posts p";
        List<Object> params = new ArrayList<>();

        // If filtering by tag name (search), join tags
        if (search != null && !search.isBlank()) {
            baseSql +=
                    " JOIN post_tags pt ON p.id = pt.post_id" +
                            " JOIN tags t ON pt.tag_id = t.id";
            baseSql += " WHERE t.name = ?";
            params.add(search);
        }

        // Count total
        String countSql = "SELECT COUNT(DISTINCT p.id) " + baseSql;
        Integer total = jdbcTemplate.queryForObject(countSql, params.toArray(), Integer.class);

        // Fetch page content
        String selectSql =
                "SELECT DISTINCT p.id, p.title, p.image_url, p.content, p.like_count, p.created_at, p.updated_at "
                        + baseSql
                        + " ORDER BY p.created_at DESC"
                        + " LIMIT ? OFFSET ?";
        params.add(pageSize);
        params.add(pageNumber * pageSize);

        List<Post> content = jdbcTemplate.query(
                selectSql,
                params.toArray(),
                (rs, rowNum) -> new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("image_url"),
                        rs.getString("content"),
                        rs.getInt("like_count"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime(),
                        new ArrayList<>(),
                        new ArrayList<>()
                )
        );

        return new PageImpl<>(
                content,
                PageRequest.of(pageNumber, pageSize),
                (total != null ? total : 0)
        );
    }

    @Override
    public Post findById(Long id) {
        return jdbcTemplate.queryForObject(
                "SELECT id, title, image_url, content, like_count, created_at, updated_at FROM posts WHERE id = ?",
                new Object[]{id},
                (rs, rn) -> new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("image_url"),
                        rs.getString("content"),
                        rs.getInt("like_count"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime(),
                        null,
                        null
                )
        );
    }

    @Override
    public void save(Post post) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO posts(title, image_url, content, like_count, created_at, updated_at) " +
                            "VALUES(?, ?, ?, ?, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP())",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getImageUrl());
            ps.setString(3, post.getContent());
            ps.setInt(4, post.getLikesCount());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) {
            post.setId(key.longValue());
        }
    }

    @Override
    public void update(Post post) {
        jdbcTemplate.update(
                "UPDATE posts SET title = ?, image_url = ?, content = ?, like_count = ?, updated_at = CURRENT_TIMESTAMP() WHERE id = ?",
                post.getTitle(),
                post.getImageUrl(),
                post.getContent(),
                post.getLikesCount(),
                post.getId()
        );
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM posts WHERE id = ?", id);
    }
}
