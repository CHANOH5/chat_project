-- =========================================
-- Clean init.sql for chat service (MySQL 8)
-- =========================================
SET NAMES utf8mb4;
SET time_zone = '+09:00';

-- 안전모드: FK 무시하고 드랍
SET FOREIGN_KEY_CHECKS = 0;

-- 드랍 순서: 자식 → 부모
DROP TABLE IF EXISTS tb_message_status;
DROP TABLE IF EXISTS tb_chatroom_user;
DROP TABLE IF EXISTS tb_message;
DROP TABLE IF EXISTS tb_refresh_token;
DROP TABLE IF EXISTS tb_chatroom;
DROP TABLE IF EXISTS tb_user;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================
-- 1) 사용자
-- =========================================
CREATE TABLE tb_user (
    id              VARCHAR(20)      NOT NULL PRIMARY KEY COMMENT '아이디',
    password        VARCHAR(255)     NOT NULL COMMENT '비밀번호(BCrypt 등 해시)',
    name            VARCHAR(20)      NOT NULL COMMENT '이름',
    nickname        VARCHAR(20)      NOT NULL COMMENT '별명',
    email           VARCHAR(50)               NULL COMMENT '이메일',
    is_anonymous    TINYINT          NOT NULL DEFAULT 1 COMMENT '회원가입 여부 (회원-0, 비회원-1)',
    role            TINYINT          NOT NULL DEFAULT 1 COMMENT '권한 (관리자-0, 사용자-1)',
    profile_image   VARCHAR(255)              NULL COMMENT '프로필 이미지',
    status          TINYINT          NOT NULL DEFAULT 0 COMMENT '활성(0), 탈퇴(1)',
    created_at      DATETIME(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '생성일',
    UNIQUE KEY uk_user_nickname (nickname),
    UNIQUE KEY uk_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='사용자';

-- =========================================
-- 2) 리프레시 토큰
-- =========================================
CREATE TABLE tb_refresh_token (
    id              BIGINT           NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '고유 ID',
    user_id         VARCHAR(20)      NOT NULL COMMENT '사용자 ID',
    refresh_token   TEXT             NOT NULL COMMENT '리프레시 토큰',
    expires_at      DATETIME         NOT NULL COMMENT '만료일',
    created_at      DATETIME(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '생성일',
    CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES tb_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='리프레시 토큰';

CREATE INDEX idx_refresh_user ON tb_refresh_token (user_id);

-- =========================================
-- 3) 채팅방
-- =========================================
CREATE TABLE tb_chatroom (
    id              BIGINT           NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '채팅방 고유 ID',
    name            VARCHAR(30)      NOT NULL COMMENT '채팅방 이름',
    room_type       VARCHAR(20)      NOT NULL DEFAULT 'PRIVATE' COMMENT '채팅방 유형 (PRIVATE, PUBLIC)',
    created_at      DATETIME(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '생성일'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='채팅방';

CREATE INDEX idx_chatroom_type ON tb_chatroom (room_type);

-- 기본 공개 채팅방 생성
INSERT INTO tb_chatroom (name, room_type) VALUES ('공개 채팅방', 'PUBLIC');

-- =========================================
-- 4) 메시지
-- =========================================
CREATE TABLE tb_message (
    id              BIGINT           NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '메시지 ID',
    chatroom_id     BIGINT           NOT NULL COMMENT '채팅방 ID',
    sender_id       VARCHAR(20)      NOT NULL COMMENT '보낸사람 ID',
    content         TEXT             NOT NULL COMMENT '메시지 내용',
    message_type    VARCHAR(20)      NOT NULL DEFAULT 'TEXT' COMMENT '메시지 타입 (TEXT, IMAGE, FILE 등)',
    action_type     VARCHAR(20)      NOT NULL DEFAULT 'TALK' COMMENT '동작 타입 (ENTER, TALK, LEAVE)',
    sent_at         DATETIME(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '보낸 시각',
    CONSTRAINT fk_message_chatroom FOREIGN KEY (chatroom_id) REFERENCES tb_chatroom(id),
    CONSTRAINT fk_message_sender   FOREIGN KEY (sender_id)   REFERENCES tb_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='메시지';

CREATE INDEX idx_msg_room_sentat ON tb_message (chatroom_id, sent_at);
CREATE INDEX idx_msg_sender ON tb_message (sender_id);

-- =========================================
-- 5) 채팅방-사용자 매핑
-- =========================================
CREATE TABLE tb_chatroom_user (
    id                   BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '고유 ID',
    chatroom_id          BIGINT        NOT NULL COMMENT '채팅방 고유 ID',
    user_id              VARCHAR(20)   NOT NULL COMMENT '사용자 ID',
    joined_at            DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '입장 시각',
    last_read_message_id BIGINT        NULL COMMENT '마지막 읽은 메시지 ID',
    CONSTRAINT fk_cru_chatroom FOREIGN KEY (chatroom_id) REFERENCES tb_chatroom(id),
    CONSTRAINT fk_cru_user     FOREIGN KEY (user_id)     REFERENCES tb_user(id),
    CONSTRAINT fk_cru_lastread FOREIGN KEY (last_read_message_id) REFERENCES tb_message(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='채팅방-사용자 매핑';

-- 한 채팅방에 같은 사용자가 중복 입장 레코드 생기지 않도록
CREATE UNIQUE INDEX uk_cru_room_user ON tb_chatroom_user (chatroom_id, user_id);

-- =========================================
-- 6) 메시지 읽음 상태
-- =========================================
CREATE TABLE tb_message_status (
    id              BIGINT         NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '고유 ID',
    message_id      BIGINT         NOT NULL COMMENT '메시지 ID',
    user_id         VARCHAR(20)    NOT NULL COMMENT '사용자 ID(수신자 기준)',
    is_read         TINYINT        NOT NULL DEFAULT 0 COMMENT '읽음 여부',
    read_at         DATETIME(3)             NULL COMMENT '읽은 시각',
    CONSTRAINT fk_ms_message FOREIGN KEY (message_id) REFERENCES tb_message(id),
    CONSTRAINT fk_ms_user    FOREIGN KEY (user_id)    REFERENCES tb_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='메시지 읽음 상태';

CREATE INDEX idx_ms_user_read ON tb_message_status (user_id, is_read);
CREATE INDEX idx_ms_message ON tb_message_status (message_id);

-- admin 계정 생성 (비밀번호: 1234, BCrypt 암호화)
INSERT INTO tb_user (
    id, password, name, nickname, email, is_anonymous, role, profile_image, status, created_at
) VALUES (
    'admin',
    '$2a$10$CJ7.DCT4KsLjKmeV4C7IeeTJGTeAJedsUlwKEzW7RVG6XdqA0kRja', -- 1234 bcrypt
    '운영자',
    '운영자',
    NULL,
    0,   -- 회원가입 여부 (회원=0)
    0,   -- 권한 (관리자=0, 사용자=1)
    NULL,
    0,   -- status (활성=0)
    NOW(3)
);
