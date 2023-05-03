DROP PROCEDURE IF EXISTS insert_seat_sections;
DELIMITER //
CREATE PROCEDURE insert_seat_sections(IN grade_id INT, IN start_section INT, IN last_section INT)
BEGIN
    DECLARE i INT DEFAULT start_section;
    WHILE i <= last_section
        DO
            INSERT INTO seat_section (name, seat_grade_id, created_at, deleted_at, last_updated_at)
            VALUES (CONCAT(i), grade_id, NOW(), null, NOW());
            SET i = i + 1;
        END WHILE;
END //
DELIMITER ;

INSERT INTO seat_section (name, seat_grade_id, created_at, deleted_at, last_updated_at)
VALUES ('프리미엄', 1, NOW(), null, NOW());

CALL insert_seat_sections(2, 110, 113);
CALL insert_seat_sections(2, 212, 215);

INSERT INTO seat_section (name, seat_grade_id, created_at, deleted_at, last_updated_at)
VALUES ('홈', 3, NOW(), null, NOW());
INSERT INTO seat_section (name, seat_grade_id, created_at, deleted_at, last_updated_at)
VALUES ('원정', 3, NOW(), null, NOW());

CALL insert_seat_sections(4, 107, 109);
CALL insert_seat_sections(4, 209, 211);
CALL insert_seat_sections(4, 114, 116);
CALL insert_seat_sections(4, 216, 218);

INSERT INTO seat_section (name, seat_grade_id, created_at, deleted_at, last_updated_at)
VALUES ('109', 5, NOW(), null, NOW());
INSERT INTO seat_section (name, seat_grade_id, created_at, deleted_at, last_updated_at)
VALUES ('114', 5, NOW(), null, NOW());

CALL insert_seat_sections(6, 205, 208);
CALL insert_seat_sections(6, 219, 222);
CALL insert_seat_sections(7, 101, 106);
CALL insert_seat_sections(7, 201, 204);
CALL insert_seat_sections(7, 117, 122);
CALL insert_seat_sections(7, 223, 226);
CALL insert_seat_sections(8, 101, 102);
CALL insert_seat_sections(8, 121, 122);
CALL insert_seat_sections(9, 301, 334);
CALL insert_seat_sections(10, 401, 422);