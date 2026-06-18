package com.zhihui.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamMember {
    private Long id;
    private Long teamId;
    private Long userId;
    private String role;  // CREATOR / ADMIN / MEMBER
    private LocalDateTime joinTime;
}
