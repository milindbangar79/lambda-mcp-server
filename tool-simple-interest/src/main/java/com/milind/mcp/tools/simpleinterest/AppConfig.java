package com.milind.mcp.tools.simpleinterest;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Plain Spring configuration - intentionally not {@code @SpringBootApplication}. No
 * autoconfiguration, no classpath scanning beyond this package: the whole point of a
 * thin tool Lambda (Blueprint Section 5.1) is that its DI container has exactly as much
 * to do as this one tool needs, not a general-purpose Spring Boot app's worth of scanning.
 */
@Configuration
@ComponentScan(basePackageClasses = AppConfig.class)
public class AppConfig {
}
