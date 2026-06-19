package com.zhihui.mapper;

import com.zhihui.entity.Team;
import com.zhihui.entity.TeamMember;
import com.zhihui.vo.TeamMemberVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TeamMapper {
    int insert(Team team);
    Team selectById(Long id);
    List<Team> selectByUserId(Long userId);
    int update(Team team);
    int deleteById(Long id);
    List<TeamMemberVO> selectMembersByTeamId(Long teamId);
    int insertMember(TeamMember teamMember);
    int deleteMemberByUserId(Long userId);

    @Select("SELECT * FROM team_member WHERE team_id = #{teamId} AND user_id = #{userId}")
    TeamMember selectMemberByTeamAndUser(@Param("teamId") Long teamId,
                                         @Param("userId") Long userId);

    int deleteMembersByTeamId(Long teamId);

    int deleteMember(@Param("teamId") Long teamId, @Param("userId") Long userId);
}