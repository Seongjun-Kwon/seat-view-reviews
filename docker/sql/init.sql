CREATE TABLE IF NOT EXISTS `member`
(
    `id`               BIGINT AUTO_INCREMENT PRIMARY KEY,
    `login_email`      VARCHAR(50) NOT NULL UNIQUE,
    `password`         VARCHAR(64) NOT NULL,
    `nickname`         VARCHAR(10) NOT NULL UNIQUE,
    `point`            INT         NOT NULL,
    `member_authority` VARCHAR(10) NOT NULL,
    `created_at`       TIMESTAMP   NOT NULL,
    `last_updated_at`  TIMESTAMP   NOT NULL,
    `deleted_at`       TIMESTAMP   NULL
);

CREATE TABLE IF NOT EXISTS `stadium`
(
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name`            VARCHAR(50)  NOT NULL,
    `address`         VARCHAR(255) NOT NULL,
    `home_team`       VARCHAR(50)  NOT NULL,
    `created_at`      TIMESTAMP    NOT NULL,
    `last_updated_at` TIMESTAMP    NOT NULL,
    `deleted_at`      TIMESTAMP    NULL
);

CREATE TABLE IF NOT EXISTS `seat_grade`
(
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `stadium_id`      BIGINT       NOT NULL,
    `name`            VARCHAR(30)  NOT NULL,
    `price_info`      VARCHAR(100) NOT NULL,
    `created_at`      TIMESTAMP    NOT NULL,
    `last_updated_at` TIMESTAMP    NOT NULL,
    `deleted_at`      TIMESTAMP    NULL
);

CREATE TABLE IF NOT EXISTS `seat_section`
(
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `stadium_id`      BIGINT      NOT NULL,
    `seat_grade_id`   BIGINT      NOT NULL,
    `name`            VARCHAR(50) NOT NULL,
    `created_at`      TIMESTAMP   NOT NULL,
    `last_updated_at` TIMESTAMP   NOT NULL,
    `deleted_at`      TIMESTAMP   NULL
);

CREATE TABLE IF NOT EXISTS `seat`
(
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `seat_grade_id`   BIGINT      NOT NULL,
    `seat_section_id` BIGINT      NOT NULL,
    `seat_info`       VARCHAR(10) NOT NULL,
    `average_score`   DECIMAL     NOT NULL,
    `created_at`      TIMESTAMP   NOT NULL,
    `last_updated_at` TIMESTAMP   NOT NULL,
    `deleted_at`      TIMESTAMP   NULL
);

CREATE TABLE IF NOT EXISTS `point_log`
(
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `member_id`       BIGINT      NOT NULL,
    `used_amount`     INT         NOT NULL,
    `used_field`      VARCHAR(30) NOT NULL,
    `remaining_point` INT         NOT NULL,
    `created_at`      TIMESTAMP   NOT NULL,
    `last_updated_at` TIMESTAMP   NOT NULL,
    `deleted_at`      TIMESTAMP   NULL
);

CREATE TABLE IF NOT EXISTS `review`
(
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `member_id`       BIGINT         NOT NULL,
    `seat_id`         BIGINT         NOT NULL,
    `title`           VARCHAR(50)    NULL,
    `content`         VARCHAR(10000) NULL,
    `score`           INT            NULL,
    `view_count`      INT            NOT NULL,
    `published`       BOOLEAN        NOT NULL,
    `like_count`      INT            NOT NULL,
    `dislike_count`   INT            NOT NULL,
    `created_at`      TIMESTAMP      NOT NULL,
    `last_updated_at` TIMESTAMP      NOT NULL,
    `deleted_at`      TIMESTAMP      NULL
);

CREATE TABLE IF NOT EXISTS `comment`
(
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `member_id`       BIGINT        NOT NULL,
    `review_id`       BIGINT        NOT NULL,
    `content`         VARCHAR(1000) NOT NULL,
    `created_at`      TIMESTAMP     NOT NULL,
    `last_updated_at` TIMESTAMP     NOT NULL,
    `deleted_at`      TIMESTAMP     NULL
);

CREATE TABLE IF NOT EXISTS `review_vote`
(
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `member_id`       BIGINT      NOT NULL,
    `review_id`       BIGINT      NOT NULL,
    `vote_choice`     VARCHAR(10) NOT NULL,
    `created_at`      TIMESTAMP   NOT NULL,
    `last_updated_at` TIMESTAMP   NOT NULL,
    `deleted_at`      TIMESTAMP   NULL
);

CREATE TABLE IF NOT EXISTS `image`
(
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `image_url`       VARCHAR(500) NOT NULL,
    `image_type`      VARCHAR(30)  NOT NULL,
    `reference_id`    BIGINT       NOT NULL,
    `uploaded_name`   VARCHAR(50)  NOT NULL,
    `saved_name`      VARCHAR(50)  NOT NULL,
    `created_at`      TIMESTAMP    NOT NULL,
    `last_updated_at` TIMESTAMP    NOT NULL,
    `deleted_at`      TIMESTAMP    NULL
);

