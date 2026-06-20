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

    // 状态流转（只更新 status + version）
    int updateStatusWithVersion(@Param("id") Long id,
                                @Param("status") String status,
                                @Param("version") Integer version);

    // 全字段更新（带乐观锁）
    int updateWithVersion(Meeting meeting);

    int deleteById(Long id);
}
