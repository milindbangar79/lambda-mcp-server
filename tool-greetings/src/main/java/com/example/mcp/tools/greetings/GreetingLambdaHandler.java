package com.example.mcp.tools.greetings;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Lambda entry point for the "greetings" tool. The {@link AnnotationConfigApplicationContext}
 * is built once in the constructor (a cold-start-only cost) and the {@link GreetingService}
 * bean is resolved once and reused across every warm invocation this execution
 * environment handles - no per-request DI lookups.
 */
public class GreetingLambdaHandler implements RequestHandler<GreetingRequest, GreetingResponse> {

    private final GreetingService greetingService;

    public GreetingLambdaHandler() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        this.greetingService = context.getBean(GreetingService.class);
    }

    @Override
    public GreetingResponse handleRequest(GreetingRequest input, Context context) {
        String correlationId = extractCorrelationId(context);
        context.getLogger().log("Handling greetings tool call, correlationId=" + correlationId);
        return greetingService.greet(input);
    }

    /** Correlation ID set by the router via ClientContext (see ToolDispatcherService). */
    private String extractCorrelationId(Context context) {
        ClientContext clientContext = context.getClientContext();
        if (clientContext == null || clientContext.getCustom() == null) {
            return null;
        }
        return clientContext.getCustom().get("correlationId");
    }
}
