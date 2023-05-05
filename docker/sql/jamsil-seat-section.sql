DROP PROCEDURE IF EXISTS insert_seat_sections;
DELIMITER //
CREATE PROCEDURE insert_seat_sections(IN stadium_id INT, IN grade_id INT, IN start_section INT, IN last_section INT)
BEGIN
    DECLARE i INT DEFAULT start_section;
    WHILE i <= last_section
        DO
            INSERT INTO seat_section (name, stadium_id, seat_grade_id, created_at, deleted_at, last_updated_at)
            VALUES (CONCAT(i), stadium_id, grade_id, NOW(), null, NOW());
            SET i = i + 1;
        END WHILE;
END //
DELIMITER ;

INSERT INTO seat_section (name, stadium_id, seat_grade_id, created_at, deleted_at, last_updated_at)
VALUES ('프리미엄', 1, 1, NOW(), null, NOW());

CALL insert_seat_sections(1, 2, 110, 113);
CALL insert_seat_sections(1, 2, 212, 215);

INSERT INTO seat_section (name, stadium_id, seat_grade_id, created_at, deleted_at, last_updated_at)
VALUES ('홈', 1, 3, NOW(), null, NOW());
INSERT INTO seat_section (name, stadium_id, seat_grade_id, created_at, deleted_at, last_updated_at)
VALUES ('원정', 1, 3, NOW(), null, NOW());

CALL insert_seat_sections(1, 4, 107, 109);
CALL insert_seat_sections(1, 4, 209, 211);
CALL insert_seat_sections(1, 4, 114, 116);
CALL insert_seat_sections(1, 4, 216, 218);

INSERT INTO seat_section (name, stadium_id, seat_grade_id, created_at, deleted_at, last_updated_at)
VALUES ('109', 1, 5, NOW(), null, NOW());
INSERT INTO seat_section (name, stadium_id, seat_grade_id, created_at, deleted_at, last_updated_at)
VALUES ('114', 1, 5, NOW(), null, NOW());

CALL insert_seat_sections(1, 6, 205, 208);
CALL insert_seat_sections(1, 6, 219, 222);
CALL insert_seat_sections(1, 7, 101, 106);
CALL insert_seat_sections(1, 7, 201, 204);
CALL insert_seat_sections(1, 7, 117, 122);
CALL insert_seat_sections(1, 7, 223, 226);
CALL insert_seat_sections(1, 8, 101, 102);
CALL insert_seat_sections(1, 8, 121, 122);
CALL insert_seat_sections(1, 9, 301, 334);
CALL insert_seat_sections(1, 10, 401, 422);