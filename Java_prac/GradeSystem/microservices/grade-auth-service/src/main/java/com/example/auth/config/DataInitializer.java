package com.example.auth.config;

import com.example.auth.domain.Role;
import com.example.auth.domain.RoleConstants;
import com.example.auth.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            for (String roleName : RoleConstants.getAllRoles()) {
                if (roleRepository.findByName(roleName).isEmpty()) {
                    Role role = new Role();
                    role.setName(roleName);
                    roleRepository.save(role);
                    logger.info("Created role: {}", roleName);
                } else {
                    logger.debug("Role already exists: {}", roleName);
                }
            }
            logger.info("Role initialization completed. Total roles: {}", roleRepository.count());
        };
    }
}
