package pl.edu.agh.to.realtimecracow.mcp.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import pl.edu.agh.to.realtimecracow.mcp.service.StopsService;
import pl.edu.agh.to.realtimecracow.mcp.dto.StopDto;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StopsTool {

    private final StopsService mcpStopsService;

    @Tool(description = "Returns all GTFS stops")
    public List<StopDto> listStops() {
        return mcpStopsService.listStops();
    }
}