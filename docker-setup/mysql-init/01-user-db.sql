-- 该脚本会在 MySQL 容器【首次】启动时自动执行
-- 后续新增服务（product-service等）时，把对应建库建表语句也放进这个目录，
-- 命名建议按顺序：02-product-db.sql、03-inventory-db.sql ...

CREATE DATABASE IF NOT EXISTS user_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE user_db;

CREATE TABLE t_user (
                        id              BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
                        username        VARCHAR(64)  NOT NULL COMMENT '登录名',
                        password        VARCHAR(128) NOT NULL COMMENT 'BCrypt加密密码',
                        nickname        VARCHAR(64)  DEFAULT NULL,
                        phone           VARCHAR(20)  DEFAULT NULL,
                        email           VARCHAR(128) DEFAULT NULL,
                        avatar          VARCHAR(255) DEFAULT NULL,
                        status          TINYINT      NOT NULL DEFAULT 1 COMMENT '1正常 0禁用',
                        create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        UNIQUE KEY uk_username (username),
                        UNIQUE KEY uk_phone (phone)
) ENGINE=InnoDB COMMENT='用户表';

CREATE TABLE t_address (
                           id              BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
                           user_id         BIGINT UNSIGNED NOT NULL,
                           receiver_name   VARCHAR(64)  NOT NULL,
                           receiver_phone  VARCHAR(20)  NOT NULL,
                           province        VARCHAR(64)  NOT NULL,
                           city            VARCHAR(64)  NOT NULL,
                           district        VARCHAR(64)  NOT NULL,
                           detail_address  VARCHAR(255) NOT NULL,
                           is_default      TINYINT      NOT NULL DEFAULT 0,
                           create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           KEY idx_user_id (user_id)
) ENGINE=InnoDB COMMENT='收货地址表';

-- 建一个专用账号给 user-service 使用，不要用 root 连业务库（好习惯，展示时也加分）
CREATE USER IF NOT EXISTS 'user_svc'@'%' IDENTIFIED BY 'userSvc123456';
GRANT ALL PRIVILEGES ON user_db.* TO 'user_svc'@'%';
FLUSH PRIVILEGES;