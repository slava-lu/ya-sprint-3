package com.example.blog.repository;

import com.example.blog.model.Comment;
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
            params.add(search.trim());
        }

        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT p.id)" + baseSql,
                params.toArray(),
                Integer.class
        );

        String selectSql =
                "SELECT DISTINCT "
                        + "  p.id, "
                        + "  p.title, "
                        + "  p.image_url, "
                        + "  p.content, "
                        + "  p.like_count, "
                        + "  (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id) AS comments_count"
                        + baseSql
                        + " ORDER BY p.id DESC"
                        + " LIMIT ? OFFSET ?";

        params.add(pageSize);
        params.add(pageNumber * pageSize);

        List<Post> content = jdbcTemplate.query(
                selectSql,
                params.toArray(),
                (rs, rowNum) -> {
                    Post p = new Post();
                    p.setId(rs.getLong("id"));
                    p.setTitle(rs.getString("title"));
                    p.setImageUrl(rs.getString("image_url"));
                    p.setContent(rs.getString("content"));
                    p.setLikesCount(rs.getInt("like_count"));
                    p.setCommentsCount(rs.getInt("comments_count"));
                    p.setTags(new ArrayList<>());
                    p.setComments(new ArrayList<>());
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
                            "FROM post_tags pt " +
                            "JOIN tags t ON pt.tag_id = t.id " +
                            "WHERE pt.post_id IN (" + inSql + ")";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(tagSql, postIds.toArray());

            Map<Long, List<Tag>> tagsByPost = new HashMap<>();
            for (Map<String, Object> row : rows) {
                Long pid = ((Number) row.get("post_id")).longValue();
                Long tagId = ((Number) row.get("tag_id")).longValue();
                String name = (String) row.get("name");
                tagsByPost.computeIfAbsent(pid, k -> new ArrayList<>())
                        .add(new Tag(tagId, name));
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
                "SELECT id, title, image_url, content, like_count " +
                        "FROM posts WHERE id = ?",
                new Object[]{id},
                (rs, rn) -> {
                    Post p = new Post();
                    p.setId(rs.getLong("id"));
                    p.setTitle(rs.getString("title"));
                    p.setImageUrl(rs.getString("image_url"));
                    p.setContent(rs.getString("content"));
                    p.setLikesCount(rs.getInt("like_count"));
                    return p;
                }
        );

        List<Comment> comments = jdbcTemplate.query(
                "SELECT id, post_id, content, " +
                        "FROM comments WHERE post_id = ?",
                new Object[]{id},
                (rs, rn) -> {
                    Comment c = new Comment();
                    c.setId(rs.getLong("id"));
                    c.setPostId(rs.getLong("post_id"));
                    c.setContent(rs.getString("content"));
                    return c;
                }
        );
        post.setComments(comments);

        List<Tag> tags = jdbcTemplate.query(
                "SELECT t.id, t.name " +
                        "FROM post_tags pt " +
                        "JOIN tags t ON pt.tag_id = t.id " +
                        "WHERE pt.post_id = ?",
                new Object[]{id},
                (rs, rn) -> new Tag(rs.getLong("id"), rs.getString("name"))
        );
        post.setTags(tags);

        post.setTextParts(
                Arrays.asList(post.getContent().split("\\r?\\n\\r?\\n"))
        );

        return post;
    }

    @Override
    public void save(Post post) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO posts(title, image_url, content, like_count) VALUES(?, ?, ?, ?)",
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
    public void clearTagsForPost(Long postId) {
        jdbcTemplate.update("DELETE FROM post_tags WHERE post_id = ?", postId);
    }

    @Override
    public void update(Post post) {
        jdbcTemplate.update(
                "UPDATE posts SET title = ?, image_url = ?, content = ?, like_count = ? WHERE id = ?",
                post.getTitle(), post.getImageUrl(), post.getContent(), post.getLikesCount(), post.getId()
        );
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM posts WHERE id = ?", id);
    }

    @Override
    public void saveComment(Long postId, String text) {
        jdbcTemplate.update(
                "INSERT INTO comments(post_id, content) VALUES (?, ?)",
                postId, text
        );
    }

    @Override
    public void updateComment(Long commentId, String text) {
        jdbcTemplate.update(
                "UPDATE comments SET content = ?, updated_at = CURRENT_TIMESTAMP() WHERE id = ?",
                text, commentId
        );
    }

    @Override
    public void deleteComment(Long commentId) {
        jdbcTemplate.update("DELETE FROM comments WHERE id = ?", commentId);
    }

    @Override
    public Optional<Long> findTagIdByName(String name) {
        return jdbcTemplate.query(
                "SELECT id FROM tags WHERE name = ?",
                new Object[]{name},
                (rs, rn) -> rs.getLong("id")
        ).stream().findFirst();
    }

    @Override
    public Long saveTag(String name) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO tags(name) VALUES(?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, name);
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    @Override
    public void savePostTag(Long postId, Long tagId) {
        jdbcTemplate.update(
                "INSERT INTO post_tags(post_id, tag_id) VALUES(?, ?)",
                postId, tagId
        );
    }
}
