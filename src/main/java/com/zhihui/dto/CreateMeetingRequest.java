package com.zhihui.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateMeetingRequest {
    @NotBlank(message = "会议标题不能为空")
    @Size(max = 200, message = "标题最多200字")
    private String title;

    private String description;

    @NotNull(message = "开始时间不能为空")
    @Future(message = "开始时间必须是未来时间")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Future(message = "结束时间必须是未来时间")
    private LocalDateTime endTime;

    @Size(max = 50, message = "最多添加50个参会人")
    private List<Long> participantIds;  // 可选：初始参会人ID列表

    // 校验：结束时间必须晚于开始时间
    @AssertTrue(message = "结束时间必须晚于开始时间")
    public boolean isEndAfterStart() {
        return startTime == null || endTime == null || endTime.isAfter(startTime);
    }
}