package com.example.GradeSystemBackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
        throws Exception {
        http
            // 1. 禁用 CSRF (因为我们用 JWT，不需要 Session，所以也不需要 CSRF 防护)
            .csrf(csrf -> csrf.disable())
            // 2. 设置 Session 管理为无状态 (Stateless)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // 3. 配置拦截规则
            .authorizeHttpRequests(auth ->
                auth
                    // 放行 Swagger 相关路径 (否则你看不到 API 文档)
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                    .permitAll()
                    // 放行登录接口
                    .requestMatchers("/auth/**")
                    .permitAll()
                    // 其他所有请求都需要认证
                    .anyRequest()
                    .authenticated()
            )
            // 4. 把我们的 JWT 过滤器加到 Spring Security 过滤器链中
            .addFilterBefore(
                jwtFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    // 密码加密器
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
