package com.zhihui.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Team {
    private Long id;
    private String name;
    private String description;
    private Long creatorId;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
