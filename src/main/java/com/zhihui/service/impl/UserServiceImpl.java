package com.zhihui.service.impl;

import com.zhihui.common.JwtUtils;
import com.zhihui.common.exception.BusinessException;
import com.zhihui.dto.UserLoginDTO;
import com.zhihui.dto.UserRegisterDTO;
import com.zhihui.entity.User;
import com.zhihui.mapper.UserMapper;
import com.zhihui.service.UserService;
import com.zhihui.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtils jwtUtils;


    @Transactional
    @Override
    public User register(UserRegisterDTO dto) {
        // 1. 校验用户名唯一
        if (userMapper.selectByUsername(dto.getUsername()) != null) {
            throw new BusinessException(409, "用户名已存在");
        }
        // 2. 加密密码
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        // 3. 入库
        userMapper.insert(user);
        user.setPassword(null); // 返回前脱敏
        return user;
    }

    @Override
    public LoginVO login(UserLoginDTO dto) {
        // 1. 查用户
        User user = userMapper.selectByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }
        // 2. 验密
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "密码错误");
        }
        // 3. 生成 token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        return LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}
