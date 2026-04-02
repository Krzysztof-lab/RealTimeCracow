package pl.edu.agh.to.realtimecracow.mcp.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import pl.edu.agh.to.realtimecracow.mcp.dto.ConnectionDto;
import pl.edu.agh.to.realtimecracow.mcp.service.ConnectionService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ConnectionsTool {

    private final ConnectionService connectionService;

    @Tool(description = "Find direct connections between two stops. Returns top 3 connections that depart the earliest, including real-time delays.")
    public List<ConnectionDto> findConnections(
            @ToolParam(description = "Feed type: A (bus), M (bus suburban), or T (tram)")
            String feedType,

            @ToolParam(description = "Departure stop name (e.g. 'Agatowa', 'AGH / UR')")
            String fromStopName,

            @ToolParam(description = "Arrival stop name (e.g. 'Rondo Mogilskie', 'Dworzec Główny')")
            String toStopName,

            @ToolParam(description = "Optional: ISO datetime for departure (e.g. '2025-01-23T14:30:00'). If not provided, uses current time.", required = false)
            String dateTime
    ) {
        validateRequiredParams(feedType, fromStopName, toStopName);

        LocalDateTime at = parseDateTime(dateTime);

        return connectionService.findConnections(feedType, fromStopName, toStopName, at);
    }

    private void validateRequiredParams(String feedType, String fromStopName, String toStopName) {
        List<String> missing = new ArrayList<>();
        if (feedType == null || feedType.isBlank()) missing.add("feedType");
        if (fromStopName == null || fromStopName.isBlank()) missing.add("fromStopName");
        if (toStopName == null || toStopName.isBlank()) missing.add("toStopName");

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