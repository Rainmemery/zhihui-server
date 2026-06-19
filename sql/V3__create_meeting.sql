-- 会议表
CREATE TABLE meeting (
                         id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                         title       VARCHAR(200) NOT NULL COMMENT '会议标题',
                         description TEXT COMMENT '会议描述',
                         start_time  DATETIME NOT NULL COMMENT '开始时间',
                         end_time    DATETIME NOT NULL COMMENT '结束时间',
                         status      VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT/SCHEDULED/IN_PROGRESS/COMPLETED/CANCELLED',
                         creator_id  BIGINT NOT NULL COMMENT '创建人ID',
                         create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         INDEX idx_creator (creator_id),
                         INDEX idx_status (status),
                         INDEX idx_start_time (start_time),
                         INDEX idx_creator_status (creator_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会议表';

-- 参会人表
CREATE TABLE meeting_participant (
                                     id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     meeting_id BIGINT NOT NULL COMMENT '会议ID',
                                     user_id    BIGINT NOT NULL COMMENT '用户ID',
                                     role       VARCHAR(20) NOT NULL DEFAULT 'PARTICIPANT' COMMENT '角色: HOST/PARTICIPANT',
                                     join_time  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
                                     UNIQUE KEY uk_meeting_user (meeting_id, user_id),
                                     INDEX idx_meeting_id (meeting_id),
                                     INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会议参会人表';