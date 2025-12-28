package com.example.GradeSystemBackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 数据库并发控制配置类
 * 配置事务管理、隔离级别等并发控制参数
 */
@Configuration
@EnableTransactionManagement
public class ConcurrencyConfig {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    /**
     * 配置JPA事务管理器
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);

        // 设置全局默认事务超时时间（秒）
        transactionManager.setDefaultTimeout(30);

        // 启用嵌套事务支持
        transactionManager.setNestedTransactionAllowed(true);

        return transactionManager;
    }

    /**
     * 异常转换处理器
     * 将JPA异常转换为Spring的DataAccessException
     */
    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }
}
