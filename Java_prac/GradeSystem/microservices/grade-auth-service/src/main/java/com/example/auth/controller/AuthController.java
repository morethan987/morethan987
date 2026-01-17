package com.example.auth.controller;

import com.example.auth.dto.AuthResponse;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SecurityContextRepository securityContextRepository;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
        @Valid @RequestBody LoginRequest req,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        try {
            UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword());

            Authentication auth = authenticationManager.authenticate(token);

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);

            securityContextRepository.saveContext(context, request, response);

            logger.debug("User {} logged in successfully", req.getUsername());

            return ResponseEntity.ok(authService.getCurrentUser());
        } catch (Exception e) {
            logger.warn("Login failed for user {}: {}", req.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(AuthResponse.error("Login failed: Invalid username or password"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        logger.debug("User logged out successfully");
        return ResponseEntity.ok(AuthResponse.success("Logout successful"));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
        @Valid @RequestBody RegisterRequest request,
        BindingResult bindingResult
    ) {
        try {
            if (bindingResult.hasErrors()) {
                StringBuilder errorMsg = new StringBuilder();
                bindingResult.getFieldErrors().forEach(error -> {
                    errorMsg.append(error.getDefaultMessage()).append("; ");
                });
                return ResponseEntity.badRequest()
                    .body(AuthResponse.error("Validation failed: " + errorMsg.toString()));
            }

            String validationError = authService.validateRegisterRequest(request);
            if (validationError != null) {
                return ResponseEntity.badRequest().body(AuthResponse.error(validationError));
            }

            AuthResponse response = authService.register(request);

            if (response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AuthResponse.error("Registration error: " + e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser() {
        try {
            AuthResponse response = authService.getCurrentUser();

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AuthResponse.error("Failed to get user info: " + e.getMessage()));
        }
    }

    @GetMapping("/check-username")
    public ResponseEntity<AuthResponse> checkUsername(@RequestParam String username) {
        try {
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(AuthResponse.error("Username cannot be empty"));
            }

            AuthResponse response = authService.checkUsernameAvailable(username.trim());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AuthResponse.error("Failed to check username: " + e.getMessage()));
        }
    }

    @GetMapping("/roles")
    public ResponseEntity<AuthResponse> getAvailableRoles() {
        try {
            var roles = authService.getAvailableRoles();
            return ResponseEntity.ok(AuthResponse.success("Roles retrieved successfully").setData(roles));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AuthResponse.error("Failed to get roles: " + e.getMessage()));
        }
    }

    @GetMapping("/check-role")
    public ResponseEntity<AuthResponse> checkRole(@RequestParam String role) {
        try {
            if (role == null || role.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(AuthResponse.error("Role cannot be empty"));
            }

            boolean exists = authService.roleExists(role.trim());
            if (exists) {
                return ResponseEntity.ok(AuthResponse.success("Role exists"));
            } else {
                return ResponseEntity.ok(
                    AuthResponse.error("Role does not exist, available roles: " +
                        String.join(", ", authService.getAvailableRoles()))
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AuthResponse.error("Failed to check role: " + e.getMessage()));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<AuthResponse> getAuthStatus() {
        try {
            boolean isAuthenticated = authService.isCurrentUserAuthenticated();

            if (isAuthenticated) {
                return ResponseEntity.ok(AuthResponse.success("User is logged in"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.error("User is not logged in"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AuthResponse.error("Failed to check auth status: " + e.getMessage()));
        }
    }

    @GetMapping("/username")
    public ResponseEntity<AuthResponse> getCurrentUsername() {
        try {
            String username = authService.getCurrentUsername();

            if (username != null) {
                return ResponseEntity.ok(AuthResponse.success("Current username: " + username));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.error("User is not logged in"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AuthResponse.error("Failed to get username: " + e.getMessage()));
        }
    }
}
