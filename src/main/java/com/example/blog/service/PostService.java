package com.example.blog.service;

import com.example.blog.model.Post;
import com.example.blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    public void createPost(Post post, String tagsLine, MultipartFile imageFile) {
        if (tagsLine == null) {
            tagsLine = "";
        }

        postRepository.save(post);

        // 2) save uploaded image
        if (imageFile != null && !imageFile.isEmpty()) {
            String original = imageFile.getOriginalFilename();
            String ext = (original != null && original.lastIndexOf('.') >= 0)
                    ? original.substring(original.lastIndexOf('.'))
                    : ".jpg";
            try {
                Path dir = Paths.get("target/classes/static/images");
                Files.createDirectories(dir);
                Path file = dir.resolve(post.getId() + ext);
                Files.copy(imageFile.getInputStream(), file, StandardCopyOption.REPLACE_EXISTING);
                post.setImageUrl("/images/" + file.getFileName());
                postRepository.update(post);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store image", e);
            }
        }

        // 3) parse and persist tags
        List<String> tags = Arrays.stream(tagsLine.split("[,\\s]+"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .collect(Collectors.toList());

        for (String name : tags) {
            Long tagId = postRepository
                    .findTagIdByName(name)
                    .orElseGet(() -> postRepository.saveTag(name));
            postRepository.savePostTag(post.getId(), tagId);
        }
    }
}
