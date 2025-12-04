package com.example.GradeSystemBackend.service;

import com.example.GradeSystemBackend.domain.Permission;
import com.example.GradeSystemBackend.domain.Role;
import com.example.GradeSystemBackend.domain.User;
import com.example.GradeSystemBackend.repository.UserRepository;
import java.util.HashSet;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 关键方法：根据用户名从 DB 加载用户及其角色/权限，
     * 然后把它们转换为 Spring Security 所理解的 UserDetails（和 GrantedAuthority）。
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException {
        User user = userRepository
            .findByUsername(username)
            .orElseThrow(() ->
                new UsernameNotFoundException("用户不存在: " + username)
            );

        // 构建 authorities：既包含 ROLE_ 前缀的角色，也包含具体权限名
        Set<GrantedAuthority> authorities = new HashSet<>();

        // load roles -> each role as ROLE_NAME
        Set<Role> roles = user.getRoles();
        if (roles != null) {
            for (Role role : roles) {
                // Spring 的角色通常以 ROLE_ 前缀表示
                authorities.add(
                    new SimpleGrantedAuthority("ROLE_" + role.getName())
                );

                // 每个 role 的 permissions
                if (role.getPermissions() != null) {
                    for (Permission perm : role.getPermissions()) {
                        // 权限直接用 permission.name 比如 "score:input"
                        authorities.add(
                            new SimpleGrantedAuthority(perm.getName())
                        );
                    }
                }
            }
        }

        // 返回 Spring 提供的 User 实现（也可以自定义实现）
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            user.isEnabled(),
            true, // accountNonExpired
            true, // credentialsNonExpired
            true, // accountNonLocked
            authorities
        );
    }
}
