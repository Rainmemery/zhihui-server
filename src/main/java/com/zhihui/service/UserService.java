package com.zhihui.service;

import com.zhihui.dto.UserLoginDTO;
import com.zhihui.dto.UserRegisterDTO;
import com.zhihui.entity.User;
import com.zhihui.vo.LoginVO;
import org.springframework.transaction.annotation.Transactional;

public interface UserService {
    User register(UserRegisterDTO dto);
    LoginVO login(UserLoginDTO dto);
    User getById(Long id);
    User update(User user);
    User delete(Long id);
    User getByUsername(String username);
}
