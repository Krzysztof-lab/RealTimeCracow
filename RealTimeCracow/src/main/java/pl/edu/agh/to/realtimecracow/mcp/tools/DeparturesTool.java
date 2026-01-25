package pl.edu.agh.to.realtimecracow.mcp.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import pl.edu.agh.to.realtimecracow.mcp.dto.DepartureDto;
import pl.edu.agh.to.realtimecracow.mcp.service.DepartureService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DeparturesTool {

    private final DepartureService departureService;

    @Tool(description = "Get next departures for a specific stop and line")
    public List<DepartureDto> nextDepartures(
            @ToolParam(description = "Feed type: A (bus), M (bus suburban), or T (tram)")
            String feedType,

            @ToolParam(description = "Stop name (e.g. 'Agatowa', 'AGH / UR')")
            String stopName,

            @ToolParam(description = "Line number/name (e.g. '4', '164')")
            String line,

            @ToolParam(description = "Optional: ISO datetime (e.g. '2025-01-23T14:30:00'). If not provided, uses current time.", required = false)
            String dateTime
    ) {
        validateRequiredParams(feedType, stopName, line);

        LocalDateTime at = parseDateTime(dateTime);

        return departureService.findNextDepartures(feedType, stopName, line, at);
    }

    private void validateRequiredParams(String feedType, String stopName, String line) {
        List<String> missing = new ArrayList<>();
        if (feedType == null || feedType.isBlank()) missing.add("feedType");
        if (stopName == null || stopName.isBlank()) missing.add("stopName");
        if (line == null || line.isBlank()) missing.add("line");

        if (!missing.isEmpty()) {
            throw new IllegalArgumentException(
                    "Missing required parameter(s): " + String.join(", ", missing)
            );
        }
    }

    private LocalDateTime parseDateTime(String dateTime) {
        return (dateTime == null || dateTime.isBlank())
                ? LocalDateTime.now()
                : LocalDateTime.parse(dateTime);
    }
}