package com.ss871104.springsecurityboot.user.service;

import com.ss871104.springsecurityboot.user.dto.UserResponse;

import java.util.List;

public interface UserService {
    public List<UserResponse> getAll();
    public UserResponse getByUserId(Long userId);
    public void clearAllUserCache();
    public void clearUserCacheById(Long userId);
}
