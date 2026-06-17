package com.zhihui.interceptor;

import com.zhihui.common.JwtUtils;
import com.zhihui.common.UserContextHolder;
import com.zhihui.entity.User;
import com.zhihui.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired private JwtUtils jwtUtils;
    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws IOException {
        // 1. 取 token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\"}");
            return false;
        }
        String token = authHeader.substring(7);

        // 2. 验证 token
        if (!jwtUtils.validateToken(token)) {
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"token无效或已过期\"}");
            return false;
        }

        // 3. 解析用户并存入 ThreadLocal
        Long userId = jwtUtils.getUserIdFromToken(token);
        User user = userMapper.selectById(userId);
        UserContextHolder.setCurrentUser(user);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response, Object handler, Exception ex) {
        UserContextHolder.clear(); // 🔑 防止内存泄漏
    }
}
