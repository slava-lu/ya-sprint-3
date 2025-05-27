package com.example.blog.repository;

import com.example.blog.model.Post;
import com.example.blog.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class JdbcNativePostRepository implements PostRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcNativePostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Page<Post> findAll(String search, int pageNumber, int pageSize) {
        String baseSql = " FROM posts p";
        List<Object> params = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            baseSql +=
                    " JOIN post_tags pt ON p.id = pt.post_id" +
                            " JOIN tags t ON pt.tag_id = t.id" +
                            " WHERE t.name = ?";
            params.add(search);
        }

        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT p.id)" + baseSql,
                params.toArray(),
                Integer.class
        );

        String selectSql =
                "SELECT DISTINCT "
                        + "  p.id, p.title, p.image_url, p.content, p.like_count, "
                        + "  p.created_at, p.updated_at, "
                        + "  (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id) AS comments_count"
                        + baseSql
                        + " ORDER BY p.created_at DESC"
                        + " LIMIT ? OFFSET ?";

        params.add(pageSize);
        params.add(pageNumber * pageSize);

        List<Post> content = jdbcTemplate.query(
                selectSql,
                params.toArray(),
                (rs, rowNum) -> {
                    Post p = new Post(
                            rs.getLong("id"),
                            rs.getString("title"),
                            rs.getString("image_url"),
                            rs.getString("content"),
                            rs.getInt("like_count"),
                            rs.getInt("comments_count"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getTimestamp("updated_at").toLocalDateTime(),
                            new ArrayList<>(),  // tags to be filled
                            new ArrayList<>()   // comments not loaded here
                    );
                    return p;
                }
        );

        if (!content.isEmpty()) {
            List<Long> postIds = content.stream()
                    .map(Post::getId)
                    .collect(Collectors.toList());
            String inSql = postIds.stream().map(id -> "?").collect(Collectors.joining(","));
            String tagSql =
                    "SELECT pt.post_id, t.id AS tag_id, t.name " +
                            "  FROM post_tags pt " +
                            "  JOIN tags t ON pt.tag_id = t.id " +
                            " WHERE pt.post_id IN (" + inSql + ")";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(tagSql, postIds.toArray());

            Map<Long, List<Tag>> tagsByPost = new HashMap<>();
            for (Map<String, Object> row : rows) {
                Long pid    = ((Number) row.get("post_id")).longValue();
                Long tagId  = ((Number) row.get("tag_id")).longValue();
                String name = (String) row.get("name");
                Tag tag = new Tag(tagId, name);
                tagsByPost.computeIfAbsent(pid, k -> new ArrayList<>()).add(tag);
            }

            content.forEach(p ->
                    p.setTags(tagsByPost.getOrDefault(p.getId(), Collections.emptyList()))
            );
        }

        return new PageImpl<>(
                content,
                PageRequest.of(pageNumber, pageSize),
                total == null ? 0 : total
        );
    }

    @Override
    public Post findById(Long id) {
        Post post = jdbcTemplate.queryForObject(
                "SELECT id, title, image_url, content, like_count, created_at, updated_at " +
                        "  FROM posts WHERE id = ?",
                new Object[]{id},
                (rs, rn) -> new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("image_url"),
                        rs.getString("content"),
                        rs.getInt("like_count"),
                        0,
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime(),
                        new ArrayList<>(),
                        new ArrayList<>()
                )
        );

        if (post != null) {
            List<Tag> tags = jdbcTemplate.query(
                    "SELECT t.id, t.name " +
                            "  FROM post_tags pt " +
                            "  JOIN tags t ON pt.tag_id = t.id " +
                            " WHERE pt.post_id = ?",
                    new Object[]{id},
                    (rs, rn) -> new Tag(rs.getLong("id"), rs.getString("name"))
            );
            post.setTags(tags);
        }

        return post;
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

        if (keyHolder.getKey() != null) {
            post.setId(keyHolder.getKey().longValue());
        }
    }

    @Override
    public void update(Post post) {
        jdbcTemplate.update(
                "UPDATE posts " +
                        "   SET title = ?, image_url = ?, content = ?, like_count = ?, updated_at = CURRENT_TIMESTAMP() " +
                        " WHERE id = ?",
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
