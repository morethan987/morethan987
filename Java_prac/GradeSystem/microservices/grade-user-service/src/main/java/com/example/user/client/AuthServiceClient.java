package com.example.user.client;

import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "grade-auth-service", path = "/api/v1")
public interface AuthServiceClient {

    @GetMapping("/auth/user/{userId}")
    UserDTO getUserById(@PathVariable("userId") UUID userId);

    @GetMapping("/auth/user/by-username/{username}")
    UserDTO getUserByUsername(@PathVariable("username") String username);

    @PostMapping("/auth/validate-password")
    PasswordValidationResponse validatePassword(@RequestBody PasswordValidationRequest request);

    @PutMapping("/auth/user/{userId}/password")
    void changePassword(@PathVariable("userId") UUID userId, @RequestBody ChangePasswordRequest request);

    @PutMapping("/auth/user/{userId}/username")
    UserDTO changeUsername(@PathVariable("userId") UUID userId, @RequestBody ChangeUsernameRequest request);

    @GetMapping("/auth/user/exists/{username}")
    ExistsResponse usernameExists(@PathVariable("username") String username);

    @PutMapping("/auth/user/{userId}/status")
    UserDTO updateUserStatus(@PathVariable("userId") UUID userId, @RequestParam("enabled") boolean enabled);
}
