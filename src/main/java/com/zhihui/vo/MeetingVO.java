package com.zhihui.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MeetingVO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Long creatorId;
    private String creatorName;      // 创建人名称（冗余）
    private List<ParticipantVO> participants; // 参会人列表
    private LocalDateTime createTime;
}