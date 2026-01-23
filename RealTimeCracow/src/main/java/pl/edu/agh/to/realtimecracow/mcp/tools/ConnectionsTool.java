package pl.edu.agh.to.realtimecracow.mcp.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import pl.edu.agh.to.realtimecracow.mcp.dto.ConnectionDto;
import pl.edu.agh.to.realtimecracow.service.GtfsDataService;
import pl.edu.agh.to.realtimecracow.service.TripService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class ConnectionsTool {

    private final GtfsDataService gtfsDataService;
    private final TripService tripService;

    public ConnectionsTool(GtfsDataService gtfsDataService, TripService tripService) {
        this.gtfsDataService = gtfsDataService;
        this.tripService = tripService;
    }

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
        List<String> missing = new ArrayList<>();
        if (feedType == null || feedType.isBlank()) missing.add("feedType");
        if (fromStopName == null || fromStopName.isBlank()) missing.add("fromStopName");
        if (toStopName == null || toStopName.isBlank()) missing.add("toStopName");

        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Missing required parameter(s): " + String.join(", ", missing));
        }

        LocalDateTime at = (dateTime == null || dateTime.isBlank())
                ? LocalDateTime.now()
                : LocalDateTime.parse(dateTime);

        List<String> fromIds = gtfsDataService.getStopIdsByStopName(fromStopName);
        List<String> toIds = gtfsDataService.getStopIdsByStopName(toStopName);
        if (fromIds.isEmpty() || toIds.isEmpty()) return List.of();

        Set<String> serviceIds = gtfsDataService.getActiveServiceIds(feedType, at.toLocalDate());

        var trips = gtfsDataService.findTopDirectTrips(feedType, fromIds, toIds, at, serviceIds, 3);

        return trips.stream().map(t -> {

            Integer delaySeconds = tripService.getDepartureDelaySeconds(
                    t.tripId(),
                    fromIds,
                    t.departureTime()
            );

            return new ConnectionDto(
                    t.departureTime(),
                    t.arrivalTime(),
                    delaySeconds
            );
        }).toList();
    }
}