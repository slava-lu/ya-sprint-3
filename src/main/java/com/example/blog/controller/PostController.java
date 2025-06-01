package com.example.blog.controller;

import com.example.blog.model.Post;
import com.example.blog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public String posts(
            @RequestParam(value = "search",     required = false) String search,
            @RequestParam(value = "pageSize",   defaultValue = "10")  int pageSize,
            @RequestParam(value = "pageNumber", defaultValue = "0")   int pageNumber,
            Model model
    ) {
        if (search != null) {
            search = search.trim();
        }
        Page<Post> paging = postService.findAll(search, pageNumber, pageSize);
        model.addAttribute("posts",  paging.getContent());
        model.addAttribute("paging", paging);
        model.addAttribute("search", search);
        return "posts";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("post", new Post());
        return "add-post";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Post post = postService.findById(id);
        model.addAttribute("post", post);
        return "add-post";
    }

    @PostMapping("/{id}")
    public String updatePost(
            @PathVariable("id") Long id,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "tags", required = false, defaultValue = "") String tagsLine,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) {
        Post post = new Post();
        post.setId(id);
        post.setTitle(title);
        post.setContent(content);
        post.setLikesCount(0);
        postService.updatePost(post, tagsLine, imageFile);
        return "redirect:/posts/" + id;
    }

    @PostMapping
    public String createPost(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "tags", required = false, defaultValue = "") String tagsLine,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) {
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setLikesCount(0);
        postService.createPost(post, tagsLine, imageFile);
        return "redirect:/posts";
    }

    @PostMapping(value = "/{id}/delete")
    public String delete(@PathVariable("id") Long id) {
        postService.deleteById(id);
        return "redirect:/posts";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable("id") Long id, Model model) {
        Post post = postService.findById(id);
        model.addAttribute("post", post);
        return "post";
    }
    @PostMapping("/{id}/like")
    public String like(
            @PathVariable("id") Long id,
            @RequestParam("like") boolean like
    ) {
        if (like) {
            postService.incrementLikes(id);
        } else {
            postService.decrementLikes(id);
        }
        return "redirect:/posts/" + id;
    }

    @PostMapping("/{id}/comments")
    public String addComment(@PathVariable("id")Long id, @RequestParam("text") String text) {
        postService.saveComment(id, text);
        return "redirect:/posts/" + id;
    }

    @PostMapping("/{id}/comments/{commentId}")
    public String editComment(@PathVariable("id") Long id,
                              @PathVariable("commentId") Long commentId,
                              @RequestParam("text") String text) {
        postService.updateComment(commentId, text);
        return "redirect:/posts/" + id;
    }

    @PostMapping("/{id}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable("id") Long id,
                                @PathVariable("commentId") Long commentId) {
        postService.deleteComment(commentId);
        return "redirect:/posts/" + id;
    }

}
