package com.example.mcp.tools.simpleinterest;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

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

    private String extractCorrelationId(Context context) {
        ClientContext clientContext = context.getClientContext();
        if (clientContext == null || clientContext.getCustom() == null) {
            return null;
        }
        return clientContext.getCustom().get("correlationId");
    }
}
