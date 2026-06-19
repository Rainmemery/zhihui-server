package com.zhihui.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MeetingQueryDTO {
    private String status;      // 按状态筛选
    private String keyword;     // 按标题模糊搜索
    private Long creatorId;     // 按创建人筛选
    private LocalDateTime startFrom;  // 开始时间 >=
    private LocalDateTime startTo;    // 开始时间 <=
    private String sortBy = "createTime";  // 排序字段（白名单）
    private String sortOrder = "DESC";     // 排序方向
}
