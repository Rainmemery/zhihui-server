package com.zhihui.service.impl;


import com.zhihui.common.RedisUtils;
import com.zhihui.common.UserContextHolder;
import com.zhihui.entity.Team;
import com.zhihui.entity.TeamMember;
import com.zhihui.entity.User;
import com.zhihui.mapper.TeamMapper;
import com.zhihui.mapper.UserMapper;
import com.zhihui.service.TeamService;
import com.zhihui.vo.TeamVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
public class TeamServiceImpl implements TeamService {

    @Autowired
    private TeamMapper teamMapper;
    @Autowired
    private RedisUtils redisUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TeamVO createTeam(Team team) {//创建者自动加入团队

        User currentUser = UserContextHolder.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        team.setCreatorId(currentUser.getId());
        team.setStatus(1); // 设置团队状态为正常

        //插入team
        teamMapper.insert(team);
        Long teamId = team.getId();

        //将创建者加入团队
        TeamMember teamMember = new TeamMember();
        teamMember.setTeamId(teamId);
        teamMember.setUserId(currentUser.getId());
        teamMember.setRole("CREATOR");
        teamMember.setJoinTime(LocalDateTime.now()); // 设置加入时间
        teamMapper.insertMember(teamMember);

        TeamVO teamVO = new TeamVO();//返回视图对象
        teamVO.setTeamId(teamId);
        teamVO.setTeamName(team.getName());
        teamVO.setTeamDescription(team.getDescription());
        return teamVO;
    }

    @Override
    //@Cacheable(value = "team", key = "#id", unless = "#result == null")
    public TeamVO getById(Long id) {
        TeamVO teamVO = new TeamVO();
        String cacheKey = "team:" + id;
        Team cached = redisUtils.get(cacheKey);
        if(cached!=null){
            if (cached.getId() == null) return null; // 空缓存标记
            teamVO.setTeamId(cached.getId());
            teamVO.setTeamName(cached.getName());
            teamVO.setTeamDescription(cached.getDescription());
            return teamVO;
        }
        log.info("查询团队，id={}，走数据库", id);
        Team team = teamMapper.selectById(id);
        if (team == null) {
            redisUtils.set(cacheKey, new Team(), 60);
            log.warn("团队不存在，id={}", id);
            return null;
        }else{
            redisUtils.set(cacheKey, team, 1800);
        }
        teamVO.setTeamId(team.getId());
        teamVO.setTeamName(team.getName());
        teamVO.setTeamDescription(team.getDescription());
        return teamVO;
    }

    @Override
    public List<TeamVO> Teams() {
        List<Team>teams=teamMapper.selectByUserId(UserContextHolder.getCurrentUser().getId());
        List<TeamVO> teamVOS=new ArrayList<>();
        for(Team team:teams){
            TeamVO teamVO=new TeamVO();
            teamVO.setTeamId(team.getId());
            teamVO.setTeamName(team.getName());
            teamVO.setTeamDescription(team.getDescription());
            teamVOS.add(teamVO);
        }
        return teamVOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "team", key = "#id")
    public TeamVO disbandTeam(Long id) {
        Team team=teamMapper.selectById(id);//查询团队详情

        if(team==null){
            log.info("团队不存在");
            return null;
        }

        User currentUser = UserContextHolder.getCurrentUser();
        TeamVO teamVO=new TeamVO();

        if(currentUser.getId().equals(team.getCreatorId())){//创建者才可以删除团队
            teamVO.setTeamId(team.getId());
            teamVO.setTeamName(team.getName());
            teamVO.setTeamDescription(team.getDescription());
            teamMapper.deleteById(id);//删除团队
            //删除团队对应的团队成员
            teamMapper.deleteMembersByTeamId(team.getId());
            log.info("删除成功");
        }else{
            log.info("删除失败，需要创建者进行删除");
        }
        return teamVO;
    }

    @Override
    public TeamVO joinTeam(Long id) {
        User currentUser = UserContextHolder.getCurrentUser();
        Team team=teamMapper.selectById(id);
        TeamVO teamVO=new TeamVO();
        if(team!=null){
            //检查是否已经加入

            TeamMember teamMember=teamMapper.selectMemberByTeamAndUser(team.getId(),currentUser.getId());
            if(teamMember!=null){
                log.warn("不能重复加入团队");
            }else{
                teamMember=new TeamMember();
                teamMember.setTeamId(team.getId());
                teamMember.setUserId(currentUser.getId());
                teamMember.setRole("MEMBER");
                teamMember.setJoinTime(LocalDateTime.now());
                //加入团队
                teamMapper.insertMember(teamMember);
                log.info("加入团队成功");
            }

            //返回数据
            teamVO.setTeamId(team.getId());
            teamVO.setTeamName(team.getName());
            teamVO.setTeamDescription(team.getDescription());

        }else{
            log.info("团队不存在");
        }
        return teamVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMember(Long teamId, Long userId) {
        User currentUser = UserContextHolder.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }

        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new RuntimeException("团队不存在");
        }

        String operatorRole = checkTeamPermission(teamId, currentUser.getId());
        TeamMember targetMember = teamMapper.selectMemberByTeamAndUser(teamId, userId);
        
        if (targetMember == null) {
            throw new RuntimeException("该用户不在团队中");
        }

        String targetRole = targetMember.getRole();

        if ("CREATOR".equals(targetRole)) {
            throw new RuntimeException("不能移除创建者");
        }

        if ("ADMIN".equals(operatorRole) && "ADMIN".equals(targetRole)) {
            throw new RuntimeException("管理员不能移除其他管理员");
        }

        teamMapper.deleteMember(teamId, userId);
        log.info("移除成员成功: teamId={}, userId={}", teamId, userId);
    }

    private String checkTeamPermission(Long teamId, Long userId) {
        TeamMember member = teamMapper.selectMemberByTeamAndUser(teamId, userId);
        if (member == null) {
            throw new RuntimeException("您不在该团队中");
        }
        
        String role = member.getRole();
        if (!"CREATOR".equals(role) && !"ADMIN".equals(role)) {
            throw new RuntimeException("权限不足，需要管理员或创建者权限");
        }
        
        return role;
    }
}