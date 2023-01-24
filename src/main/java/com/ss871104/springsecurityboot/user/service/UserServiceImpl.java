package com.ss871104.springsecurityboot.user.service;

import com.ss871104.springsecurityboot.user.domain.User;
import com.ss871104.springsecurityboot.user.dto.UserResponse;
import com.ss871104.springsecurityboot.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
// cacheNames 與 value 定義一样，若設置了 value 的值，cacheNames 配置就無效
@CacheConfig(cacheNames = "userService")
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Cacheable(value="userList", keyGenerator = "wiselyKeyGenerator")
    public List<UserResponse> getAll() {
        System.out.println("此方法被使用");
        List<User> userList = userRepository.findAll();

        return userList.stream().map(x ->
            UserResponse.builder()
                    .id(x.getId())
                    .name(x.getName())
                    .username(x.getUsername())
                    .email(x.getEmail())
                    .role(x.getRole())
                    .build())
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value="user", key ="#userId")
    public UserResponse getByUserId(Long userId) {
        System.out.println("此方法被使用");
        User user = userRepository.findById(userId)
                .orElseThrow();

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    @CacheEvict(value = "userList", allEntries=true)
    public void clearAllUserCache() {

    }

    @CacheEvict(value="user", key ="#userId")
    public void clearUserCacheById(Long userId) {

    }
}
