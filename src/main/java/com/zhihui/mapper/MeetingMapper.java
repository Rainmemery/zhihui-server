package com.zhihui.mapper;

import com.zhihui.dto.MeetingQueryDTO;
import com.zhihui.entity.Meeting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MeetingMapper {

    int insert(Meeting meeting);

    Meeting selectById(Long id);

    List<Meeting> selectList(@Param("query") MeetingQueryDTO query,
                             @Param("offset") int offset,
                             @Param("size") int size);

    long count(@Param("query") MeetingQueryDTO query);

    List<Meeting> selectMyCreated(@Param("creatorId") Long creatorId,
                                  @Param("offset") int offset,
                                  @Param("size") int size);

    List<Meeting> selectMyJoined(@Param("userId") Long userId,
                                 @Param("offset") int offset,
                                 @Param("size") int size);

    int update(Meeting meeting);

    int deleteById(Long id);
}
