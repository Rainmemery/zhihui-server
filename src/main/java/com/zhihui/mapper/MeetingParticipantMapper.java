package com.zhihui.mapper;

import com.zhihui.entity.MeetingParticipant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MeetingParticipantMapper {

    int insert(MeetingParticipant participant);

    int batchInsert(@Param("meetingId") Long meetingId,
                    @Param("participants") List<MeetingParticipant> participants);

    int deleteById(Long id);

    int deleteByMeetingAndUser(@Param("meetingId") Long meetingId,
                               @Param("userId") Long userId);

    List<MeetingParticipant> selectByMeetingId(Long meetingId);

    MeetingParticipant selectByMeetingAndUser(@Param("meetingId") Long meetingId,
                                              @Param("userId") Long userId);
}
