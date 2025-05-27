package com.example.blog.repository;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.example.blog.model.User;

@Repository
@RequiredArgsConstructor
public class JdbcNativeUserRepository implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<User> findAll() {
        // Выполняем запрос с помощью JdbcTemplate
        // Преобразовываем ответ с помощью RowMapper
        return jdbcTemplate.query(
                "select id, first_name, last_name, age, active from users",
                (rs, rowNum) -> new User(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getInt("age"),
                        rs.getBoolean("active")
                ));
    }

    @Override
    public void save(User user) {
        // Формируем insert-запрос с параметрами
        jdbcTemplate.update("insert into users(first_name, last_name, age, active) values(?, ?, ?, ?)",
                user.getFirstName(), user.getLastName(), user.getAge(), user.isActive());
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("delete from users where id = ?", id);
    }

}
