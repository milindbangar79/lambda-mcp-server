package com.milind.mcp.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.milind.mcp.client.config.ClientConfig;
import com.milind.mcp.client.http.HttpTransport;
import com.milind.mcp.client.http.HttpTransportException;
import com.milind.mcp.client.http.JdkHttpTransport;
import com.milind.mcp.client.llm.AnthropicClient;
import com.milind.mcp.client.llm.AnthropicClientException;
import com.milind.mcp.client.mcp.McpClient;
import com.milind.mcp.client.mcp.McpClientException;
import com.milind.mcp.client.orchestrator.PromptOrchestrator;
import com.milind.mcp.common.model.InitializeResult;
import com.milind.mcp.common.model.McpTool;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * CLI entry point. No Lambda runtime here - see this module's pom.xml and the README's
 * "mcp-client" section for why this is a plain {@code java -jar}, not a fifth Lambda.
 *
 * <p>Two modes:
 * <ul>
 *   <li>{@code java -jar mcp-client.jar "your prompt here"} - single-shot: resolve and
 *       answer that one prompt, then exit.</li>
 *   <li>{@code java -jar mcp-client.jar} (no arguments) - interactive: discover tools
 *       once, then read prompts from stdin, one per line, until {@code exit}/{@code quit}
 *       or EOF.</li>
 * </ul>
 *
 * <p>Wiring is manual constructor injection - no DI container. See this module's pom.xml
 * for why: a short-lived CLI process doesn't need one.
 */
public final class Main {

    private static final int MAX_TOOL_CALLS_PER_PROMPT = 3;

    private Main() {
    }

    public static void main(String[] args) {
        ClientConfig config;
        try {
            config = ClientConfig.fromEnvironment();
        } catch (IllegalStateException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        HttpTransport httpTransport = new JdkHttpTransport();
        McpClient mcpClient = new McpClient(config.getMcpServerUrl(), httpTransport, objectMapper);

        List<McpTool> toolCatalog;
        try {
            InitializeResult serverInfo = mcpClient.initialize();
            System.err.printf("Connected to %s v%s (protocol %s) at %s%n",
                    serverInfo.getServerInfo().getName(), serverInfo.getServerInfo().getVersion(),
                    serverInfo.getProtocolVersion(), config.getMcpServerUrl());

            toolCatalog = mcpClient.listTools();
            if (toolCatalog.isEmpty()) {
                System.err.println("Warning: the router published zero tools - Claude will only be able to answer directly.");
            } else {
                System.err.println("Discovered " + toolCatalog.size() + " tool(s): "
                        + toolCatalog.stream().map(McpTool::getName).toList());
            }
        } catch (McpClientException | HttpTransportException e) {
            System.err.println("Failed to reach the MCP server at " + config.getMcpServerUrl() + ": " + e.getMessage());
            System.exit(1);
            return;
        }

        AnthropicClient anthropicClient = new AnthropicClient(config, httpTransport, objectMapper);
        PromptOrchestrator orchestrator = new PromptOrchestrator(
                mcpClient, anthropicClient, objectMapper, toolCatalog, MAX_TOOL_CALLS_PER_PROMPT);

        if (args.length > 0) {
            runSingleShot(orchestrator, String.join(" ", args));
        } else {
            runInteractive(orchestrator);
        }
    }

    private static void runSingleShot(PromptOrchestrator orchestrator, String prompt) {
        String result = resolve(orchestrator, prompt);
        System.out.println(result);
        if (result.startsWith("Error: ")) {
            System.exit(1);
        }
    }

    private static void runInteractive(PromptOrchestrator orchestrator) {
        System.err.println("Interactive mode. Type a prompt and press Enter; 'exit' or 'quit' to leave.");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            String line;
            while (true) {
                System.err.print("> ");
                System.err.flush();
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                String prompt = line.strip();
                if (prompt.isEmpty()) {
                    continue;
                }
                if (prompt.equalsIgnoreCase("exit") || prompt.equalsIgnoreCase("quit")) {
                    break;
                }
                System.out.println(resolve(orchestrator, prompt));
            }
        } catch (Exception e) {
            System.err.println("Input error: " + e.getMessage());
        }
    }

    private static String resolve(PromptOrchestrator orchestrator, String prompt) {
        try {
            return orchestrator.handlePrompt(prompt);
        } catch (AnthropicClientException | McpClientException | HttpTransportException e) {
            return "Error: " + e.getMessage();
        }
    }
}
