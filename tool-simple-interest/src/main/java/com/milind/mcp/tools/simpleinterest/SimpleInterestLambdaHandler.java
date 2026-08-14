package com.milind.mcp.tools.simpleinterest;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Lambda entry point for the "simple-interest-calculator" tool. Like
 * {@code GreetingLambdaHandler}, the plain Spring {@link AnnotationConfigApplicationContext}
 * is built once in the constructor (cold-start only) and the resolved
 * {@link SimpleInterestService} bean is reused across every warm invocation.
 */
public class SimpleInterestLambdaHandler implements RequestHandler<SimpleInterestRequest, SimpleInterestResponse> {

    private final SimpleInterestService simpleInterestService;

    public SimpleInterestLambdaHandler() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        this.simpleInterestService = context.getBean(SimpleInterestService.class);
    }

    @Override
    public SimpleInterestResponse handleRequest(SimpleInterestRequest input, Context context) {
        String correlationId = extractCorrelationId(context);
        context.getLogger().log("Handling simple-interest-calculator tool call, correlationId=" + correlationId);
        return simpleInterestService.calculate(input);
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
