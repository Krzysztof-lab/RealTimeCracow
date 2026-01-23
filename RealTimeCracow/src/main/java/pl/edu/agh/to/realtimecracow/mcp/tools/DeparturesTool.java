package pl.edu.agh.to.realtimecracow.mcp.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import pl.edu.agh.to.realtimecracow.mcp.dto.DepartureDto;
import pl.edu.agh.to.realtimecracow.service.GtfsDataService;
import pl.edu.agh.to.realtimecracow.service.TripService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
public class DeparturesTool {

    private final GtfsDataService gtfsDataService;
    private final TripService tripService;

    public DeparturesTool(GtfsDataService gtfsDataService, TripService tripService) {
        this.gtfsDataService = gtfsDataService;
        this.tripService = tripService;
    }

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
        if (feedType == null || stopName == null || line == null) {
            throw new IllegalArgumentException("feedType, stopName, and line are required");
        }

        LocalDateTime at = (dateTime == null || dateTime.isBlank())
                ? LocalDateTime.now()
                : LocalDateTime.parse(dateTime);

        List<String> stopIds = gtfsDataService.getStopIdsByStopName(stopName);
        if (stopIds.isEmpty()) return List.of();

        Set<String> serviceIds = gtfsDataService.getActiveServiceIds(feedType, at.toLocalDate());
        if (serviceIds.isEmpty()) return List.of();

        var rows = gtfsDataService.findNextDeparturesRows(feedType, stopIds, line, at, serviceIds, 5);

        return rows.stream().map(r -> {
            Integer delaySeconds = tripService.getDepartureDelaySeconds(
                    r.tripId(),
                    stopIds,
                    r.departureTime()
            );

            return new DepartureDto(
                    r.departureTime(),
                    delaySeconds
            );
        }).toList();
    }
}