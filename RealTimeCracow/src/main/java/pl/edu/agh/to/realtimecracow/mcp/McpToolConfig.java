package pl.edu.agh.to.realtimecracow.mcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.edu.agh.to.realtimecracow.mcp.tools.ConnectionsTool;
import pl.edu.agh.to.realtimecracow.mcp.tools.DeparturesTool;
import pl.edu.agh.to.realtimecracow.mcp.tools.StopsTool;

@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider stopsTools(StopsTool stopsTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(stopsTool)
                .build();
    }

    @Bean
    public ToolCallbackProvider departuresTools(DeparturesTool departuresTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(departuresTool)
                .build();
    }

    @Bean
    public ToolCallbackProvider connectionsTools(ConnectionsTool connectionsTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(connectionsTool)
                .build();
    }
}