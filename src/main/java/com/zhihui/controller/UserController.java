package com.zhihui.controller;

import com.zhihui.common.Result;
import com.zhihui.common.UserContextHolder;
import com.zhihui.dto.UserLoginDTO;
import com.zhihui.dto.UserRegisterDTO;
import com.zhihui.entity.User;
import com.zhihui.service.UserService;
import com.zhihui.vo.LoginVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public Result<User> getCurrentUser() {
        User user = UserContextHolder.getCurrentUser();
        if (user == null) {
            return Result.error(401, "用户未登录");
        }
        user.setPassword(null);
        return Result.success(user);
    }
}