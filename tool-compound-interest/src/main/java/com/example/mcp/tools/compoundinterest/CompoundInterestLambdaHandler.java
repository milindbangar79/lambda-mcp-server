package com.example.mcp.tools.compoundinterest;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class CompoundInterestLambdaHandler implements RequestHandler<CompoundInterestRequest, CompoundInterestResponse> {

    private final CompoundInterestService compoundInterestService;

    public CompoundInterestLambdaHandler() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        this.compoundInterestService = context.getBean(CompoundInterestService.class);
    }

    @Override
    public CompoundInterestResponse handleRequest(CompoundInterestRequest input, Context context) {
        String correlationId = extractCorrelationId(context);
        context.getLogger().log("Handling compound-interest-calculator tool call, correlationId=" + correlationId);
        return compoundInterestService.calculate(input);
    }

    private String extractCorrelationId(Context context) {
        ClientContext clientContext = context.getClientContext();
        if (clientContext == null || clientContext.getCustom() == null) {
            return null;
        }
        return clientContext.getCustom().get("correlationId");
    }
}
