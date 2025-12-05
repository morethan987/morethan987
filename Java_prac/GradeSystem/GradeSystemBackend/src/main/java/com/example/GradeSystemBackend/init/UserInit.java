package com.example.GradeSystemBackend.init;

import com.example.GradeSystemBackend.domain.auth.Role;
import com.example.GradeSystemBackend.domain.auth.RoleConstants;
import com.example.GradeSystemBackend.domain.auth.User;
import com.example.GradeSystemBackend.repository.RoleRepository;
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
    private RoleRepository roleRepository;

    @Override
    public void run(ApplicationArguments args) {
        createAdminUser();
    }

    private void createAdminUser() {
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
        userRepository.save(adminUser);
    }
}
