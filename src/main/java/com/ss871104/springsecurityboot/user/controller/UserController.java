package com.ss871104.springsecurityboot.user.controller;

import com.ss871104.springsecurityboot.user.dto.UserResponse;
import com.ss871104.springsecurityboot.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{userId}")
    public UserResponse getByUserId(@PathVariable Long userId) {
        return userService.getByUserId(userId);
    }

    @GetMapping("/getAllUsers")
    public List<UserResponse> getAllUsers() {
        return userService.getAll();
    }

    @GetMapping("/clear/allUsers")
    public void clearUserCacheById() {
        userService.clearAllUserCache();
    }

    @GetMapping("/clear/{userId}")
    public void clearUserCacheById(@PathVariable Long userId) {
        userService.clearUserCacheById(userId);
    }

    @GetMapping("/testAccessDenied")
    public void testAccessDenied() {
    }
}
