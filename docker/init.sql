CREATE TABLE `member`
(
    `id`              BIGINT      NOT NULL,
    `login_id`        VARCHAR(20) NOT NULL,
    `password`        VARCHAR(20) NOT NULL,
    `name`            VARCHAR(6)  NOT NULL,
    `birth`           TIMESTAMP   NOT NULL,
    `email`           VARCHAR(50) NOT NULL,
    `nickname`        VARCHAR(10) NOT NULL,
    `point`           INT         NOT NULL,
    `created_at`      TIMESTAMP   NOT NULL,
    `last_updated_at` TIMESTAMP   NOT NULL,
    `deleted_at`      TIMESTAMP NULL
);

CREATE TABLE `stadium`
(
    `id`              BIGINT       NOT NULL,
    `name`            VARCHAR(50)  NOT NULL,
    `address`         VARCHAR(255) NOT NULL,
    `home_team`       VARCHAR(50)  NOT NULL,
    `created_at`      TIMESTAMP    NOT NULL,
    `last_updated_at` TIMESTAMP    NOT NULL,
    `deleted_at`      TIMESTAMP NULL
);

CREATE TABLE `seat`
(
    `id`              BIGINT      NOT NULL,
    `stadium_id`      BIGINT      NOT NULL,
    `section`         VARCHAR(10) NOT NULL,
    `price`           INT         NOT NULL,
    `seat_number`     INT         NOT NULL,
    `average_score`   DECIMAL     NOT NULL,
    `created_at`      TIMESTAMP   NOT NULL,
    `last_updated_at` TIMESTAMP   NOT NULL,
    `deleted_at`      TIMESTAMP NULL
);

CREATE TABLE `point_log`
(
    `id`              BIGINT      NOT NULL,
    `member_id`       BIGINT      NOT NULL,
    `used_amount`     INT         NOT NULL,
    `used_field`      VARCHAR(30) NOT NULL,
    `remaining_point` INT         NOT NULL,
    `created_at`      TIMESTAMP   NOT NULL,
    `last_updated_at` TIMESTAMP   NOT NULL,
    `deleted_at`      TIMESTAMP NULL
);

CREATE TABLE `review`
(
    `id`              BIGINT        NOT NULL,
    `member_id`       BIGINT        NOT NULL,
    `seat_id`         BIGINT        NOT NULL,
    `title`           VARCHAR(50)   NOT NULL,
    `content`         VARCHAR(2000) NOT NULL,
    `score`           INT           NOT NULL,
    `hits`            INT           NOT NULL,
    `created_at`      TIMESTAMP     NOT NULL,
    `last_updated_at` TIMESTAMP     NOT NULL,
    `deleted_at`      TIMESTAMP NULL
);

CREATE TABLE `comment`
(
    `id`              BIGINT        NOT NULL,
    `member_id`       BIGINT        NOT NULL,
    `review_id`       BIGINT        NOT NULL,
    `content`         VARCHAR(1000) NOT NULL,
    `created_at`      TIMESTAMP     NOT NULL,
    `last_updated_at` TIMESTAMP     NOT NULL,
    `deleted_at`      TIMESTAMP NULL
);

CREATE TABLE `review_like`
(
    `id`              BIGINT      NOT NULL,
    `member_id`       BIGINT      NOT NULL,
    `review_id`       BIGINT      NOT NULL,
    `like_type`       VARCHAR(10) NOT NULL,
    `created_at`      TIMESTAMP   NOT NULL,
    `last_updated_at` TIMESTAMP   NOT NULL,
    `deleted_at`      TIMESTAMP NULL
);

CREATE TABLE `image`
(
    `id`              BIGINT       NOT NULL,
    `image_url`       VARCHAR(500) NOT NULL,
    `image_type`      VARCHAR(30)  NOT NULL,
    `reference_id`    BIGINT       NOT NULL,
    `uploaded_name`   VARCHAR(50)  NOT NULL,
    `saved_name`      VARCHAR(50)  NOT NULL,
    `created_at`      TIMESTAMP    NOT NULL,
    `last_updated_at` TIMESTAMP    NOT NULL,
    `deleted_at`      TIMESTAMP    NOT NULL
);

ALTER TABLE `member`
    ADD CONSTRAINT `PK_MEMBER` PRIMARY KEY (`id`);

ALTER TABLE `stadium`
    ADD CONSTRAINT `PK_STADIUM` PRIMARY KEY (`id`);

ALTER TABLE `seat`
    ADD CONSTRAINT `PK_SEAT` PRIMARY KEY (`id`);

ALTER TABLE `point_log`
    ADD CONSTRAINT `PK_POINT_LOG` PRIMARY KEY (`id`);

ALTER TABLE `review`
    ADD CONSTRAINT `PK_REVIEW` PRIMARY KEY (`id`);

ALTER TABLE `comment`
    ADD CONSTRAINT `PK_COMMENT` PRIMARY KEY (`id`);

ALTER TABLE `review_like`
    ADD CONSTRAINT `PK_REVIEW_LIKE` PRIMARY KEY (`id`);

ALTER TABLE `image`
    ADD CONSTRAINT `PK_IMAGE` PRIMARY KEY (`id`);

