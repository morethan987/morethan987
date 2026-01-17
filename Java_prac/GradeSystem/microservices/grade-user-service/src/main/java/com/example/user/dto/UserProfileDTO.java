package com.example.user.dto;

import com.example.user.domain.Gender;
import com.example.user.domain.UserProfile;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class UserProfileDTO {

    private UUID id;
    private UUID userId;
    private String realName;
    private Gender gender;
    private LocalDate birthDate;
    private String email;
    private String phone;
    private String address;
    private String bio;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserProfileDTO() {}

    public UserProfileDTO(UserProfile userProfile) {
        this.id = userProfile.getId();
        this.userId = userProfile.getUserId();
        this.realName = userProfile.getRealName();
        this.gender = userProfile.getGender();
        this.birthDate = userProfile.getBirthDate();
        this.email = userProfile.getEmail();
        this.phone = userProfile.getPhone();
        this.address = userProfile.getAddress();
        this.bio = userProfile.getBio();
        this.avatarUrl = userProfile.getAvatarUrl();
        this.createdAt = userProfile.getCreatedAt();
        this.updatedAt = userProfile.getUpdatedAt();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
