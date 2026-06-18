package com.zhihui.vo;

import lombok.Data;

@Data
public class TeamMemberVO {
    private Long teamId;
    private String teamName;
    private Long userId;
    private String username;
    private String email;
}
