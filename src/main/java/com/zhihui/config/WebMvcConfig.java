package com.zhihui.config;

import com.zhihui.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**")              // 拦截所有 /api/**
                .excludePathPatterns(
                        "/api/auth/login",               // 放行登录
                        "/api/auth/register",            // 放行注册
                        "/api/hello"                    // 放行测试接口
                );
    }
}
