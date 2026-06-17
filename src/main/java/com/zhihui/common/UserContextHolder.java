package com.zhihui.common;

import com.zhihui.entity.User;

public class UserContextHolder {
    private static final ThreadLocal<User> CURRENT_USER = new ThreadLocal<>();

    public static void setCurrentUser(User user) { CURRENT_USER.set(user); }
    public static User getCurrentUser() { return CURRENT_USER.get(); }
    public static Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }
    public static void clear() { CURRENT_USER.remove(); }
}
