package pl.edu.agh.to.realtimecracow.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.edu.agh.to.realtimecracow.repository.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class GtfsDataService {
    private static final Logger log = LoggerFactory.getLogger(GtfsDataService.class);

    private final StopRepository stopRepository;
    private final RouteRepository routeRepository;
    private final TripRepository tripRepository;
    private final StopTimeRepository stopTimeRepository;

    private final ServiceCalendarService serviceCalendarService;
    private final DirectConnectionRepository directConnectionRepository;

    // In-memory cache for fast lookups
    private final Map<String, String> stopIdToName = new ConcurrentHashMap<>();
    private final Map<String, String> tripIdToRouteId = new ConcurrentHashMap<>();
    private final Map<String, Map<Integer, String>> tripIdToStopSeqToStopId = new ConcurrentHashMap<>();
    private final Map<String, List<String>> stopNameToIds = new ConcurrentHashMap<>();
    private final Map<String, String> routeIdToShortName = new ConcurrentHashMap<>();

    public GtfsDataService(StopRepository stopRepository,
                           RouteRepository routeRepository,
                           TripRepository tripRepository,
                           StopTimeRepository stopTimeRepository,ServiceCalendarService serviceCalendarService,
                           DirectConnectionRepository directConnectionRepository ) {
        this.stopRepository = stopRepository;
        this.routeRepository = routeRepository;
        this.tripRepository = tripRepository;
        this.stopTimeRepository = stopTimeRepository;
        this.serviceCalendarService = serviceCalendarService;
        this.directConnectionRepository = directConnectionRepository;
    }

    @PostConstruct
    public void initializeCache() {
        refreshCache();
    }

    public void refreshCache() {

        log.info("Refreshing GTFS data cache from database...");

        stopNameToIds.clear();
        stopIdToName.clear();
        tripIdToRouteId.clear();
        tripIdToStopSeqToStopId.clear();
        routeIdToShortName.clear();

        // Load stops
        stopRepository.findAll().forEach(stop -> {
            stopIdToName.put(stop.getStopId(), stop.getStopName());
            stopNameToIds.computeIfAbsent(stop.getStopName(), k -> new ArrayList<>()).add(stop.getStopId());
        });


        // Load routes
        routeRepository.findAll().forEach(route ->
                routeIdToShortName.put(route.getRouteId(), route.getRouteShortName()));

        // Load trips
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

    public String getRouteShortName(String routeId) {
        return routeIdToShortName.get(routeId);
    }

    public String getStopIdForTripAndSequence(String tripId, int stopSequence) {
        Map<Integer, String> seqToStop = tripIdToStopSeqToStopId.get(tripId);
        return seqToStop != null ? seqToStop.get(stopSequence) : null;
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

    public List<String> getStopIdsByStopName(String stopName) {
        return stopNameToIds.getOrDefault(stopName, Collections.emptyList());
    }

    public Set<String> getActiveServiceIds(String feedType, LocalDate date) {
        return serviceCalendarService.activeServiceIds(feedType, date);
    }
    public record DirectTripResult(String tripId, String routeId, String departureTime, String arrivalTime) {}

    public DirectTripResult findBestDirectTrip(String feedType, List<String> fromStopIds, List<String> toStopIds,
                                               LocalDateTime at, Set<String> serviceIds) {
        String fromTime = at.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        DirectConnectionRepository.DirectTripRow row =
                directConnectionRepository.findBestDirectTrip(feedType, fromStopIds, toStopIds, fromTime, serviceIds);
        if (row == null) return null;

        return new DirectTripResult(row.tripId(), row.routeId(), row.departureTime(), row.arrivalTime());
    }

}
