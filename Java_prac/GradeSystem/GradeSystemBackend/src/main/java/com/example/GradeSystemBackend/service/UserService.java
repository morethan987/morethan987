package com.example.GradeSystemBackend.service;

import com.example.GradeSystemBackend.domain.auth.Role;
import com.example.GradeSystemBackend.domain.auth.User;
import com.example.GradeSystemBackend.dto.*;
import com.example.GradeSystemBackend.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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
    private PasswordEncoder passwordEncoder;

    /**
     * 根据ID获取用户资料信息
     */
    public UserDTO getUserProfile(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        return convertToDTO(userOpt.get());
    }

    /**
     * 根据用户名获取用户资料信息
     */
    public UserDTO getUserProfileByUsername(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException(
                "User not found with username: " + username
            );
        }
        return convertToDTO(userOpt.get());
    }

    /**
     * 更新用户资料信息
     */
    public UserDTO updateUserProfile(
        UUID userId,
        UpdateUserProfileRequest request
    ) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with id: " + userId);
        }

        User user = userOpt.get();

        // 如果要更新用户名，检查新用户名是否已存在
        if (
            request.getUsername() != null &&
            !request.getUsername().equals(user.getUsername())
        ) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException(
                    "Username already exists: " + request.getUsername()
                );
            }
            user.setUsername(request.getUsername());
        }

        // 更新启用状态
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    /**
     * 修改用户名
     */
    public UserDTO changeUsername(UUID userId, ChangeUsernameRequest request) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with id: " + userId);
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

        User user = userOpt.get();
        user.setUsername(request.getNewUsername());
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    /**
     * 修改密码
     */
    public void changePassword(UUID userId, ChangePasswordRequest request) {
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
        userRepository.save(user);
    }

    /**
     * 获取所有用户列表
     */
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAllOrderByUsernameAsc();
        return users
            .stream()
            .map(this::convertToDTO)
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
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * 获取已启用的用户列表
     */
    public List<UserDTO> getEnabledUsers() {
        List<User> users = userRepository.findByEnabledTrue();
        return users
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * 获取已禁用的用户列表
     */
    public List<UserDTO> getDisabledUsers() {
        List<User> users = userRepository.findByEnabledFalse();
        return users
            .stream()
            .map(this::convertToDTO)
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
        return convertToDTO(savedUser);
    }

    /**
     * 检查用户名是否存在
     */
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 将User实体转换为UserDTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEnabled(user.isEnabled());

        // 转换角色信息
        if (user.getRoles() != null) {
            Set<String> roleNames = user
                .getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
            dto.setRoles(roleNames);
        }

        return dto;
    }
}
