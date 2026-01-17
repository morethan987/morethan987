package com.example.user.controller;

import com.example.user.dto.UserProfileDTO;
import com.example.user.service.UserProfileService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user-profile")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('user_profile:view', 'admin:all')")
    public ResponseEntity<?> getUserProfile(@PathVariable UUID userId) {
        try {
            UserProfileDTO profile = userProfileService.getUserProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/by-id/{profileId}")
    @PreAuthorize("hasAnyAuthority('user_profile:view', 'admin:all')")
    public ResponseEntity<?> getUserProfileById(@PathVariable UUID profileId) {
        try {
            UserProfileDTO profile = userProfileService.getUserProfileById(profileId);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('user_profile:update', 'admin:all')")
    public ResponseEntity<?> updateUserProfile(@PathVariable UUID userId, @RequestBody UserProfileDTO request) {
        try {
            UserProfileDTO profileDTO = userProfileService.updateUserProfile(userId, request);
            return ResponseEntity.ok(profileDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('user_profile:create', 'admin:all')")
    public ResponseEntity<?> createUserProfile(@PathVariable UUID userId, @RequestBody(required = false) UserProfileDTO request) {
        try {
            UserProfileDTO profileDTO = userProfileService.createUserProfile(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(profileDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('user_profile:view', 'admin:all')")
    public ResponseEntity<List<UserProfileDTO>> getAllUserProfiles() {
        List<UserProfileDTO> profiles = userProfileService.getAllUserProfiles();
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('user_profile:view', 'admin:all')")
    public ResponseEntity<List<UserProfileDTO>> searchByRealName(@RequestParam String realName) {
        List<UserProfileDTO> profiles = userProfileService.searchByRealName(realName);
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/by-email/{email}")
    @PreAuthorize("hasAnyAuthority('user_profile:view', 'admin:all')")
    public ResponseEntity<?> getUserProfileByEmail(@PathVariable String email) {
        try {
            UserProfileDTO profile = userProfileService.getUserProfileByEmail(email);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/by-phone/{phone}")
    @PreAuthorize("hasAnyAuthority('user_profile:view', 'admin:all')")
    public ResponseEntity<?> getUserProfileByPhone(@PathVariable String phone) {
        try {
            UserProfileDTO profile = userProfileService.getUserProfileByPhone(phone);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAnyAuthority('user_profile:view', 'admin:all')")
    public ResponseEntity<List<UserProfileDTO>> getRecentlyCreatedProfiles(@RequestParam(defaultValue = "7") int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<UserProfileDTO> profiles = userProfileService.getRecentlyCreatedProfiles(since);
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyAuthority('user_profile:view', 'admin:all')")
    public ResponseEntity<Map<String, Long>> getTotalUserProfiles() {
        long count = userProfileService.getTotalUserProfiles();
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/exists/{userId}")
    @PreAuthorize("hasAnyAuthority('user_profile:view', 'admin:all')")
    public ResponseEntity<Map<String, Boolean>> existsByUserId(@PathVariable UUID userId) {
        boolean exists = userProfileService.existsByUserId(userId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('user_profile:delete', 'admin:all')")
    public ResponseEntity<?> deleteUserProfile(@PathVariable UUID userId) {
        try {
            userProfileService.deleteUserProfile(userId);
            return ResponseEntity.ok(createSuccessResponse("User profile deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            Map.of(
                "error", "Access denied",
                "message", "You don't have permission to access this resource: " + e.getMessage(),
                "timestamp", System.currentTimeMillis()
            )
        );
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }

    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}
