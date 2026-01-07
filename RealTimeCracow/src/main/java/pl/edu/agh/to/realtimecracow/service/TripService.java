package pl.edu.agh.to.realtimecracow.service;

import com.google.transit.realtime.GtfsRealtime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.edu.agh.to.realtimecracow.model.Departure;
import pl.edu.agh.to.realtimecracow.model.StopInfo;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.time.LocalDateTime;


@Service
public class TripService {
    private static final Logger log = LoggerFactory.getLogger(TripService.class);

    private final GtfsClient gtfsClient;
    private final GtfsDataService gtfsDataService;
    private final GtfsParser gtfsParser;


    public TripService(GtfsClient gtfsClient,
                       GtfsDataService gtfsDataService,
                       GtfsParser gtfsParser) {
        this.gtfsClient = gtfsClient;
        this.gtfsDataService = gtfsDataService;
        this.gtfsParser = gtfsParser;
    }

    public Departure getRandomDeparture() throws IOException {

        GtfsRealtime.FeedMessage feed = gtfsClient.getTripUpdatesFeed();
        if (feed.getEntityCount() == 0) {
            return new Departure("No data", "-", "-");
        }

        GtfsRealtime.FeedEntity entity = feed.getEntity(0);

        // Try database first, fallback to GtfsParser
        if (gtfsDataService.hasData()) {
            List<StopInfo> stops = getStopListFromDatabase(entity);
            if (!stops.isEmpty()) {
                String lineNumber = getLineNumberFromDatabase(entity.getId());
                return new Departure(
                        stops.getFirst().stopName(),
                        lineNumber != null ? lineNumber : "-",
                        stops.getFirst().departureTime()
                );
            }
        }

        // Fallback to GtfsParser
        log.debug("Using GtfsParser fallback for entity {}", entity.getId());
        List<StopInfo> stops = gtfsParser.getStopListForEntity(entity);
        if (stops.isEmpty()) {
            return new Departure("No data", "-", "-");
        }

        return new Departure(
                stops.getFirst().stopName(),
                gtfsParser.getLineNumber(entity.getId()),
                stops.getFirst().departureTime()
        );
    }

    public Optional<Departure> getNextDirectDeparture(String fromStopName, String toStopName, LocalDateTime at) throws IOException {

        String fromStopId = gtfsDataService.getStopIdByStopName(fromStopName);
        String toStopId = gtfsDataService.getStopIdByStopName(toStopName);
        if (fromStopId == null || toStopId == null) {
            return Optional.empty();
        }
        Set<String> activeServiceIds = gtfsDataService.getActiveServiceIds(at.toLocalDate());
        if (activeServiceIds.isEmpty()) {
            return Optional.empty();
        }

        var best = gtfsDataService.findBestDirectTrip(fromStopId, toStopId, at, activeServiceIds);
        if (best == null) {
            return Optional.empty();
        }

        String predictedDeparture = best.departureTime();
        String lineNumber = (best.routeId() != null) ? best.routeId() : "-";

        String realtimeArrival = getRealtimeArrivalTimeIfAvailable(best.tripId(), toStopId);

        return Optional.of(new Departure(fromStopName, lineNumber, predictedDeparture));
    }
    private String getRealtimeArrivalTimeIfAvailable(String tripId, String toStopId) throws IOException {
        return null;
    }


    private List<StopInfo> getStopListFromDatabase(GtfsRealtime.FeedEntity entity) {
        List<StopInfo> stops = new ArrayList<>();

        for (GtfsRealtime.TripUpdate.StopTimeUpdate stopTimeUpdate : entity.getTripUpdate().getStopTimeUpdateList()) {
            if (stopTimeUpdate.hasDeparture()) {
                long departureTimestamp = stopTimeUpdate.getDeparture().getTime();
                String formattedDepartureTime = Instant.ofEpochSecond(departureTimestamp)
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                long arrivalTimestamp = stopTimeUpdate.getArrival().getTime();
                String formattedArrivalTime = Instant.ofEpochSecond(arrivalTimestamp)
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                String tripId = extractTripId(entity.getId());
                String stopId = gtfsDataService.getStopIdForTripAndSequence(tripId, stopTimeUpdate.getStopSequence());
                String stopName = stopId != null ? gtfsDataService.getStopName(stopId) : null;

                stops.add(new StopInfo(
                        stopId,
                        stopName,
                        formattedArrivalTime,
                        formattedDepartureTime,
                        stopTimeUpdate.getStopSequence()
                ));
            }
        }
        return stops;
    }

    private String getLineNumberFromDatabase(String entityId) {
        String tripId = extractTripId(entityId);
        return gtfsDataService.getRouteIdForTrip(tripId);
    }

    private String extractTripId(String entityId) {
        return entityId.substring(7);
    }
}

