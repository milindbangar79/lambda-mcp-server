package com.example.mcp.router.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot's JacksonAutoConfiguration only registers an ObjectMapper bean via
 * {@code Jackson2ObjectMapperBuilder}, which lives in spring-web - not on this
 * module's classpath by design (no spring-boot-starter-web; see router's pom.xml).
 * So the ObjectMapper is defined explicitly here instead of relying on autoconfiguration.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
