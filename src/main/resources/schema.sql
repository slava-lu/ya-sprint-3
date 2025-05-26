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

