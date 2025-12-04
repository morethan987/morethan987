package com.example.GradeSystemBackend;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class GradeSystemBackendApplication {

    private static final Logger log = LoggerFactory.getLogger(
        GradeSystemBackendApplication.class
    );

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext env = SpringApplication.run(
            GradeSystemBackendApplication.class,
            args
        );
        show_startup(env);
    }

    private static void show_startup(ConfigurableApplicationContext env)
        throws UnknownHostException {
        String ip = InetAddress.getLocalHost().getHostAddress();
        Environment environment = env.getEnvironment();
        String port = environment.getProperty("server.port", "8080"); // 默认8080
        String path = environment.getProperty(
            "server.servlet.context-path",
            ""
        ); // 默认空

        log.info(
            "\n----------------------------------------------------------\n" +
                "Application is running! Access URLs:\n" +
                "Local: \t\thttp://localhost:" +
                port +
                path +
                "/\n" +
                "External: \thttp://" +
                ip +
                ":" +
                port +
                path +
                "/\n" +
                "Swagger文档: \thttp://localhost:" +
                port +
                path +
                "/swagger-ui/index.html\n" +
                "----------------------------------------------------------"
        );
    }
}
