package com.example.GradeSystemBackend.init;

import com.example.GradeSystemBackend.domain.auth.Role;
import com.example.GradeSystemBackend.domain.auth.RoleConstants;
import com.example.GradeSystemBackend.domain.auth.UIType;
import com.example.GradeSystemBackend.domain.auth.User;
import com.example.GradeSystemBackend.domain.info.Gender;
import com.example.GradeSystemBackend.domain.info.UserProfile;
import com.example.GradeSystemBackend.repository.RoleRepository;
import com.example.GradeSystemBackend.repository.UserProfileRepository;
import com.example.GradeSystemBackend.repository.UserRepository;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Order(3)
@Component
public class UserInit implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(ApplicationArguments args) {
        createAdminUser();
    }

    private void createAdminUser() {
        // 添加用户
        Optional<User> adminUserOpt = userRepository.findByUsername("admin");
        if (adminUserOpt.isPresent()) {
            return; // 已存在，不重复创建
        }
        User adminUser = new User();
        adminUser.setUsername("admin");
        String encodedPassword = new BCryptPasswordEncoder().encode("admin123");
        adminUser.setPassword(encodedPassword); // 请确保密码符合安全要求

        Optional<Role> adminRoleOpt = roleRepository.findByName(
            RoleConstants.ROLE_ADMIN
        );
        if (adminRoleOpt.isPresent()) {
            adminUser.setRoles(Set.of(adminRoleOpt.get()));
        }
        adminUser.setUiType(UIType.ADMIN);
        userRepository.save(adminUser);

        // 创建关联的用户资料
        UserProfile adminProfile = new UserProfile(
            adminUser,
            "系统管理员",
            Gender.MALE
        );
        adminProfile.setEmail("2404385626@qq.com");
        adminProfile.setPhone("1234567890");
        adminProfile.setAvatarUrl(
            "https://raw.githubusercontent.com/morethan987/hugo_main/refs/heads/main/assets/img/figure_transparent.png"
        );

        userProfileRepository.save(adminProfile);
    }
}
