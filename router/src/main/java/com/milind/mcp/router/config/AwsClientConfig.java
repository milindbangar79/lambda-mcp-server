package com.milind.mcp.router.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.lambda.LambdaClient;

/** Provides the AWS SDK client(s) the router needs to talk to other AWS services. */
@Configuration
public class AwsClientConfig {

    /**
     * The URL Connection HTTP client is used instead of the SDK's default (Apache/Netty)
     * — it's dependency-light and starts faster, which matters more inside a Lambda cold
     * start than its lack of HTTP/2 or connection pooling sophistication does for a
     * low-QPS router invoking 3 downstream functions.
     */
    @Bean
    public LambdaClient lambdaClient() {
        return LambdaClient.builder()
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .build();
    }
}
