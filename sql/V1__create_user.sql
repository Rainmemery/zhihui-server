-- src/main/resources/db/migration/V1__create_user.sql
CREATE TABLE IF NOT EXISTS `user` (
                                      `id`          BIGINT        AUTO_INCREMENT PRIMARY KEY,
                                      `username`    VARCHAR(50)   NOT NULL UNIQUE COMMENT '用户名',
    `password`    VARCHAR(255)  NOT NULL COMMENT '密码（BCrypt加密）',
    `email`       VARCHAR(100)  DEFAULT NULL COMMENT '邮箱',
    `avatar_url`  VARCHAR(255)  DEFAULT NULL COMMENT '头像URL',
    `status`      TINYINT       DEFAULT 1 COMMENT '状态 1-正常 0-禁用',
    `create_time` DATETIME      DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX `uk_username` (`username`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

INSERT INTO user (username, password, email) VALUES
                                                 ('zhangsan', 'test123', 'zhangsan@test.com'),
                                                 ('lisi',     'test123', 'lisi@test.com'),
                                                 ('wangwu',   'test123', 'wangwu@test.com');