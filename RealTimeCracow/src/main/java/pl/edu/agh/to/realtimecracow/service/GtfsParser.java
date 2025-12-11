package pl.edu.agh.to.realtimecracow.service;

import com.google.transit.realtime.GtfsRealtime;
import jakarta.annotation.PostConstruct;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import pl.edu.agh.to.realtimecracow.model.StopInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class GtfsParser {
    private final String gtfsFolder;
    private static final List<Character> AGENCY_TYPES = List.of('A', 'M', 'T');

    // Data structures to hold GTFS data (Cached in memory)
    private final Map<String, String> stopIdToName = new HashMap<>();
    private final Map<String, String> tripIdToRoute = new HashMap<>();
    private final Map<String, Map<String, String>> tripIdToStopSeqToStopId = new HashMap<>();

    private final CSVFormat format = CSVFormat.Builder.create()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreSurroundingSpaces(true)
            .setTrim(true)
            .build();

    public GtfsParser(@Value("${gtfs.gtfs-folder}") String gtfsFolder) {
        this.gtfsFolder = gtfsFolder;
    }

    @PostConstruct
    public void loadGtfsData() throws IOException {
        loadStops();
        loadTrips();
        loadStopTimes();
    }

    private void loadStops() throws IOException {
        for (char type : AGENCY_TYPES) {
            ClassPathResource resource = new ClassPathResource(gtfsFolder + type + "/stops.txt");
            try (InputStream in = BOMInputStream.builder()
                    .setInputStream(resource.getInputStream())
                    .get();
                 Reader reader = new InputStreamReader(in);
                 CSVParser parser = new CSVParser(reader, format)) {
                for (CSVRecord CSVrecord : parser) {
                    stopIdToName.put(CSVrecord.get("stop_id"), CSVrecord.get("stop_name"));
                }
            }
        }
    }

    private void loadTrips() throws IOException {
        for (char type : AGENCY_TYPES) {
            ClassPathResource resource = new ClassPathResource(gtfsFolder + type + "/trips.txt");
            try (InputStream in = BOMInputStream.builder()
                    .setInputStream(resource.getInputStream())
                    .get();
                 Reader reader = new InputStreamReader(in);
                 CSVParser parser = new CSVParser(reader, format)) {
                for (CSVRecord CSVrecord : parser) {
                    tripIdToRoute.put(CSVrecord.get("trip_id"), CSVrecord.get("route_id"));
                }
            }
        }
    }

    private void loadStopTimes() throws IOException {
        for (char type : AGENCY_TYPES) {
            ClassPathResource resource = new ClassPathResource(gtfsFolder + type + "/stop_times.txt");
            try (InputStream in = BOMInputStream.builder()
                    .setInputStream(resource.getInputStream())
                    .get();
                 Reader reader = new InputStreamReader(in);
                 CSVParser parser = new CSVParser(reader, format)) {
                for (CSVRecord CSVrecord : parser) {
                    String tripId = CSVrecord.get("trip_id");
                    String stopSeq = CSVrecord.get("stop_sequence");
                    String stopId = CSVrecord.get("stop_id");

                    tripIdToStopSeqToStopId
                            .computeIfAbsent(tripId, _ -> new HashMap<>())
                            .put(stopSeq, stopId);
                }
            }
        }
    }

    public List<StopInfo> getStopListForEntity(GtfsRealtime.FeedEntity entity) {
        List<StopInfo> stops = new ArrayList<>();
        for (GtfsRealtime.TripUpdate.StopTimeUpdate stopTimeUpdate : entity.getTripUpdate().getStopTimeUpdateList()) {
            if (stopTimeUpdate.hasDeparture()) {
                long departureTimestamp = stopTimeUpdate.getDeparture().getTime(); //no optional here, hasDeparture() checked
                String formattedDepartureTime = Instant.ofEpochSecond(departureTimestamp)
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                long arrivalTimestamp = stopTimeUpdate.getArrival().getTime(); //no optional here, if it hasDeparture(), it hasArrival()
                String formattedArrivalTime = Instant.ofEpochSecond(arrivalTimestamp)
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("HH:mm:ss"));


                String tripId = extractTripId(entity.getId());
                String stopId = tripIdToStopSeqToStopId.getOrDefault(tripId, Collections.emptyMap())
                        .get(String.valueOf(stopTimeUpdate.getStopSequence()));
                String stopName = stopIdToName.get(stopId);

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

    public String getLineNumber(String id) {
        return tripIdToRoute.get(extractTripId(id));
    }

    private String extractTripId(String entityId) {
        return entityId.substring(7);
    }
}
