package pl.edu.agh.to.realtimecracow.repository;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class NextDeparturesRepository {

    public record DepartureRow(String tripId, String routeId, String stopId, String departureTime) {}

    private final JdbcTemplate jdbc;

    public NextDeparturesRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<DepartureRow> findNextDepartures(
            String feedType,
            List<String> stopIds,
            String line,
            String fromTime,
            Set<String> serviceIds,
            int limit
    ) {
        if (limit <= 0) return List.of();
        if (serviceIds == null || serviceIds.isEmpty()) return List.of();
        if (stopIds == null || stopIds.isEmpty()) return List.of();
        if (line == null || line.isBlank()) return List.of();

        String stopIn = String.join(",", Collections.nCopies(stopIds.size(), "?"));
        String serviceIn = String.join(",", Collections.nCopies(serviceIds.size(), "?"));

        String sql = """
            SELECT t.trip_id,
                   t.route_id,
                   st.stop_id,
                   st.departure_time
            FROM trip t
            JOIN stop_time st
              ON st.trip_id = t.trip_id AND st.feed_type = t.feed_type
            JOIN route r
              ON r.route_id = t.route_id AND r.feed_type = t.feed_type
            WHERE t.feed_type = ?
              AND st.stop_id IN (%s)
              AND st.departure_time >= ?
              AND t.service_id IN (%s)
              AND (r.route_short_name = ? OR t.route_id = ?)
            ORDER BY st.departure_time ASC
            LIMIT ?
        """.formatted(stopIn, serviceIn);

        List<Object> params = new ArrayList<>();
        params.add(feedType);
        params.addAll(stopIds);
        params.add(fromTime);
        params.addAll(serviceIds);
        params.add(line);
        params.add(line);
        params.add(limit);

        return jdbc.query(sql, (rs, i) -> new DepartureRow(
                rs.getString("trip_id"),
                rs.getString("route_id"),
                rs.getString("stop_id"),
                rs.getString("departure_time")
        ), params.toArray());
    }
}