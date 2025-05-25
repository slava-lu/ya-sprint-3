package com.example.blog.controller;

import com.example.blog.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


import java.util.Arrays;
import java.util.List;


@Controller
@RequestMapping("/users")
public class HomeController {

    @GetMapping
    public String users(Model model) {
        List<User> users = Arrays.asList(
                new User(1L, "Иван", "Иванов", 300, true),
                new User(2L, "Пётр", "Петров", 250, false),
                new User(3L, "Мария", "Сидорова", 28, true)
        );
        model.addAttribute("users", users);
        return "users";
    }
}
