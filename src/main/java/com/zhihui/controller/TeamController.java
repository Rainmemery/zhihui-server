package com.zhihui.controller;


import com.zhihui.common.Result;
import com.zhihui.entity.Team;
import com.zhihui.service.TeamService;
import com.zhihui.vo.TeamVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @PostMapping("/teams")
    public Result<TeamVO> teams(@RequestBody Team team){//创建团队
        TeamVO teamVO = teamService.createTeam(team);
        return Result.success(teamVO);
    }

    @PostMapping("/join")
    public Result<TeamVO> joinTeam(@PathVariable Long id){//user加入team
        TeamVO teamVO = teamService.joinTeam(id);
        return Result.success(teamVO);
    }

    @GetMapping("/teams/{id}")
    public Result<TeamVO> teams(@PathVariable Long id){//根据teamId查询团队
        TeamVO teamVO = teamService.getById(id);
        if(teamVO == null){
            return Result.fail(404,"查找失败");
        }
        return Result.success(teamVO);
    }

    @GetMapping("/teams/my")
    public Result<List<TeamVO>> teams(){//查询当前用户的团队
        List<TeamVO> teams=teamService.Teams();
        return Result.success(teams);
    }

    @DeleteMapping("/teams/{id}")
    public Result<TeamVO> deleteTeam(@PathVariable Long id){//根据ID删除团队
        TeamVO teamVO=teamService.disbandTeam(id);
        if(teamVO == null){
            return Result.fail(400,"删除失败");
        }
        return Result.success(teamVO);
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    public Result<Void> removeMember(@PathVariable Long teamId, @PathVariable Long userId) {
        teamService.removeMember(teamId, userId);
        return Result.success(null);
    }

}