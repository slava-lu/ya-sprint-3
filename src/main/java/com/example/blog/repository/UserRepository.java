package com.example.blog.repository;

import com.example.blog.model.User;

import java.util.List;

public interface UserRepository {
    List<User> findAll();

    void save(User user);

    void deleteById(Long id);
}