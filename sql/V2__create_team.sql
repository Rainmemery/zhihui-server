-- ============================================
-- V2__create_team.sql
-- 团队表 & 团队成员表
-- ============================================

-- 团队表
CREATE TABLE team (
                      id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '团队ID',
                      name        VARCHAR(100) NOT NULL COMMENT '团队名称',
                      description VARCHAR(500) COMMENT '团队描述',
                      creator_id  BIGINT NOT NULL COMMENT '创建人ID',
                      status      TINYINT DEFAULT 1 COMMENT '状态: 1-正常, 0-已解散',
                      create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                      update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                      INDEX idx_creator (creator_id),
                      INDEX idx_status (status),
                      KEY fk_team_creator (creator_id),
                      CONSTRAINT fk_team_creator FOREIGN KEY (creator_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队表';

-- 团队成员表
CREATE TABLE team_member (
                             id        BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
                             team_id   BIGINT NOT NULL COMMENT '团队ID',
                             user_id   BIGINT NOT NULL COMMENT '用户ID',
                             role      VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT '角色: CREATOR/ADMIN/MEMBER',
                             join_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
                             UNIQUE KEY uk_team_user (team_id, user_id) COMMENT '防止重复加入',
                             INDEX idx_user_id (user_id),
                             CONSTRAINT fk_tm_team FOREIGN KEY (team_id) REFERENCES team(id) ON DELETE CASCADE,
                             CONSTRAINT fk_tm_user FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队成员表';