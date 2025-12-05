package com.example.GradeSystemBackend.dto;

import com.example.GradeSystemBackend.domain.auth.RoleConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20之间")
    @Pattern(
        regexp = "^[a-zA-Z0-9_]+$",
        message = "用户名只能包含字母、数字和下划线"
    )
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度必须在6-50之间")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    // 真实姓名（可选）
    @Size(max = 50, message = "真实姓名长度不能超过50")
    private String realName;

    // 邮箱（可选）
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
        message = "邮箱格式不正确"
    )
    private String email;

    // 手机号（可选）
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    // 角色类型（可选，默认为学生）
    private String role = RoleConstants.DEFAULT_ROLE;

    // 构造函数
    public RegisterRequest() {}

    public RegisterRequest(
        String username,
        String password,
        String confirmPassword
    ) {
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /**
     * 验证密码是否一致
     */
    public boolean isPasswordMatch() {
        return password != null && password.equals(confirmPassword);
    }

    /**
     * 验证角色是否有效
     */
    public boolean isValidRole() {
        return RoleConstants.isValidRole(this.role);
    }

    /**
     * 获取有效的角色（如果无效则返回默认角色）
     */
    public String getValidRole() {
        return isValidRole() ? this.role : RoleConstants.DEFAULT_ROLE;
    }
}
