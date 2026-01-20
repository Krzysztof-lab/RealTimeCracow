package pl.edu.agh.to.realtimecracow.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class DirectConnectionRepository {

    public record DirectTripRow(String tripId, String routeId, String departureTime, String arrivalTime) {}

    private final JdbcTemplate jdbc;

    public DirectConnectionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<DirectTripRow> findDirectTrips(
            String feedType,
            List<String> fromStopIds,
            List<String> toStopIds,
            String fromTime,
            Set<String> serviceIds,
            int limit
    ) {
        if (feedType == null || feedType.isBlank()) return List.of();
        if (limit <= 0) return List.of();
        if (serviceIds == null || serviceIds.isEmpty()) return List.of();
        if (fromStopIds == null || fromStopIds.isEmpty()) return List.of();
        if (toStopIds == null || toStopIds.isEmpty()) return List.of();

        String serviceIn = String.join(",", Collections.nCopies(serviceIds.size(), "?"));
        String fromIn = String.join(",", Collections.nCopies(fromStopIds.size(), "?"));
        String toIn = String.join(",", Collections.nCopies(toStopIds.size(), "?"));

        String sql = """
        SELECT t.trip_id,
               t.route_id,
               st_from.departure_time,
               st_to.arrival_time
        FROM trip t
        JOIN stop_time st_from
          ON st_from.trip_id = t.trip_id AND st_from.feed_type = t.feed_type
        JOIN stop_time st_to
          ON st_to.trip_id = t.trip_id AND st_to.feed_type = t.feed_type
        WHERE t.feed_type = ?
          AND st_from.stop_id IN (%s)
          AND st_to.stop_id IN (%s)
          AND st_from.stop_sequence < st_to.stop_sequence
          AND st_from.departure_time >= ?
          AND t.service_id IN (%s)
        ORDER BY st_from.departure_time ASC
        LIMIT ?
    """.formatted(fromIn, toIn, serviceIn);

        List<Object> params = new ArrayList<>();
        params.add(feedType);
        params.addAll(fromStopIds);
        params.addAll(toStopIds);
        params.add(fromTime);
        params.addAll(serviceIds);
        params.add(limit);

        return jdbc.query(sql, (rs, i) -> new DirectTripRow(
                rs.getString("trip_id"),
                rs.getString("route_id"),
                rs.getString("departure_time"),
                rs.getString("arrival_time")
        ), params.toArray());
    }

    public DirectTripRow findBestDirectTrip(
            String feedType,
            List<String> fromStopIds,
            List<String> toStopIds,
            String fromTime,
            Set<String> serviceIds
    ) {
        return findDirectTrips(feedType, fromStopIds, toStopIds, fromTime, serviceIds, 1)
                .stream()
                .findFirst()
                .orElse(null);
    }

    public List<DirectTripRow> findTopDirectTrips(
            String feedType,
            List<String> fromStopIds,
            List<String> toStopIds,
            String fromTime,
            Set<String> serviceIds,
            int limit
    ) {
        return findDirectTrips(feedType, fromStopIds, toStopIds, fromTime, serviceIds, limit);
    }
}
