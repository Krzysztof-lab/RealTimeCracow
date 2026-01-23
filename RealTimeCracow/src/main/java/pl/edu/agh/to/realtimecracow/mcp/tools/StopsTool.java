package pl.edu.agh.to.realtimecracow.mcp.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import pl.edu.agh.to.realtimecracow.mcp.McpStopsService;
import pl.edu.agh.to.realtimecracow.mcp.dto.StopDto;

import java.util.List;

@Component
public class StopsTool {

    private final McpStopsService mcpStopsService;

    public StopsTool(McpStopsService mcpStopsService) {
        this.mcpStopsService = mcpStopsService;
    }

    @Tool(description = "Returns all GTFS stops")
    public List<StopDto> listStops() {
        return mcpStopsService.listStops();
    }
}