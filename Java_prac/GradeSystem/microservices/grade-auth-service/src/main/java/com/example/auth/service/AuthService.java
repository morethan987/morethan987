package com.example.auth.service;

import com.example.auth.domain.Role;
import com.example.auth.domain.RoleConstants;
import com.example.auth.domain.User;
import com.example.auth.dto.AuthResponse;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.dto.UserBasicInfo;
import com.example.auth.repository.RoleRepository;
import com.example.auth.repository.UserRepository;
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

@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthResponse register(RegisterRequest request) {
        try {
            if (!request.isPasswordMatch()) {
                logger.warn("Registration failed: passwords do not match for user: {}", request.getUsername());
                return AuthResponse.error("Passwords do not match");
            }

            if (!request.isValidRole()) {
                logger.warn("Registration failed: invalid role {} for user: {}", request.getRole(), request.getUsername());
                return AuthResponse.error("Invalid role type");
            }

            if (userRepository.existsByUsername(request.getUsername())) {
                logger.warn("Registration failed: username already exists: {}", request.getUsername());
                return AuthResponse.error("Username already exists");
            }

            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEnabled(true);

            String roleName = request.getValidRole();
            Role assignedRole = assignUserRole(user, roleName);
            if (assignedRole == null) {
                logger.error("Registration failed: role assignment failed for role: {}, user: {}", roleName, request.getUsername());
                return AuthResponse.error("Role assignment failed, please contact administrator");
            }

            user = userRepository.save(user);

            UserBasicInfo userInfo = new UserBasicInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setEnabled(user.isEnabled());
            userInfo.setRoles(getUserRoleNames(user));
            userInfo.setUiType(user.getUiType());
            userInfo.setRealName(request.getRealName());
            userInfo.setEmail(request.getEmail());

            logger.info("User registered successfully: username: {}, role: {}", user.getUsername(), roleName);
            return AuthResponse.success("Registration successful", userInfo);
        } catch (Exception e) {
            logger.error("User registration failed: {}", e.getMessage(), e);
            return AuthResponse.error("Registration failed: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return AuthResponse.error("User not logged in");
            }

            String username = authentication.getName();

            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                return AuthResponse.error("User not found");
            }

            User user = userOpt.get();

            UserBasicInfo userInfo = new UserBasicInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setEnabled(user.isEnabled());
            userInfo.setRoles(getUserRoleNames(user));
            userInfo.setUiType(user.getUiType());

            return AuthResponse.success("User info retrieved successfully", userInfo);
        } catch (Exception e) {
            return AuthResponse.error("Failed to get user info: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse checkUsernameAvailable(String username) {
        try {
            boolean exists = userRepository.existsByUsername(username);
            if (exists) {
                return AuthResponse.error("Username already exists");
            } else {
                return AuthResponse.success("Username is available");
            }
        } catch (Exception e) {
            return AuthResponse.error("Failed to check username: " + e.getMessage());
        }
    }

    private Set<String> getUserRoleNames(User user) {
        if (user.getRoles() == null) {
            return new HashSet<>();
        }
        return user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public boolean isCurrentUserAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
               authentication.isAuthenticated() &&
               !"anonymousUser".equals(authentication.getName());
    }

    @Transactional(readOnly = true)
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }

    @Transactional(readOnly = true)
    public Optional<User> getCurrentUserEntity() {
        String username = getCurrentUsername();
        if (username != null) {
            return userRepository.findByUsername(username);
        }
        return Optional.empty();
    }

    private Role assignUserRole(User user, String roleName) {
        try {
            Optional<Role> roleOpt = roleRepository.findByName(roleName);

            if (roleOpt.isPresent()) {
                Role role = roleOpt.get();
                Set<Role> roles = new HashSet<>();
                roles.add(role);
                user.setRoles(roles);
                logger.debug("Successfully assigned role {} to user {}", roleName, user.getUsername());
                return role;
            } else {
                logger.warn("Role {} not found, attempting to assign default role {}", roleName, RoleConstants.DEFAULT_ROLE);

                Optional<Role> defaultRoleOpt = roleRepository.findByName(RoleConstants.DEFAULT_ROLE);
                if (defaultRoleOpt.isPresent()) {
                    Role defaultRole = defaultRoleOpt.get();
                    Set<Role> roles = new HashSet<>();
                    roles.add(defaultRole);
                    user.setRoles(roles);
                    logger.info("Successfully assigned default role {} to user {}", RoleConstants.DEFAULT_ROLE, user.getUsername());
                    return defaultRole;
                } else {
                    logger.error("Default role {} also not found, role assignment failed", RoleConstants.DEFAULT_ROLE);
                    return null;
                }
            }
        } catch (Exception e) {
            logger.error("Error during role assignment: {}", e.getMessage(), e);
            return null;
        }
    }

    @Transactional(readOnly = true)
    public boolean roleExists(String roleName) {
        return roleRepository.findByName(roleName).isPresent();
    }

    @Transactional(readOnly = true)
    public Set<String> getAvailableRoles() {
        return roleRepository.findAll().stream()
            .map(Role::getName)
            .collect(Collectors.toSet());
    }

    public String validateRegisterRequest(RegisterRequest request) {
        if (!request.isPasswordMatch()) {
            return "Passwords do not match";
        }

        if (!request.isValidRole()) {
            return "Invalid role type, available roles: " + String.join(", ", getAvailableRoles());
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            return "Username already exists";
        }

        return null;
    }
}
