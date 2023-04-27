CREATE TABLE `member`
(
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `login_email`     VARCHAR(50) NOT NULL UNIQUE,
    `password`        VARCHAR(64) NOT NULL,
    `nickname`        VARCHAR(10) NOT NULL UNIQUE,
    `point`           INT         NOT NULL,
    `created_at`      TIMESTAMP   NOT NULL,
    `last_updated_at` TIMESTAMP   NOT NULL,
    `deleted_at`      TIMESTAMP   NULL
);

CREATE TABLE `stadium`
(
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name`            VARCHAR(50)  NOT NULL,
    `address`         VARCHAR(255) NOT NULL,
    `home_team`       VARCHAR(50)  NOT NULL,
    `created_at`      TIMESTAMP    NOT NULL,
    `last_updated_at` TIMESTAMP    NOT NULL,
    `deleted_at`      TIMESTAMP    NULL
);

CREATE TABLE `seat`
(
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `stadium_id`      BIGINT      NOT NULL,
    `section`         VARCHAR(10) NOT NULL,
    `price`           INT         NOT NULL,
    `seat_number`     INT         NOT NULL,
    `average_score`   DECIMAL     NOT NULL,
    `created_at`      TIMESTAMP   NOT NULL,
    `last_updated_at` TIMESTAMP   NOT NULL,
    `deleted_at`      TIMESTAMP   NULL
);

CREATE TABLE `point_log`
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

CREATE TABLE `review`
(
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `member_id`       BIGINT        NOT NULL,
    `seat_id`         BIGINT        NOT NULL,
    `title`           VARCHAR(50)   NOT NULL,
    `content`         VARCHAR(2000) NOT NULL,
    `score`           INT           NOT NULL,
    `hits`            INT           NOT NULL,
    `created_at`      TIMESTAMP     NOT NULL,
    `last_updated_at` TIMESTAMP     NOT NULL,
    `deleted_at`      TIMESTAMP     NULL
);

CREATE TABLE `comment`
(
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `member_id`       BIGINT        NOT NULL,
    `review_id`       BIGINT        NOT NULL,
    `content`         VARCHAR(1000) NOT NULL,
    `created_at`      TIMESTAMP     NOT NULL,
    `last_updated_at` TIMESTAMP     NOT NULL,
    `deleted_at`      TIMESTAMP     NULL
);

CREATE TABLE `review_like`
(
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `member_id`       BIGINT      NOT NULL,
    `review_id`       BIGINT      NOT NULL,
    `like_type`       VARCHAR(10) NOT NULL,
    `created_at`      TIMESTAMP   NOT NULL,
    `last_updated_at` TIMESTAMP   NOT NULL,
    `deleted_at`      TIMESTAMP   NULL
);

CREATE TABLE `image`
(
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `image_url`       VARCHAR(500) NOT NULL,
    `image_type`      VARCHAR(30)  NOT NULL,
    `reference_id`    BIGINT       NOT NULL,
    `uploaded_name`   VARCHAR(50)  NOT NULL,
    `saved_name`      VARCHAR(50)  NOT NULL,
    `created_at`      TIMESTAMP    NOT NULL,
    `last_updated_at` TIMESTAMP    NOT NULL,
    `deleted_at`      TIMESTAMP    NOT NULL
);

