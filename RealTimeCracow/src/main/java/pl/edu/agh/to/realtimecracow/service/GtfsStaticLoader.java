package pl.edu.agh.to.realtimecracow.service;

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
public class GtfsStaticLoader {
    private static final Logger log = LoggerFactory.getLogger(GtfsStaticLoader.class);
    private static final int BATCH_SIZE = 1000;

    private final StopRepository stopRepository;
    private final RouteRepository routeRepository;
    private final TripRepository tripRepository;
    private final StopTimeRepository stopTimeRepository;

    private final CSVFormat csvFormat = CSVFormat.Builder.create()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreSurroundingSpaces(true)
            .setTrim(true)
            .build();

    public GtfsStaticLoader(StopRepository stopRepository,
                            RouteRepository routeRepository,
                            TripRepository tripRepository,
                            StopTimeRepository stopTimeRepository) {
        this.stopRepository = stopRepository;
        this.routeRepository = routeRepository;
        this.tripRepository = tripRepository;
        this.stopTimeRepository = stopTimeRepository;
    }

    @Transactional
    public void loadFromZip(Path zipPath, String feedType) throws IOException {
        log.info("Loading GTFS data from {} for feed type {}", zipPath, feedType);

        clearFeedData(feedType);

        try (ZipFile zipFile = ZipFile.builder().setPath(zipPath).get()) {
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                String name = entry.getName();

                if (name.endsWith("stops.txt")) {
                    loadStops(zipFile.getInputStream(entry), feedType);
                } else if (name.endsWith("routes.txt")) {
                    loadRoutes(zipFile.getInputStream(entry), feedType);
                } else if (name.endsWith("trips.txt")) {
                    loadTrips(zipFile.getInputStream(entry), feedType);
                } else if (name.endsWith("stop_times.txt")) {
                    loadStopTimes(zipFile.getInputStream(entry), feedType);
                }
            }
        }

        log.info("Finished loading GTFS data for feed type {}", feedType);
    }

    @Transactional
    public void clearFeedData(String feedType) {
        log.info("Clearing existing data for feed type {}", feedType);
        stopTimeRepository.deleteByFeedType(feedType);
        tripRepository.deleteByFeedType(feedType);
        routeRepository.deleteByFeedType(feedType);
        stopRepository.deleteByFeedType(feedType);
    }

    private void loadStops(InputStream inputStream, String feedType) throws IOException {
        log.info("Loading stops for feed type {}", feedType);
        List<Stop> batch = new ArrayList<>();
        int count = 0;

        try (Reader reader = new InputStreamReader(
                BOMInputStream.builder().setInputStream(inputStream).get(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, csvFormat)) {

            for (CSVRecord record : parser) {
                Stop stop = new Stop();
                stop.setStopId(record.get("stop_id"));
                stop.setFeedType(feedType);
                stop.setStopName(getOptionalField(record, "stop_name"));
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

            for (CSVRecord record : parser) {
                Route route = new Route();
                route.setRouteId(record.get("route_id"));
                route.setFeedType(feedType);

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

            for (CSVRecord record : parser) {
                Trip trip = new Trip();
                trip.setTripId(record.get("trip_id"));
                trip.setFeedType(feedType);
                trip.setRouteId(getOptionalField(record, "route_id"));

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

            for (CSVRecord record : parser) {
                StopTime stopTime = new StopTime();
                stopTime.setTripId(record.get("trip_id"));
                stopTime.setStopSequence(parseIntOrNull(record.get("stop_sequence")));
                stopTime.setFeedType(feedType);
                stopTime.setStopId(getOptionalField(record, "stop_id"));

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

    private String getOptionalField(CSVRecord record, String field) {
        try {
            return record.get(field);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Integer parseIntOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
