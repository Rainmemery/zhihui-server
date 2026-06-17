package com.zhihui.mapper;

import com.zhihui.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.AssertionsKt.assertNotNull;

@SpringBootTest
class UserMapperTest {
    @Autowired
    UserMapper userMapper;

    @Test
    void testInsertAndSelect() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("123456");
        userMapper.insert(user);
        assertNotNull(user.getId()); // 主键回填

        User found = userMapper.selectById(user.getId());
        assertEquals("testuser", found.getUsername());
    }
}
