package com.vizu.identidade.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class FlywayConfig {
    @Bean
    static BeanFactoryPostProcessor migrateDatabaseBeforeJpa() {
        return beanFactory -> {
            Environment env = beanFactory.getBean(Environment.class);
            Flyway.configure()
                    .dataSource(env.getRequiredProperty("spring.datasource.url"),
                            env.getRequiredProperty("spring.datasource.username"),
                            env.getRequiredProperty("spring.datasource.password"))
                    .schemas("identidade")
                    .defaultSchema("identidade")
                    .load()
                    .migrate();
        };
    }
}
