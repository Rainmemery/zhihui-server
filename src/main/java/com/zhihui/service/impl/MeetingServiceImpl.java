package com.zhihui.service.impl;

import com.zhihui.common.UserContextHolder;
import com.zhihui.common.exception.BusinessException;
import com.zhihui.common.exception.ErrorCode;
import com.zhihui.dto.CreateMeetingRequest;
import com.zhihui.dto.MeetingQueryDTO;
import com.zhihui.entity.Meeting;
import com.zhihui.entity.MeetingParticipant;
import com.zhihui.entity.User;
import com.zhihui.enums.MeetingStatus;
import com.zhihui.mapper.MeetingMapper;
import com.zhihui.mapper.MeetingParticipantMapper;
import com.zhihui.mapper.UserMapper;
import com.zhihui.service.MeetingService;
import com.zhihui.vo.MeetingVO;
import com.zhihui.vo.PageVO;
import com.zhihui.vo.ParticipantVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service    // 标记为Spring业务层Bean
@Slf4j      // Lombok日志注解，自动生成log对象
public class MeetingServiceImpl implements MeetingService {

    // 注入依赖
    @Autowired private MeetingMapper meetingMapper;
    @Autowired private MeetingParticipantMapper participantMapper;
    @Autowired private UserMapper userMapper;

