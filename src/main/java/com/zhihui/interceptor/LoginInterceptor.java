package com.zhihui.interceptor;

import com.zhihui.common.JwtUtils;
import com.zhihui.common.RedisUtils;
import com.zhihui.common.UserContextHolder;
import com.zhihui.entity.User;
import com.zhihui.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired private JwtUtils jwtUtils;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisUtils redisUtils;

    private static final String USER_CACHE_PREFIX = "user:info:";
    private static final long USER_CACHE_TTL = 30; // 30分钟

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\"}");
            return false;
        }
        String token = authHeader.substring(7);

        if (!jwtUtils.validateToken(token)) {
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"token无效或已过期\"}");
            return false;
        }

        Long userId = jwtUtils.getUserIdFromToken(token);
        User user = getUserFromCache(userId);
        UserContextHolder.setCurrentUser(user);

        return true;
    }

    private User getUserFromCache(Long userId) {
        String cacheKey = USER_CACHE_PREFIX + userId;
        User user = redisUtils.get(cacheKey);
        if (user != null) {
            return user;
        }
        user = userMapper.selectById(userId);
        if (user != null) {
            redisUtils.set(cacheKey, user, USER_CACHE_TTL * 60);
        }
        return user;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response, Object handler, Exception ex) {
        UserContextHolder.clear();
    }
}