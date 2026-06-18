package com.zhihui.service;

import com.zhihui.entity.Team;
import com.zhihui.vo.TeamVO;
import org.springframework.stereotype.Service;

import java.util.List;

public interface TeamService {
    TeamVO createTeam(Team team);
    TeamVO findTeam(Long id);
    List<TeamVO> Teams();
    TeamVO disbandTeam(Long id);
    TeamVO joinTeam(Long id);
    void removeMember(Long teamId, Long userId);
}