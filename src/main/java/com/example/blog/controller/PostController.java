package com.example.blog.controller;

import com.example.blog.model.Post;
import com.example.blog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public String addPost(@ModelAttribute Post post) {
        postService.save(post);
        return "redirect:/posts";
    }

    @PostMapping(value = "/{id}", params = "_method=delete")
    public String delete(@PathVariable Long id) {
        postService.deleteById(id);
        return "redirect:/posts";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        Post post = postService.findById(id);
        model.addAttribute("post", post);
        return "post";
    }
}
