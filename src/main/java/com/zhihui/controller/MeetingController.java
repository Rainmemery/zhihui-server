package com.zhihui.controller;

import com.zhihui.common.Result;
import com.zhihui.dto.CreateMeetingRequest;
import com.zhihui.dto.MeetingQueryDTO;
import com.zhihui.entity.Meeting;
import com.zhihui.service.MeetingService;
import com.zhihui.vo.MeetingVO;
import com.zhihui.vo.PageVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/meeting")
@Slf4j
public class MeetingController {

    @Autowired
    private MeetingService meetingService;

    /** 创建会议 */
    @PostMapping
    public Result<MeetingVO> create(@Valid @RequestBody CreateMeetingRequest dto) {
        return Result.success(meetingService.createMeeting(dto));
    }

    /** 会议详情 */
    @GetMapping("/{id}")
    public Result<MeetingVO> getById(@PathVariable Long id) {
        return Result.success(meetingService.getById(id));
    }

    /** 会议列表（分页 + 筛选） */
    @GetMapping
    public Result<PageVO<Meeting>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            MeetingQueryDTO query) {
        if (size > 100) size = 100;
        return Result.success(meetingService.list(page, size, query));
    }

    /** 我创建的会议 */
    @GetMapping("/my-created")
    public Result<PageVO<MeetingVO>> myCreated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(meetingService.listMyCreated(page, size));
    }

    /** 我参与的会议 */
    @GetMapping("/my-joined")
    public Result<PageVO<MeetingVO>> myJoined(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(meetingService.listMyJoined(page, size));
    }

    /** 更新会议（乐观锁版本实现见 Day09） */
    @PutMapping("/{id}")
    public Result<MeetingVO> update(@PathVariable Long id,
                                    @Valid @RequestBody CreateMeetingRequest dto) {
        return Result.success(meetingService.updateMeeting(id, dto));
    }

    /** 删除会议 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        meetingService.deleteMeeting(id);
        return Result.success(null);
    }
}
