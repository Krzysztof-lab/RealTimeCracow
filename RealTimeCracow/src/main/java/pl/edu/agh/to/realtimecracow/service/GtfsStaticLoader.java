package pl.edu.agh.to.realtimecracow.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.agh.to.realtimecracow.config.GtfsFileNamesConfig;
import pl.edu.agh.to.realtimecracow.entity.*;
import pl.edu.agh.to.realtimecracow.repository.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GtfsStaticLoader {
    private static final Logger log = LoggerFactory.getLogger(GtfsStaticLoader.class);
    private static final int BATCH_SIZE = 1000;

    private final StopRepository stopRepository;
    private final RouteRepository routeRepository;
    private final TripRepository tripRepository;
    private final StopTimeRepository stopTimeRepository;
    private final ServiceCalendarRepository serviceCalendarRepository;
    private final ServiceCalendarDateRepository serviceCalendarDateRepository;
    private final GtfsFileNamesConfig fileNamesConfig;

    // GTFS field names
    private static final String FIELD_SERVICE_ID = "service_id";
    private static final String FIELD_STOP_ID = "stop_id";
    private static final String FIELD_STOP_NAME = "stop_name";
    private static final String FIELD_ROUTE_ID = "route_id";
    private static final String FIELD_ROUTE_SHORT_NAME = "route_short_name";
    private static final String FIELD_TRIP_ID = "trip_id";
    private static final String FIELD_STOP_SEQUENCE = "stop_sequence";
    private static final String FIELD_ARRIVAL_TIME = "arrival_time";
    private static final String FIELD_DEPARTURE_TIME = "departure_time";
    private static final String FIELD_EXCEPTION_TYPE = "exception_type";
    private static final String FIELD_DATE = "date";


    private final CSVFormat csvFormat = CSVFormat.Builder.create()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreSurroundingSpaces(true)
            .setTrim(true)
            .build();


    @Transactional
    public void loadFromZip(Path zipPath, String feedType) throws IOException {
        log.info("Loading GTFS data from {} for feed type {}", zipPath, feedType);

        clearFeedData(feedType);

        try (ZipFile zipFile = ZipFile.builder().setPath(zipPath).get()) {
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                String name = entry.getName();

                switch (name) {
                    case String n when n.endsWith(fileNamesConfig.getStops()) ->
                            loadStops(zipFile.getInputStream(entry), feedType);
                    case String n when n.endsWith(fileNamesConfig.getRoutes()) ->
                            loadRoutes(zipFile.getInputStream(entry), feedType);
                    case String n when n.endsWith(fileNamesConfig.getTrips()) ->
                            loadTrips(zipFile.getInputStream(entry), feedType);
                    case String n when n.endsWith(fileNamesConfig.getStopTimes()) ->
                            loadStopTimes(zipFile.getInputStream(entry), feedType);
                    case String n when n.endsWith(fileNamesConfig.getCalendar()) ->
                            loadServiceCalendar(zipFile.getInputStream(entry), feedType);
                    case String n when n.endsWith(fileNamesConfig.getCalendarDates()) ->
                            loadServiceCalendarDates(zipFile.getInputStream(entry), feedType);
                    default -> { /* Ignore other files in the GTFS archive */ }
                }
            }
        }

        log.info("Finished loading GTFS data for feed type {}", feedType);
    }

    public void clearFeedData(String feedType) {
        log.info("Clearing existing data for feed type {}", feedType);
        stopTimeRepository.deleteByFeedType(feedType);
        tripRepository.deleteByFeedType(feedType);
        routeRepository.deleteByFeedType(feedType);
        stopRepository.deleteByFeedType(feedType);
        serviceCalendarDateRepository.deleteByFeedType(feedType);
        serviceCalendarRepository.deleteByFeedType(feedType);
    }


    private void loadStops(InputStream inputStream, String feedType) throws IOException {
        log.info("Loading stops for feed type {}", feedType);
        List<Stop> batch = new ArrayList<>();
        int count = 0;

        try (Reader reader = new InputStreamReader(
                BOMInputStream.builder().setInputStream(inputStream).get(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, csvFormat)) {

            for (CSVRecord csvRecord : parser) {
                Stop stop = new Stop();
                stop.setStopId(csvRecord.get(FIELD_STOP_ID));
                stop.setFeedType(feedType);
                stop.setStopName(getOptionalField(csvRecord, FIELD_STOP_NAME));
                batch.add(stop);
                count++;

                if (batch.size() >= BATCH_SIZE) {
                    stopRepository.saveAll(batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                stopRepository.saveAll(batch);
            }
        }
        log.info("Loaded {} stops for feed type {}", count, feedType);
    }

    private void loadRoutes(InputStream inputStream, String feedType) throws IOException {
        log.info("Loading routes for feed type {}", feedType);
        List<Route> batch = new ArrayList<>();
        int count = 0;

        try (Reader reader = new InputStreamReader(
                BOMInputStream.builder().setInputStream(inputStream).get(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, csvFormat)) {

            for (CSVRecord csvRecord : parser) {
                Route route = new Route();
                route.setRouteId(csvRecord.get(FIELD_ROUTE_ID));
                route.setFeedType(feedType);
                route.setRouteShortName(getOptionalField(csvRecord, FIELD_ROUTE_SHORT_NAME));

                batch.add(route);
                count++;

                if (batch.size() >= BATCH_SIZE) {
                    routeRepository.saveAll(batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                routeRepository.saveAll(batch);
            }
        }
        log.info("Loaded {} routes for feed type {}", count, feedType);
    }

    private void loadTrips(InputStream inputStream, String feedType) throws IOException {
        log.info("Loading trips for feed type {}", feedType);
        List<Trip> batch = new ArrayList<>();
        int count = 0;

        try (Reader reader = new InputStreamReader(
                BOMInputStream.builder().setInputStream(inputStream).get(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, csvFormat)) {

            for (CSVRecord csvRecord : parser) {
                Trip trip = new Trip();
                trip.setTripId(csvRecord.get(FIELD_TRIP_ID));
                trip.setFeedType(feedType);
                trip.setRouteId(getOptionalField(csvRecord, FIELD_ROUTE_ID));
                trip.setServiceId(getOptionalField(csvRecord, FIELD_SERVICE_ID));

                batch.add(trip);
                count++;

                if (batch.size() >= BATCH_SIZE) {
                    tripRepository.saveAll(batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                tripRepository.saveAll(batch);
            }
        }
        log.info("Loaded {} trips for feed type {}", count, feedType);
    }

    private void loadStopTimes(InputStream inputStream, String feedType) throws IOException {
        log.info("Loading stop times for feed type {}", feedType);
        List<StopTime> batch = new ArrayList<>();
        int count = 0;

        try (Reader reader = new InputStreamReader(
                BOMInputStream.builder().setInputStream(inputStream).get(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, csvFormat)) {

            for (CSVRecord csvRecord : parser) {
                StopTime stopTime = new StopTime();
                stopTime.setTripId(csvRecord.get(FIELD_TRIP_ID));
                stopTime.setStopSequence(parseIntOrNull(csvRecord.get(FIELD_STOP_SEQUENCE)));
                stopTime.setFeedType(feedType);
                stopTime.setStopId(getOptionalField(csvRecord, FIELD_STOP_ID));
                stopTime.setArrivalTime(getOptionalField(csvRecord, FIELD_ARRIVAL_TIME));
                stopTime.setDepartureTime(getOptionalField(csvRecord, FIELD_DEPARTURE_TIME));

                batch.add(stopTime);
                count++;

                if (batch.size() >= BATCH_SIZE) {
                    stopTimeRepository.saveAll(batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                stopTimeRepository.saveAll(batch);
            }
        }
        log.info("Loaded {} stop times for feed type {}", count, feedType);
    }

    private String getOptionalField(CSVRecord csvRecord, String field) {
        try {
            return csvRecord.get(field);
        } catch (IllegalArgumentException _) {
            // Field doesn't exist in CSV - this is expected for optional fields
            return null;
        }
    }

    private Integer parseIntOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException _) {
            // Invalid number format - treat as null
            return null;
        }
    }

private void loadServiceCalendar(InputStream inputStream, String feedType) throws IOException {
    List<ServiceCalendar> batch = new ArrayList<>();

    try (Reader reader = new InputStreamReader(
            BOMInputStream.builder().setInputStream(inputStream).get(), StandardCharsets.UTF_8);
         CSVParser parser = new CSVParser(reader, csvFormat)) {

        for (CSVRecord r : parser) {
            ServiceCalendar c = new ServiceCalendar();
            c.setFeedType(feedType);
            c.setServiceId(r.get(FIELD_SERVICE_ID));
            c.setMonday(Integer.parseInt(r.get("monday")));
            c.setTuesday(Integer.parseInt(r.get("tuesday")));
            c.setWednesday(Integer.parseInt(r.get("wednesday")));
            c.setThursday(Integer.parseInt(r.get("thursday")));
            c.setFriday(Integer.parseInt(r.get("friday")));
            c.setSaturday(Integer.parseInt(r.get("saturday")));
            c.setSunday(Integer.parseInt(r.get("sunday")));
            c.setStartDate(r.get("start_date"));
            c.setEndDate(r.get("end_date"));

            batch.add(c);

            if (batch.size() >= BATCH_SIZE) {
                serviceCalendarRepository.saveAll(batch);
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            serviceCalendarRepository.saveAll(batch);
        }
    }
}

private void loadServiceCalendarDates(InputStream inputStream, String feedType) throws IOException {
    List<ServiceCalendarDate> batch = new ArrayList<>();

    try (Reader reader = new InputStreamReader(
            BOMInputStream.builder().setInputStream(inputStream).get(), StandardCharsets.UTF_8);
         CSVParser parser = new CSVParser(reader, csvFormat)) {

        for (CSVRecord r : parser) {
            ServiceCalendarDate d = new ServiceCalendarDate();
            d.setFeedType(feedType);
            d.setServiceId(r.get(FIELD_SERVICE_ID));
            d.setDate(r.get(FIELD_DATE));
            d.setExceptionType(Integer.parseInt(r.get(FIELD_EXCEPTION_TYPE)));

            batch.add(d);
            if (batch.size() >= BATCH_SIZE) {
                serviceCalendarDateRepository.saveAll(batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            serviceCalendarDateRepository.saveAll(batch);
        }
    }
}
}