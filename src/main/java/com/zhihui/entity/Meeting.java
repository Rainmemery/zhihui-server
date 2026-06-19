package com.zhihui.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Meeting {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Long creatorId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
