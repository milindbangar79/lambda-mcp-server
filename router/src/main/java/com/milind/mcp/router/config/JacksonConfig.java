package com.milind.mcp.router.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
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
        ObjectMapper objectMapper = new ObjectMapper();
        // Be lenient in what the router accepts on incoming JSON-RPC requests: a
        // spec-compliant MCP client may send fields this server doesn't model (protocol
        // extensions, client metadata), and rejecting the whole request over an unknown
        // field is exactly the failure mode that surfaced while building mcp-client (see
        // JsonRpcRequest.isNotification()'s @JsonIgnore). Fields it does model are still
        // fully validated by their declared types.
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }
}
