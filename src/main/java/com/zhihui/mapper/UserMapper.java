package com.zhihui.mapper;

import com.zhihui.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

// mapper/UserMapper.java
@Mapper
public interface UserMapper {
    int insert(User user);
    User selectById(Long id);
    User selectByUsername(String username);
    List<User> selectAll();
    int update(User user);
    int deleteById(Long id);
}
