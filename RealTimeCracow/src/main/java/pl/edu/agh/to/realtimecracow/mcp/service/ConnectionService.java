package pl.edu.agh.to.realtimecracow.mcp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.to.realtimecracow.mcp.dto.ConnectionDto;
import pl.edu.agh.to.realtimecracow.service.GtfsDataService;
import pl.edu.agh.to.realtimecracow.service.TripService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ConnectionService {

    private final GtfsDataService gtfsDataService;
    private final TripService tripService;

    public List<ConnectionDto> findConnections(String feedType, String fromStopName, String toStopName, LocalDateTime dateTime) {
        List<String> fromIds = gtfsDataService.getStopIdsByStopName(fromStopName);
        List<String> toIds = gtfsDataService.getStopIdsByStopName(toStopName);

        if (fromIds.isEmpty() || toIds.isEmpty()) {
            return List.of();
        }

        Set<String> serviceIds = gtfsDataService.getActiveServiceIds(feedType, dateTime.toLocalDate());

        var trips = gtfsDataService.findTopDirectTrips(feedType, fromIds, toIds, dateTime, serviceIds, 3);

        return trips.stream()
                .map(trip -> {
                    Integer delaySeconds = tripService.getDepartureDelaySeconds(
                            trip.tripId(),
                            fromIds,
                            trip.departureTime()
                    );

                    return new ConnectionDto(
                            trip.departureTime(),
                            trip.arrivalTime(),
                            delaySeconds
                    );
                })
                .toList();
    }
}
