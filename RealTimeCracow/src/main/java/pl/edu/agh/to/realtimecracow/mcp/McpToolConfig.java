package pl.edu.agh.to.realtimecracow.mcp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;

import pl.edu.agh.to.realtimecracow.mcp.dto.ConnectionDto;
import pl.edu.agh.to.realtimecracow.mcp.dto.DepartureDto;
import pl.edu.agh.to.realtimecracow.mcp.dto.StopDto;
import pl.edu.agh.to.realtimecracow.service.GtfsDataService;
import pl.edu.agh.to.realtimecracow.service.TripService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallback listStopsTool(McpStopsService mcpStopsService) {
        return FunctionToolCallback
                .builder("list_stops", (Map<String, Object> args) -> {
                    List<StopDto> stops = mcpStopsService.listStops();
                    return stops;
                })
                .description("Returns all GTFS stops")
                .build();
    }

    @Bean
    public ToolCallback findConnectionsTool(GtfsDataService gtfsDataService, TripService tripService) {
        return FunctionToolCallback
                .builder("find_connections", (Map<String, Object> args) -> {

                    String feedType = (String) args.get("feedType");
                    String fromStopName = (String) args.get("fromStopName");
                    String toStopName = (String) args.get("toStopName");
                    String dateTimeIso = (String) args.get("dateTime");

                    List<String> missing = new ArrayList<>();
                    if (feedType == null || feedType.isBlank()) missing.add("feedType");
                    if (fromStopName == null || fromStopName.isBlank()) missing.add("fromStopName");
                    if (toStopName == null || toStopName.isBlank()) missing.add("toStopName");

                    if (!missing.isEmpty()) {
                        throw new IllegalArgumentException("Missing required parameter(s): " + String.join(", ", missing));
                    }

                    LocalDateTime at = (dateTimeIso == null || dateTimeIso.isBlank())
                            ? LocalDateTime.now()
                            : LocalDateTime.parse(dateTimeIso);

                    List<String> fromIds = gtfsDataService.getStopIdsByStopName(fromStopName);
                    List<String> toIds = gtfsDataService.getStopIdsByStopName(toStopName);
                    if (fromIds.isEmpty() || toIds.isEmpty()) return List.of();

                    Set<String> serviceIds = gtfsDataService.getActiveServiceIds(feedType, at.toLocalDate());

                    var trips = gtfsDataService.findTopDirectTrips(feedType, fromIds, toIds, at, serviceIds, 3);

                    return trips.stream().map(t -> {
                        String routeShortName = gtfsDataService.getRouteShortName(t.routeId());

                        Integer delaySeconds = tripService.getDepartureDelaySeconds(
                                t.tripId(),
                                fromIds,
                                t.departureTime()
                        );

                        return new ConnectionDto(
                                t.tripId(),
                                t.routeId(),
                                routeShortName,
                                t.departureTime(),
                                t.arrivalTime(),
                                delaySeconds
                        );
                    }).toList();
                })
                .description("Returns 3 direct connections that depart the earliest --- including delays")
                .build();
    }

    @Bean
    public ToolCallback nextDeparturesTool(GtfsDataService gtfsDataService, TripService tripService) {
        return FunctionToolCallback
                .builder("next_departures", (Map<String, Object> args) -> {

                    String feedType = (String) args.get("feedType");
                    String stopName = (String) args.get("stopName");
                    String line = (String) args.get("line");
                    String dateTimeIso = (String) args.get("dateTime");

                    if (feedType == null || stopName == null || line == null) {
                        throw new IllegalArgumentException(" feedType, stopName, line");
                    }

                    LocalDateTime at = (dateTimeIso == null || dateTimeIso.isBlank())
                            ? LocalDateTime.now()
                            : LocalDateTime.parse(dateTimeIso);

                    List<String> stopIds = gtfsDataService.getStopIdsByStopName(stopName);
                    if (stopIds.isEmpty()) return List.of();

                    Set<String> serviceIds = gtfsDataService.getActiveServiceIds(feedType, at.toLocalDate());
                    if (serviceIds.isEmpty()) return List.of();

                    var rows = gtfsDataService.findNextDeparturesRows(feedType, stopIds, line, at, serviceIds, 5);


                    return rows.stream().map(r -> {
                        String routeShortName = gtfsDataService.getRouteShortName(r.routeId());
                        String realStopName = gtfsDataService.getStopName(r.stopId());

                        Integer delaySeconds = tripService.getDepartureDelaySeconds(
                                r.tripId(),
                                stopIds,
                                r.departureTime()
                        );

                        return new DepartureDto(
                                r.tripId(),
                                r.routeId(),
                                routeShortName,
                                r.stopId(),
                                realStopName,
                                r.departureTime(),
                                delaySeconds
                        );
                    }).toList();
                })
                .description("")
                .build();
    }

}