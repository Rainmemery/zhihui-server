package com.zhihui.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MeetingParticipant {
    private Long id;
    private Long meetingId;
    private Long userId;
    private String role;
    private LocalDateTime joinTime;
}
