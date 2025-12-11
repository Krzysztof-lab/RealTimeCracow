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
    private final CSVFormat format = CSVFormat.Builder.create()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreSurroundingSpaces(true)
            .setTrim(true)
            .build();

    private final Map<String, String> stopIdToName = new HashMap<>();
    private final Map<String, String> tripIdToRoute = new HashMap<>();
    private final Map<String, Map<String, String>> tripIdToStopSeqToStopId = new HashMap<>();


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
        for (char type : List.of('A', 'M', 'T')) {
            ClassPathResource resource = new ClassPathResource(gtfsFolder + type + "/stops.txt");
            try (InputStream in = new BOMInputStream(resource.getInputStream());
                 Reader reader = new InputStreamReader(in);
                 CSVParser parser = new CSVParser(reader, format)) {
                for (CSVRecord record : parser) {
                    stopIdToName.put(record.get("stop_id"), record.get("stop_name"));
                }
            }
        }
    }

    private void loadTrips() throws IOException {
        for (char type : List.of('A', 'M', 'T')) {
            ClassPathResource resource = new ClassPathResource(gtfsFolder + type + "/trips.txt");
            try (InputStream in = new BOMInputStream(resource.getInputStream());
                 Reader reader = new InputStreamReader(in);
                 CSVParser parser = new CSVParser(reader, format)) {
                for (CSVRecord record : parser) {
                    tripIdToRoute.put(record.get("trip_id"), record.get("route_id"));
                }
            }
        }
    }

    private void loadStopTimes() throws IOException {
        for (char type : List.of('A', 'M', 'T')) {
            ClassPathResource resource = new ClassPathResource(gtfsFolder + type + "/stop_times.txt");
            try (InputStream in = new BOMInputStream(resource.getInputStream());
                 Reader reader = new InputStreamReader(in);
                 CSVParser parser = new CSVParser(reader, format)) {
                for (CSVRecord record : parser) {
                    String tripId = record.get("trip_id");
                    String stopSeq = record.get("stop_sequence");
                    String stopId = record.get("stop_id");

                    tripIdToStopSeqToStopId
                            .computeIfAbsent(tripId, k -> new HashMap<>())
                            .put(stopSeq, stopId);
                }
            }
        }
    }


    public List<StopInfo> getStopListForEntity(GtfsRealtime.FeedEntity entity) throws IOException {
        List<StopInfo> stops = new ArrayList<>();
        for (GtfsRealtime.TripUpdate.StopTimeUpdate stopTimeUpdate : entity.getTripUpdate().getStopTimeUpdateList()) {
            if (stopTimeUpdate.hasDeparture()) {
                long time = stopTimeUpdate.getDeparture().getTime();
                Instant departureTime = Instant.ofEpochSecond(time);
                String formattedDepartureTime = departureTime.atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                time = stopTimeUpdate.getArrival().getTime();
                Instant arrivalTime = Instant.ofEpochSecond(time);
                String formattedArrivalTime = arrivalTime.atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("HH:mm:ss"));


//                String stopId = getStopId(String.valueOf(stopTimeUpdate.getStopSequence()), entity.getId());
//
//                stops.add(new StopInfo(
//                        stopId,
//                        getStopName(stopId, entity.getId()),
//                        formattedArrivalTime,
//                        formattedDepartureTime,
//                        stopTimeUpdate.getStopSequence()
//                ));
//            }
//        }
//        return stops;
                String tripId = entity.getId().substring(7);
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

    private String getStopId(String stopSequence, String id) throws IOException {
        char type = id.charAt(0);
        String line_id = id.substring(7);
        ClassPathResource resource = new ClassPathResource(gtfsFolder + type + "/stop_times.txt");

        try (InputStream in = new BOMInputStream(resource.getInputStream());
             Reader reader = new InputStreamReader(in);
             CSVParser parser = new CSVParser(reader, format)) {

            for (CSVRecord record : parser) {
                if (record.get("trip_id").equals(line_id)
                        && record.get("stop_sequence").equals(stopSequence)) {
                    return record.get("stop_id");
                }
            }
        }
        return null;
    }

    private String getStopName(String stopId, String id) throws IOException {
        char type = id.charAt(0);
        ClassPathResource resource = new ClassPathResource(gtfsFolder + type + "/stops.txt");

        try (InputStream in = new BOMInputStream(resource.getInputStream());
             Reader reader = new InputStreamReader(in);
             CSVParser parser = new CSVParser(reader, format)) {

            for (CSVRecord record : parser) {
                if (record.get("stop_id").equals(stopId)) {
                    return record.get("stop_name");
                }
            }
        }
        return null;
    }

    public String getLineNumber(String id) throws IOException {
//        char type = id.charAt(0);
//        String line_id = id.substring(7);
//        ClassPathResource resource = new ClassPathResource(gtfsFolder + type + "/trips.txt");
//
//        try (InputStream in = new BOMInputStream(resource.getInputStream());
//             Reader reader = new InputStreamReader(in);
//             CSVParser parser = new CSVParser(reader, format)) {
//
//            for (CSVRecord record : parser) {
//
//                String tripId = record.get("trip_id");
//                if (tripId.contains(line_id)) {
//                    return record.get("route_id");
//                }
//            }
//        }
//        return null;
//    }

        return tripIdToRoute.get(id.substring(7));
    }
}
