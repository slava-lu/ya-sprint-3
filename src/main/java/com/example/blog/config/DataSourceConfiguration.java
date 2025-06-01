package com.example.blog.config;

import org.h2.Driver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Configuration
public class DataSourceConfiguration {


    @Value("${app.upload.dir}")
    private String uploadDir;

    @Bean
    public DataSource dataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password
    ) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(Driver.class.getName());
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @EventListener
    public void populate(ContextRefreshedEvent event) {
        DataSource dataSource = event.getApplicationContext().getBean(DataSource.class);

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("schema.sql"));
        populator.execute(dataSource);

        try {
            Path targetDir = Paths.get(uploadDir);
            Files.createDirectories(targetDir);

            List<String> imageNames = List.of("1.jpg", "2.jpg", "3.jpg");
            for (String imageName : imageNames) {
                ClassPathResource resource = new ClassPathResource("init-images/" + imageName);
                Path targetFile = targetDir.resolve(imageName);
                Files.copy(resource.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
            }

            System.out.println("✅ Initial images copied to: " + targetDir.toAbsolutePath());

        } catch (IOException e) {
            throw new RuntimeException("Failed to copy initial images", e);
        }
    }

}