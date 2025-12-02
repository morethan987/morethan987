package com.example.GradeSystemBackend;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(security = { @SecurityRequirement(name = "bearer-key") })
@SecurityScheme(
    name = "bearer-key",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "请在下面填入登录接口返回的 Token"
)
public class GradeSystemBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(GradeSystemBackendApplication.class, args);
    }
}
