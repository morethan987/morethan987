package com.example.GradeSystemBackend.service;

import com.example.GradeSystemBackend.domain.auth.User;
import com.example.GradeSystemBackend.domain.info.UserProfile;
import com.example.GradeSystemBackend.dto.*;
import com.example.GradeSystemBackend.repository.UserProfileRepository;
import com.example.GradeSystemBackend.repository.UserRepository;
import java.beans.PropertyDescriptor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 根据ID获取用户资料信息
     */
    public UserProfileDTO getUserProfile(UUID userId) {
        Optional<UserProfile> profile = userProfileRepository.findByUserId(
            userId
        );
        if (profile.isEmpty()) {
            throw new RuntimeException(
                "User Profile not found with id: " + userId
            );
        }
        return new UserProfileDTO(profile.get());
    }

    /**
     * 根据用户名获取用户资料信息
     */
    public UserProfileDTO getUserProfileByUsername(String username) {
        Optional<UserProfile> profile = userProfileRepository.findByUsername(
            username
        );
        if (profile.isEmpty()) {
            throw new RuntimeException(
                "User Profile not found with username: " + username
            );
        }
        return new UserProfileDTO(profile.get());
    }

    /**
     * 更新用户资料信息
     */
    public UserProfileDTO updateUserProfile(
        UUID userId,
        UserProfileDTO request
    ) {
        UserProfile userProfile = userProfileRepository
            .findByUserId(userId)
            .orElseThrow(() ->
                new RuntimeException(
                    "User Profile not found with id: " + userId
                )
            );

        BeanUtils.copyProperties(
            request,
            userProfile,
            getNullPropertyNames(request)
        );

        userProfile.setUpdatedAt(LocalDateTime.now());
        userProfileRepository.save(userProfile);
        return new UserProfileDTO(userProfile);
    }

    /**
     * 修改用户名
     */
    public UserDTO changeUsername(UUID userId, ChangeUsernameRequest request) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with id: " + userId);
        }

        // 检查密码
        User user = userOpt.get();
        if (
            !passwordEncoder.matches(request.getPassword(), user.getPassword())
        ) {
            throw new RuntimeException("Incorrect password");
        }

        if (
            request.getNewUsername() == null ||
            request.getNewUsername().trim().isEmpty()
        ) {
            throw new RuntimeException("New username cannot be empty");
        }

        if (userRepository.existsByUsername(request.getNewUsername())) {
            throw new RuntimeException(
                "Username already exists: " + request.getNewUsername()
            );
        }

        user.setUsername(request.getNewUsername());
        User savedUser = userRepository.save(user);
        return new UserDTO(savedUser);
    }

    /**
     * 修改密码
     */
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        // 验证两次新密码是否匹配
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new RuntimeException("New passwords do not match");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with id: " + userId);
        }

        User user = userOpt.get();

        // 验证当前密码
        if (
            !passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword()
            )
        ) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (
            request.getNewPassword() == null ||
            request.getNewPassword().length() < 6
        ) {
            throw new RuntimeException(
                "New password must be at least 6 characters long"
            );
        }

        // 加密新密码并保存
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    }

    /**
     * 获取所有用户列表
     */
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAllOrderByUsernameAsc();
        return users
            .stream()
            .map(user -> new UserDTO(user))
            .collect(Collectors.toList());
    }

    /**
     * 根据用户名模糊查询用户
     */
    public List<UserDTO> searchUsersByUsername(String username) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(
            username
        );
        return users
            .stream()
            .map(user -> new UserDTO(user))
            .collect(Collectors.toList());
    }

    /**
     * 获取已启用的用户列表
     */
    public List<UserDTO> getEnabledUsers() {
        List<User> users = userRepository.findByEnabledTrue();
        return users
            .stream()
            .map(user -> new UserDTO(user))
            .collect(Collectors.toList());
    }

    /**
     * 获取已禁用的用户列表
     */
    public List<UserDTO> getDisabledUsers() {
        List<User> users = userRepository.findByEnabledFalse();
        return users
            .stream()
            .map(user -> new UserDTO(user))
            .collect(Collectors.toList());
    }

    /**
     * 启用用户
     */
    public UserDTO enableUser(UUID userId) {
        return updateUserStatus(userId, true);
    }

    /**
     * 禁用用户
     */
    public UserDTO disableUser(UUID userId) {
        return updateUserStatus(userId, false);
    }

    /**
     * 更新用户启用状态
     */
    private UserDTO updateUserStatus(UUID userId, boolean enabled) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with id: " + userId);
        }

        User user = userOpt.get();
        user.setEnabled(enabled);
        User savedUser = userRepository.save(user);
        return new UserDTO(savedUser);
    }

    /**
     * 检查用户名是否存在
     */
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public static String[] getNullPropertyNames(Object source) {
        BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> nullNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            String propName = pd.getName();
            Object srcValue = src.getPropertyValue(propName);
            if (srcValue == null) {
                nullNames.add(propName);
            }
        }
        return nullNames.toArray(new String[0]);
    }
}
