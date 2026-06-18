package com.zhihui.controller;

import com.zhihui.common.Result;
import com.zhihui.common.UserContextHolder;
import com.zhihui.dto.UserLoginDTO;
import com.zhihui.dto.UserRegisterDTO;
import com.zhihui.entity.User;
import com.zhihui.service.UserService;
import com.zhihui.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public Result<User> getCurrentUser() {//重构成DTO
        User user = UserContextHolder.getCurrentUser();
        if (user == null) {
            log.error("用户未登录");
            return Result.fail(401, "用户未登录");
        }
        user.setPassword(null);
        log.info("获取当前用户成功，用户：{}", user);
        return Result.success(user);
    }
}