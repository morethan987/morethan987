package com.example.user.service;

import com.example.user.domain.UserProfile;
import com.example.user.dto.UserProfileDTO;
import com.example.user.repository.UserProfileRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserProfileService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    public UserProfileDTO getUserProfile(UUID userId) {
        Optional<UserProfile> profile = userProfileRepository.findByUserId(userId);
        if (profile.isEmpty()) {
            throw new RuntimeException("User Profile not found with user id: " + userId);
        }
        return new UserProfileDTO(profile.get());
    }

    public UserProfileDTO getUserProfileById(UUID profileId) {
        return userProfileRepository.findById(profileId)
            .map(UserProfileDTO::new)
            .orElseThrow(() -> new RuntimeException("User Profile not found with id: " + profileId));
    }

    public UserProfileDTO updateUserProfile(UUID userId, UserProfileDTO request) {
        UserProfile userProfile = userProfileRepository
            .findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("User Profile not found with user id: " + userId));

        BeanUtils.copyProperties(request, userProfile, getNullPropertyNames(request));

        userProfile.setUpdatedAt(LocalDateTime.now());
        userProfileRepository.save(userProfile);
        return new UserProfileDTO(userProfile);
    }

    public UserProfileDTO createUserProfile(UUID userId, UserProfileDTO request) {
        if (userProfileRepository.existsByUserId(userId)) {
            throw new RuntimeException("User Profile already exists for user id: " + userId);
        }

        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(userId);
        
        if (request != null) {
            BeanUtils.copyProperties(request, userProfile, getNullPropertyNames(request));
        }
        
        userProfile.setCreatedAt(LocalDateTime.now());
        userProfile.setUpdatedAt(LocalDateTime.now());
        
        userProfileRepository.save(userProfile);
        return new UserProfileDTO(userProfile);
    }

    public List<UserProfileDTO> getAllUserProfiles() {
        return userProfileRepository.findAllOrderByRealNameAsc()
            .stream()
            .map(UserProfileDTO::new)
            .collect(Collectors.toList());
    }

    public List<UserProfileDTO> searchByRealName(String realName) {
        return userProfileRepository.findByRealNameContainingIgnoreCase(realName)
            .stream()
            .map(UserProfileDTO::new)
            .collect(Collectors.toList());
    }

    public UserProfileDTO getUserProfileByEmail(String email) {
        return userProfileRepository.findByEmail(email)
            .map(UserProfileDTO::new)
            .orElseThrow(() -> new RuntimeException("User Profile not found with email: " + email));
    }

    public UserProfileDTO getUserProfileByPhone(String phone) {
        return userProfileRepository.findByPhone(phone)
            .map(UserProfileDTO::new)
            .orElseThrow(() -> new RuntimeException("User Profile not found with phone: " + phone));
    }

    public List<UserProfileDTO> getRecentlyCreatedProfiles(LocalDateTime since) {
        return userProfileRepository.findUsersCreatedAfter(since)
            .stream()
            .map(UserProfileDTO::new)
            .collect(Collectors.toList());
    }

    public long getTotalUserProfiles() {
        return userProfileRepository.countAllUserProfiles();
    }

    public boolean existsByUserId(UUID userId) {
        return userProfileRepository.existsByUserId(userId);
    }

    public void deleteUserProfile(UUID userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("User Profile not found with user id: " + userId));
        userProfileRepository.delete(profile);
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
        nullNames.add("id");
        nullNames.add("userId");
        return nullNames.toArray(new String[0]);
    }
}
