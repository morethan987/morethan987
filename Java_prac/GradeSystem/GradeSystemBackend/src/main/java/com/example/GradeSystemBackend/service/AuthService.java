package com.example.GradeSystemBackend.service;

import com.example.GradeSystemBackend.domain.auth.Role;
import com.example.GradeSystemBackend.domain.auth.RoleConstants;
import com.example.GradeSystemBackend.domain.auth.User;
import com.example.GradeSystemBackend.domain.info.UserProfile;
import com.example.GradeSystemBackend.dto.AuthResponse;
import com.example.GradeSystemBackend.dto.RegisterRequest;
import com.example.GradeSystemBackend.dto.UserBasicInfo;
import com.example.GradeSystemBackend.repository.RoleRepository;
import com.example.GradeSystemBackend.repository.UserProfileRepository;
import com.example.GradeSystemBackend.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(
        AuthService.class
    );

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     */
    public AuthResponse register(RegisterRequest request) {
        try {
            // 1. 验证密码是否一致
            if (!request.isPasswordMatch()) {
                logger.warn(
                    "注册失败：密码与确认密码不一致，用户名：{}",
                    request.getUsername()
                );
                return AuthResponse.error("密码与确认密码不一致");
            }

            // 2. 验证角色是否有效
            if (!request.isValidRole()) {
                logger.warn(
                    "注册失败：无效角色 {}，用户名：{}",
                    request.getRole(),
                    request.getUsername()
                );
                return AuthResponse.error("无效的角色类型");
            }

            // 3. 检查用户名是否已存在
            if (userRepository.existsByUsername(request.getUsername())) {
                logger.warn(
                    "注册失败：用户名已存在，用户名：{}",
                    request.getUsername()
                );
                return AuthResponse.error("用户名已存在");
            }

            // 4. 检查邮箱是否已存在（如果提供了邮箱）
            if (StringUtils.hasText(request.getEmail())) {
                Optional<UserProfile> existingProfile =
                    userProfileRepository.findByEmail(request.getEmail());
                if (existingProfile.isPresent()) {
                    logger.warn(
                        "注册失败：邮箱已被使用，邮箱：{}",
                        request.getEmail()
                    );
                    return AuthResponse.error("邮箱已被使用");
                }
            }

            // 5. 检查手机号是否已存在（如果提供了手机号）
            if (StringUtils.hasText(request.getPhone())) {
                Optional<UserProfile> existingProfile =
                    userProfileRepository.findByPhone(request.getPhone());
                if (existingProfile.isPresent()) {
                    logger.warn(
                        "注册失败：手机号已被使用，手机号：{}",
                        request.getPhone()
                    );
                    return AuthResponse.error("手机号已被使用");
                }
            }

            // 6. 创建用户
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEnabled(true);

            // 7. 分配角色
            String roleName = request.getValidRole();
            Role assignedRole = assignUserRole(user, roleName);
            if (assignedRole == null) {
                logger.error(
                    "注册失败：角色分配失败，角色：{}，用户名：{}",
                    roleName,
                    request.getUsername()
                );
                return AuthResponse.error("角色分配失败，请联系管理员");
            }

            // 8. 保存用户
            user = userRepository.save(user);

            // 9. 创建用户档案
            if (
                StringUtils.hasText(request.getRealName()) ||
                StringUtils.hasText(request.getEmail()) ||
                StringUtils.hasText(request.getPhone())
            ) {
                UserProfile userProfile = new UserProfile();
                userProfile.setUser(user);
                userProfile.setRealName(request.getRealName());
                userProfile.setEmail(request.getEmail());
                userProfile.setPhone(request.getPhone());
                userProfile.setCreatedAt(LocalDateTime.now());
                userProfile.setUpdatedAt(LocalDateTime.now());

                userProfileRepository.save(userProfile);
            }

            // 10. 构建响应
            UserBasicInfo userInfo = new UserBasicInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setEnabled(user.isEnabled());
            userInfo.setRoles(getUserRoleNames(user));
            userInfo.setUiType(user.getUiType());
            userInfo.setRealName(request.getRealName());
            userInfo.setEmail(request.getEmail());

            logger.info(
                "用户注册成功：用户名：{}，角色：{}",
                user.getUsername(),
                roleName
            );
            return AuthResponse.success("注册成功", userInfo);
        } catch (Exception e) {
            logger.error("用户注册失败：{}", e.getMessage(), e);
            return AuthResponse.error("注册失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前登录用户信息
     */
    @Transactional(readOnly = true)
    public AuthResponse getCurrentUser() {
        try {
            Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return AuthResponse.error("用户未登录");
            }

            String username = authentication.getName();

            // 查找用户
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (!userOpt.isPresent()) {
                return AuthResponse.error("用户不存在");
            }

            User user = userOpt.get();

            // 查找用户档案
            Optional<UserProfile> profileOpt = userProfileRepository.findByUser(
                user
            );

            // 构建用户信息
            UserBasicInfo userInfo = new UserBasicInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setEnabled(user.isEnabled());
            userInfo.setRoles(getUserRoleNames(user));
            userInfo.setUiType(user.getUiType());

            if (profileOpt.isPresent()) {
                UserProfile profile = profileOpt.get();
                userInfo.setRealName(profile.getRealName());
                userInfo.setEmail(profile.getEmail());
                userInfo.setAvatarUrl(profile.getAvatarUrl());
            }

            return AuthResponse.success("获取用户信息成功", userInfo);
        } catch (Exception e) {
            return AuthResponse.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户名是否可用
     */
    @Transactional(readOnly = true)
    public AuthResponse checkUsernameAvailable(String username) {
        try {
            boolean exists = userRepository.existsByUsername(username);
            if (exists) {
                return AuthResponse.error("用户名已存在");
            } else {
                return AuthResponse.success("用户名可用");
            }
        } catch (Exception e) {
            return AuthResponse.error("检查用户名失败: " + e.getMessage());
        }
    }

    /**
     * 检查邮箱是否可用
     */
    @Transactional(readOnly = true)
    public AuthResponse checkEmailAvailable(String email) {
        try {
            Optional<UserProfile> profile = userProfileRepository.findByEmail(
                email
            );
            if (profile.isPresent()) {
                return AuthResponse.error("邮箱已被使用");
            } else {
                return AuthResponse.success("邮箱可用");
            }
        } catch (Exception e) {
            return AuthResponse.error("检查邮箱失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的角色名称集合
     */
    private Set<String> getUserRoleNames(User user) {
        if (user.getRoles() == null) {
            return new HashSet<>();
        }
        return user
            .getRoles()
            .stream()
            .map(Role::getName)
            .collect(Collectors.toSet());
    }

    /**
     * 验证当前用户是否已登录
     */
    @Transactional(readOnly = true)
    public boolean isCurrentUserAuthenticated() {
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();
        return (
            authentication != null &&
            authentication.isAuthenticated() &&
            !"anonymousUser".equals(authentication.getName())
        );
    }

    /**
     * 获取当前登录用户的用户名
     */
    @Transactional(readOnly = true)
    public String getCurrentUsername() {
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * 获取当前登录用户的User对象
     */
    @Transactional(readOnly = true)
    public Optional<User> getCurrentUserEntity() {
        String username = getCurrentUsername();
        if (username != null) {
            return userRepository.findByUsername(username);
        }
        return Optional.empty();
    }

    /**
     * 为用户分配角色
     * @param user 用户对象
     * @param roleName 角色名称
     * @return 分配的角色对象，如果失败则返回null
     */
    private Role assignUserRole(User user, String roleName) {
        try {
            Optional<Role> roleOpt = roleRepository.findByName(roleName);

            if (roleOpt.isPresent()) {
                Role role = roleOpt.get();
                Set<Role> roles = new HashSet<>();
                roles.add(role);
                user.setRoles(roles);
                logger.debug(
                    "成功为用户 {} 分配角色 {}",
                    user.getUsername(),
                    roleName
                );
                return role;
            } else {
                // 如果指定角色不存在，尝试分配默认角色
                logger.warn(
                    "角色 {} 不存在，尝试分配默认角色 {}",
                    roleName,
                    RoleConstants.DEFAULT_ROLE
                );

                Optional<Role> defaultRoleOpt = roleRepository.findByName(
                    RoleConstants.DEFAULT_ROLE
                );
                if (defaultRoleOpt.isPresent()) {
                    Role defaultRole = defaultRoleOpt.get();
                    Set<Role> roles = new HashSet<>();
                    roles.add(defaultRole);
                    user.setRoles(roles);
                    logger.info(
                        "成功为用户 {} 分配默认角色 {}",
                        user.getUsername(),
                        RoleConstants.DEFAULT_ROLE
                    );
                    return defaultRole;
                } else {
                    logger.error(
                        "默认角色 {} 也不存在，角色分配失败",
                        RoleConstants.DEFAULT_ROLE
                    );
                    return null;
                }
            }
        } catch (Exception e) {
            logger.error("角色分配过程中发生错误：{}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 检查角色是否存在
     * @param roleName 角色名称
     * @return 角色是否存在
     */
    @Transactional(readOnly = true)
    public boolean roleExists(String roleName) {
        return roleRepository.findByName(roleName).isPresent();
    }

    /**
     * 获取所有可用角色
     * @return 角色名称集合
     */
    @Transactional(readOnly = true)
    public Set<String> getAvailableRoles() {
        return roleRepository
            .findAll()
            .stream()
            .map(Role::getName)
            .collect(Collectors.toSet());
    }

    /**
     * 验证注册请求的完整性
     * @param request 注册请求
     * @return 验证结果消息，如果验证通过返回null
     */
    public String validateRegisterRequest(RegisterRequest request) {
        if (!request.isPasswordMatch()) {
            return "密码与确认密码不一致";
        }

        if (!request.isValidRole()) {
            return (
                "无效的角色类型，可用角色：" +
                String.join(", ", getAvailableRoles())
            );
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            return "用户名已存在";
        }

        if (
            StringUtils.hasText(request.getEmail()) &&
            userProfileRepository.findByEmail(request.getEmail()).isPresent()
        ) {
            return "邮箱已被使用";
        }

        if (
            StringUtils.hasText(request.getPhone()) &&
            userProfileRepository.findByPhone(request.getPhone()).isPresent()
        ) {
            return "手机号已被使用";
        }

        return null; // 验证通过
    }
}
