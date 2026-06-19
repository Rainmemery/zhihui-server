package com.zhihui.service;

import com.zhihui.dto.CreateMeetingRequest;
import com.zhihui.dto.MeetingQueryDTO;
import com.zhihui.entity.Meeting;
import com.zhihui.vo.MeetingVO;
import com.zhihui.vo.PageVO;

import java.util.List;

public interface MeetingService {

    MeetingVO createMeeting(CreateMeetingRequest dto);

    MeetingVO getById(Long id);

    PageVO<Meeting> list(int page, int size, MeetingQueryDTO query);

    PageVO<MeetingVO> listMyCreated(int page, int size);

    PageVO<MeetingVO> listMyJoined(int page, int size);

    MeetingVO updateMeeting(Long id, CreateMeetingRequest dto);

    void deleteMeeting(Long id);
}