    /**
     * 创建会议：事务保证原子性
     * @Transactional 声明式事务：方法内所有数据库操作要么全部成功，要么全部回滚
     * 默认只对RuntimeException回滚，业务异常一般继承RuntimeException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MeetingVO createMeeting(CreateMeetingRequest dto) {
        // 1. 从上下文获取当前登录用户ID（拦截器解析token后存入ThreadLocal）
        Long currentUserId = UserContextHolder.getCurrentUserId();

        // 2. 构建会议实体，插入数据库
        Meeting meeting = new Meeting();
        meeting.setTitle(dto.getTitle());
        meeting.setDescription(dto.getDescription());
        meeting.setStartTime(dto.getStartTime());
        meeting.setEndTime(dto.getEndTime());
        meeting.setStatus("DRAFT"); // 默认草稿状态
        meeting.setCreatorId(currentUserId);
        // 插入后，主键id会通过useGeneratedKeys自动回填到meeting对象中
        meetingMapper.insert(meeting);

        // 3. 创建者自动加入会议，角色为HOST（主持人）
        MeetingParticipant host = new MeetingParticipant();
        host.setMeetingId(meeting.getId());
        host.setUserId(currentUserId);
        host.setRole("HOST");
        participantMapper.insert(host);

        // 4. 批量添加初始参会人（前端传的列表）
        if (dto.getParticipantIds() != null && !dto.getParticipantIds().isEmpty()) {
            List<MeetingParticipant> participants = dto.getParticipantIds().stream()
                    // 过滤掉创建者自己（避免重复加入）
                    .filter(userId -> !userId.equals(currentUserId))
                    // 去重：防止前端传重复的用户ID
                    .distinct()
                    // 转换为参会人实体
                    .map(userId -> {
                        MeetingParticipant p = new MeetingParticipant();
                        p.setMeetingId(meeting.getId());
                        p.setUserId(userId);
                        p.setRole("PARTICIPANT");
                        return p;
                    })
                    .collect(Collectors.toList());

            // 列表不为空才批量插入
            if (!participants.isEmpty()) {
                participantMapper.batchInsert(meeting.getId(), participants);
            }
        }

        // 5. 返回组装好的会议详情VO
        return getById(meeting.getId());
    }

    // 其余方法（listMyCreated、listMyJoined、update、delete）逻辑类似，此处省略核心框架
    @Override
    public PageVO<MeetingVO> listMyCreated(int page, int size) {
        int offset = (page - 1) * size;
        List<Meeting> meetings = meetingMapper.selectMyCreated(UserContextHolder.getCurrentUserId(), offset, size);
        List<MeetingVO> voList = meetings.stream().map(this::buildMeetingVO).collect(Collectors.toList());
        PageVO<MeetingVO> pageVO = new PageVO<>(page, size, voList.size(), voList);
        return pageVO;
    }

    @Override
    public PageVO<MeetingVO> listMyJoined(int page, int size) {
        int offset = (page - 1) * size;
        List<Meeting> meetings = meetingMapper.selectMyJoined(UserContextHolder.getCurrentUserId(), offset, size);
        List<MeetingVO> voList = meetings.stream().map(this::buildMeetingVO).collect(Collectors.toList());
        PageVO<MeetingVO> pageVO = new PageVO<>(page, size, voList.size(), voList);
        return pageVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MeetingVO updateMeeting(Long id, CreateMeetingRequest dto) {//更新会议时不需要更新参会人
        if (dto.getParticipantIds() != null) {
            throw new BusinessException(401,"不允许更改参会人");
        }
        Meeting meeting=meetingMapper.selectById(id);
        meeting.setTitle(dto.getTitle());
        meeting.setDescription(dto.getDescription());
        meeting.setStartTime(dto.getStartTime());
        meeting.setEndTime(dto.getEndTime());
        meeting.setStatus("DRAFT");
        meeting.setCreatorId(UserContextHolder.getCurrentUserId());
        meetingMapper.updateWithVersion(meeting);
        //前端不允许更改参会人，更改参会人通过别的接口
        MeetingVO meetingVO = buildMeetingVO(meeting);//乐观锁更新
        return meetingVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMeeting(Long id) {
        Meeting meeting=meetingMapper.selectById(id);
        if(meeting==null){
            throw new BusinessException(404,"无法删除不存在的会议");
        }
        //删除会议以及参会人
        meetingMapper.deleteById(id);
        List<MeetingParticipant> participants=participantMapper.selectByMeetingId(meeting.getId());
        for(MeetingParticipant p:participants){
            participantMapper.deleteByMeetingAndUser(p.getMeetingId(),p.getUserId());
        }
    }

    /**
     * 查询会议详情
     */
    @Override
    public MeetingVO getById(Long id) {
        // 先查会议主表
        Meeting meeting = meetingMapper.selectById(id);
        if (meeting == null) {
            // 业务异常：会议不存在，全局异常处理器捕获返回统一错误
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "会议不存在");
        }
        // 组装VO返回
        return buildMeetingVO(meeting);
    }

    /**
     * 私有方法：组装会议详情VO（关联创建人、参会人信息）
     */
    private MeetingVO buildMeetingVO(Meeting meeting) {
        MeetingVO vo = new MeetingVO();
        // 拷贝基础属性（工具类自动拷贝同名属性）
        BeanUtils.copyProperties(meeting, vo);

        // 1. 查询创建人名称
        User creator = userMapper.selectById(meeting.getCreatorId());
        vo.setCreatorName(creator != null ? creator.getUsername() : null);

        // 2. 查询参会人列表，组装VO
        List<MeetingParticipant> participantList = participantMapper.selectByMeetingId(meeting.getId());
        List<ParticipantVO> participantVOS = participantList.stream().map(p -> {
            ParticipantVO pvo = new ParticipantVO();
            pvo.setUserId(p.getUserId());
            pvo.setRole(p.getRole());
            // 查询每个参会人的用户名
            User user = userMapper.selectById(p.getUserId());
            pvo.setUsername(user != null ? user.getUsername() : "未知用户");
            return pvo;
        }).collect(Collectors.toList());
        vo.setParticipants(participantVOS);

        return vo;
    }

    /**
     * 分页条件查询会议列表
     */
    @Override
    public PageVO<Meeting> list(int page, int size, MeetingQueryDTO query) {
        // 1. 白名单校验排序字段：防止SQL注入（因为排序用的是${}）
        validateSort(query);

        // 2. 计算偏移量：第page页，跳过 (page-1)*size 条
        int offset = (page - 1) * size;

        // 3. 查询当前页数据
        List<Meeting> list = meetingMapper.selectList(query, offset, size);

        // 4. 查询总记录数
        long total = meetingMapper.count(query);

        // 5. 组装分页VO返回
        return new PageVO<>(page, size, total, list);
    }

    /**
     * 排序参数白名单校验
     */
    private void validateSort(MeetingQueryDTO query) {
        // 允许的排序字段
        Set<String> allowedFields = Set.of("createTime", "startTime", "updateTime");
        if (query.getSortBy() != null && !allowedFields.contains(query.getSortBy())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "不支持的排序字段");
        }
        // 允许的排序方向
        Set<String> allowedOrders = Set.of("ASC", "DESC");
        if (query.getSortOrder() != null && !allowedOrders.contains(query.getSortOrder().toUpperCase())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "不支持的排序方向");
        }
    }

    /**
     * 状态流转通用方法（带乐观锁）
     *
     * @param id         会议 ID
     * @param target     目标状态
     * @param maxRetries 重试次数
     */
    private void transitionStatus(Long id, MeetingStatus target, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            Meeting meeting = meetingMapper.selectById(id);
            if (meeting == null) {
                throw new BusinessException(404, "会议不存在");
            }

            MeetingStatus currentStatus = MeetingStatus.fromCode(meeting.getStatus());

            log.info("检验状态转换是否合法");
            // 校验状态转换是否合法
            currentStatus.validateTransition(target);
            log.info("状态转换合法，开始更新");
            // 乐观锁更新
            int affectedRows = meetingMapper.updateStatusWithVersion(
                    id, target.getCode(), meeting.getVersion());

            if (affectedRows > 0) {
                log.info("会议 {} 状态流转成功: {} → {}", id, currentStatus.getDesc(), target.getDesc());
                return; // 成功
            }

            // 冲突：版本号不匹配，重试
            log.warn("会议 {} 乐观锁冲突，重试 {}/{}", id, i + 1, maxRetries);
            try {
                Thread.sleep(ThreadLocalRandom.current().nextLong(10, 50));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        throw new BusinessException(400,
                "会议已被他人修改，请刷新后重试");
    }

    @Override
    public void schedule(Long id) {
        transitionStatus(id, MeetingStatus.SCHEDULED, 3);
    }

    @Override
    public void start(Long id) {
        Meeting meeting = meetingMapper.selectById(id);
        if (meeting == null) {
            throw new BusinessException(404, "会议不存在");
        }
        if (meeting.getStartTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException(400, "会议未到开始时间，不能提前开始");
        }
        transitionStatus(id, MeetingStatus.IN_PROGRESS, 3);
    }

    @Override
    public void end(Long id) {
        transitionStatus(id, MeetingStatus.COMPLETED, 3);
    }

    @Override
    public void cancel(Long id) {
        transitionStatus(id, MeetingStatus.CANCELLED, 3);
    }

    @Override
    @Transactional
    public void addParticipants(Long meetingId, List<Long> userIds) {
        Meeting meeting = meetingMapper.selectById(meetingId);
        if (meeting == null) throw new BusinessException(404, "会议不存在");

        MeetingStatus status = MeetingStatus.fromCode(meeting.getStatus());
        if (status.isTerminal()) {
            throw new BusinessException(400,
                    "会议已结束，不可添加参会人");
        }

        // 使用 INSERT IGNORE 防止重复
        for (Long userId : userIds) {
            try {
                MeetingParticipant p = new MeetingParticipant();
                p.setMeetingId(meetingId);
                p.setUserId(userId);
                p.setRole("PARTICIPANT");
                participantMapper.insert(p);
            } catch (DuplicateKeyException ignored) {
                // 已存在，跳过
            }
        }
    }

    @Override
    @Transactional
    public void removeParticipant(Long meetingId, Long userId) {
        Meeting meeting = meetingMapper.selectById(meetingId);
        if (meeting == null) throw new BusinessException(404, "会议不存在");

        MeetingStatus status = MeetingStatus.fromCode(meeting.getStatus());
        if (status.isTerminal()) {
            throw new BusinessException(400,
                    "会议已结束，不可移除参会人");
        }

        // HOST 不可被移除
        MeetingParticipant participant = participantMapper.selectByMeetingAndUser(meetingId, userId);
        if (participant == null) {
            throw new BusinessException(404, "该用户不在参会人列表中");
        }
        if ("HOST".equals(participant.getRole())) {
            throw new BusinessException(400, "HOST 不可被移除");
        }

        participantMapper.deleteByMeetingAndUser(meetingId, userId);
    }
}