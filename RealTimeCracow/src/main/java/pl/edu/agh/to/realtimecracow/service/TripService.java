package pl.edu.agh.to.realtimecracow.service;

import com.google.transit.realtime.GtfsRealtime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.edu.agh.to.realtimecracow.model.Departure;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.time.LocalDateTime;


@Service
public class TripService {
    private static final Logger log = LoggerFactory.getLogger(TripService.class);

    private final GtfsClient gtfsClient;
    private final GtfsDataService gtfsDataService;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");


    public TripService(GtfsClient gtfsClient,
                       GtfsDataService gtfsDataService) {
        this.gtfsClient = gtfsClient;
        this.gtfsDataService = gtfsDataService;
    }

    public Optional<Departure> getNextDirectDeparture(String feedType, String fromStopName, String toStopName, LocalDateTime at) {

        List<String> fromStopIds = gtfsDataService.getStopIdsByStopName(fromStopName);
        List<String> toStopIds = gtfsDataService.getStopIdsByStopName(toStopName);

        return Optional.of(fromStopIds)
                .filter(ids -> !ids.isEmpty() && !toStopIds.isEmpty())
                .flatMap(_ -> {
                    Set<String> activeServiceIds = gtfsDataService.getActiveServiceIds(feedType, at.toLocalDate());
                    return activeServiceIds.isEmpty()
                            ? Optional.empty()
                            : Optional.ofNullable(gtfsDataService.findBestDirectTrip(feedType, fromStopIds, toStopIds, at, activeServiceIds));
                })
                .map(best -> {
                    String actualDepartureTime = getDepartureTimeWithRealtime(best.tripId(), fromStopIds, best.departureTime());
                    String routeShortName = gtfsDataService.getRouteShortName(best.routeId());
                    String lineNumber = (routeShortName != null) ? routeShortName : "-";

                    return new Departure(fromStopName, lineNumber, actualDepartureTime);
                });
    }

    private String getDepartureTimeWithRealtime(String tripId, List<String> fromStopIds, String scheduledTime) {
        try {
            String realtimeDeparture = getRealtimeDepartureTime(tripId, fromStopIds);
            return (realtimeDeparture != null) ? realtimeDeparture : scheduledTime;
        } catch (IOException e) {
            log.warn("Failed to get realtime data, using scheduled time", e);
            return scheduledTime;
        }
    }

    private String getRealtimeDepartureTime(String tripId, List<String> stopIds) throws IOException {
        GtfsRealtime.FeedMessage feed = gtfsClient.getTripUpdatesFeed();
        String normalizedTripId = tripId.startsWith("block")
                ? tripId.replaceFirst("^block", "").replaceFirst("_service_1$", "")
                : tripId;

        return feed.getEntityList().stream()
                .filter(GtfsRealtime.FeedEntity::hasTripUpdate)
                .filter(entity -> extractTripId(entity.getId()).equals(normalizedTripId))
                .flatMap(entity -> entity.getTripUpdate().getStopTimeUpdateList().stream())
                .filter(GtfsRealtime.TripUpdate.StopTimeUpdate::hasDeparture)
                .filter(stopTimeUpdate -> {
                    String stopId = gtfsDataService.getStopIdForTripAndSequence(
                            normalizedTripId,
                            stopTimeUpdate.getStopSequence()
                    );
                    return stopIds.contains(stopId);
                })
                .findFirst()
                .map(stopTimeUpdate -> {
                    long realtimeTimestamp = stopTimeUpdate.getDeparture().getTime();
                    return Instant.ofEpochSecond(realtimeTimestamp)
                            .atZone(ZoneId.systemDefault())
                            .format(TIME_FORMATTER);
                })
                .orElse(null);
    }

    public Integer getDepartureDelaySeconds(String tripId, List<String> stopIds, String scheduledTime) {
        try {
            String realtime = getRealtimeDepartureTime(tripId, stopIds);
            if (realtime == null) return 0;
            return secondsBetween(scheduledTime, realtime);
        } catch (IOException e) {
            log.warn("Failed to get realtime delay, using 0", e);
            return 0;
        }
    }

    public String getRealtimeOrScheduledDeparture(String tripId, List<String> stopIds, String scheduledTime) {
        try {
            String realtime = getRealtimeDepartureTime(tripId, stopIds);
            return realtime != null ? realtime : scheduledTime;
        } catch (IOException e) {
            log.warn("Failed to get realtime departure time, using scheduled", e);
            return scheduledTime;
        }
    }

    private int secondsBetween(String scheduled, String realtime) {
        int s1 = toSeconds(scheduled);
        int s2 = toSeconds(realtime);
        return s2 - s1;
    }

    private int toSeconds(String t) {
        String[] p = t.split(":");
        int h = Integer.parseInt(p[0]);
        int m = Integer.parseInt(p[1]);
        int s = Integer.parseInt(p[2]);
        return h * 3600 + m * 60 + s;
    }

    private String extractTripId(String entityId) {
        return entityId.substring(7);
    }
}

