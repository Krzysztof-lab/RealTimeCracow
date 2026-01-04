package pl.edu.agh.to.realtimecracow.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.edu.agh.to.realtimecracow.model.entity.*;
import pl.edu.agh.to.realtimecracow.repository.*;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GtfsDataService {
    private static final Logger log = LoggerFactory.getLogger(GtfsDataService.class);

    private final StopRepository stopRepository;
    private final RouteRepository routeRepository;
    private final TripRepository tripRepository;
    private final StopTimeRepository stopTimeRepository;

    // In-memory cache for fast lookups
    private final Map<String, String> stopIdToName = new ConcurrentHashMap<>();
    private final Map<String, String> tripIdToRouteId = new ConcurrentHashMap<>();
    private final Map<String, Map<Integer, String>> tripIdToStopSeqToStopId = new ConcurrentHashMap<>();

    public GtfsDataService(StopRepository stopRepository,
                           RouteRepository routeRepository,
                           TripRepository tripRepository,
                           StopTimeRepository stopTimeRepository) {
        this.stopRepository = stopRepository;
        this.routeRepository = routeRepository;
        this.tripRepository = tripRepository;
        this.stopTimeRepository = stopTimeRepository;
    }

    @PostConstruct
    public void initializeCache() {
        refreshCache();
    }

    public void refreshCache() {
        log.info("Refreshing GTFS data cache from database...");

        stopIdToName.clear();
        tripIdToRouteId.clear();
        tripIdToStopSeqToStopId.clear();

        // Load stops
        stopRepository.findAll().forEach(stop ->
                stopIdToName.put(stop.getStopId(), stop.getStopName()));

        // Load trips -> routes mapping
        tripRepository.findAll().forEach(trip ->
                tripIdToRouteId.put(trip.getTripId(), trip.getRouteId()));

        // Load stop times
        stopTimeRepository.findAll().forEach(stopTime ->
                tripIdToStopSeqToStopId
                        .computeIfAbsent(stopTime.getTripId(), k -> new ConcurrentHashMap<>())
                        .put(stopTime.getStopSequence(), stopTime.getStopId()));

        log.info("Cache refreshed: {} stops, {} trips, {} stop time entries",
                stopIdToName.size(), tripIdToRouteId.size(), tripIdToStopSeqToStopId.size());
    }

    public String getStopName(String stopId) {
        return stopIdToName.get(stopId);
    }

    public String getRouteIdForTrip(String tripId) {
        return tripIdToRouteId.get(tripId);
    }

    public String getStopIdForTripAndSequence(String tripId, int stopSequence) {
        Map<Integer, String> seqToStop = tripIdToStopSeqToStopId.get(tripId);
        return seqToStop != null ? seqToStop.get(stopSequence) : null;
    }

    public Optional<Route> getRouteById(String routeId) {
        return routeRepository.findByRouteId(routeId);
    }

    public Optional<Stop> getStopById(String stopId) {
        return stopRepository.findByStopId(stopId);
    }

    public Optional<Trip> getTripById(String tripId) {
        return tripRepository.findByTripId(tripId);
    }

    public boolean hasData() {
        return stopRepository.count() > 0;
    }

    public long getStopCount() {
        return stopRepository.count();
    }

    public long getTripCount() {
        return tripRepository.count();
    }

    public long getRouteCount() {
        return routeRepository.count();
    }

    public long getStopTimeCount() {
        return stopTimeRepository.count();
    }
}
