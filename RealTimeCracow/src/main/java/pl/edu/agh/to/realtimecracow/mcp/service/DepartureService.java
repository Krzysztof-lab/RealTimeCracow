package pl.edu.agh.to.realtimecracow.mcp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.to.realtimecracow.mcp.dto.DepartureDto;
import pl.edu.agh.to.realtimecracow.service.GtfsDataService;
import pl.edu.agh.to.realtimecracow.service.TripService;

import java.util.List;
import java.util.Set;
@Service
@RequiredArgsConstructor
public class DepartureService {
    private final GtfsDataService gtfsDataService;
    private final TripService tripService;

    public List<DepartureDto> findNextDepartures(String feedType, String stopName, String line, java.time.LocalDateTime at) {

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
