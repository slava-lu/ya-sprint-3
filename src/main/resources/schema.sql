DROP TABLE IF EXISTS post_tags;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS tags;
DROP TABLE IF EXISTS posts;

-- ----------------------------
-- Таблица posts — посты блога
-- ----------------------------
CREATE TABLE posts (
                       id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                       title       VARCHAR(255) NOT NULL,
                       image_url   VARCHAR(500),
                       content     CLOB       NOT NULL,
                       like_count  INT        NOT NULL DEFAULT 0,
                       created_at  TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP(),
                       updated_at  TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP()
);

-- --------------------------------
-- Таблица tags — словарь тегов
-- --------------------------------
CREATE TABLE tags (
                      id    BIGINT AUTO_INCREMENT PRIMARY KEY,
                      name  VARCHAR(100) NOT NULL UNIQUE
);

-- -----------------------------------------
-- Связующая таблица post_tags
-- -----------------------------------------
CREATE TABLE post_tags (
                           post_id BIGINT NOT NULL,
                           tag_id  BIGINT NOT NULL,
                           PRIMARY KEY (post_id, tag_id),
                           CONSTRAINT fk_pt_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
                           CONSTRAINT fk_pt_tag  FOREIGN KEY (tag_id)  REFERENCES tags(id)  ON DELETE CASCADE
);

-- --------------------------------------
-- Таблица comments — комментарии к постам
-- --------------------------------------
CREATE TABLE comments (
                          id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                          post_id    BIGINT NOT NULL,
                          parent_id  BIGINT,
                          content    CLOB    NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
                          CONSTRAINT fk_c_post   FOREIGN KEY (post_id)   REFERENCES posts(id)    ON DELETE CASCADE,
                          CONSTRAINT fk_c_parent FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE
);

-- ----------------------------
-- Наполнение тестовыми данными
-- ----------------------------

-- Теги (6 штук, по 2 на каждый из 3 постов)
INSERT INTO tags (name) VALUES
                            ('Java'),
                            ('Spring'),
                            ('Thymeleaf'),
                            ('H2'),
                            ('JDBC'),
                            ('Web');

-- Посты (3 записи, по 1 лайку каждая)
INSERT INTO posts (title, image_url, content, like_count) VALUES
                                                              ('Первый пост', '/images/1.jpg', 'Это содержимое первого поста.', 1),
                                                              ('Второй пост', '/images/2.jpg', 'Это содержимое второго поста.', 1),
                                                              ('Третий пост', '/images/3.jpg', 'Это содержимое третьего поста.', 1);

-- Связь постов и тегов (по 2 тега на пост)
INSERT INTO post_tags (post_id, tag_id) VALUES
                                            (1, 1), -- Пост 1 ↔ Java
                                            (1, 2), -- Пост 1 ↔ Spring
                                            (2, 3), -- Пост 2 ↔ Thymeleaf
                                            (2, 4), -- Пост 2 ↔ H2
                                            (3, 5), -- Пост 3 ↔ JDBC
                                            (3, 6); -- Пост 3 ↔ Web

-- Комментарии (по 2 на каждый пост)
INSERT INTO comments (post_id, content) VALUES
                                            (1, 'Первый комментарий к первому посту.'),
                                            (1, 'Второй комментарий к первому посту.'),
                                            (2, 'Первый комментарий ко второму посту.'),
                                            (2, 'Второй комментарий ко второму посту.'),
                                            (3, 'Первый комментарий к третьему посту.'),
                                            (3, 'Второй комментарий к третьему посту.');
