package pl.edu.agh.to.realtimecracow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.to.realtimecracow.entity.Route;
import pl.edu.agh.to.realtimecracow.entity.Stop;
import pl.edu.agh.to.realtimecracow.entity.StopTime;
import pl.edu.agh.to.realtimecracow.entity.Trip;
import pl.edu.agh.to.realtimecracow.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GtfsDataService")
class GtfsDataServiceTest {

    @Mock
    private StopRepository stopRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private StopTimeRepository stopTimeRepository;

    @Mock
    private ServiceCalendarService serviceCalendarService;

    @Mock
    private DirectConnectionRepository directConnectionRepository;

    @InjectMocks
    private GtfsDataService service;

    @Nested
    @DisplayName("Cache initialization")
    class CacheInitializationTests {

        @Test
        @DisplayName("should load stops into cache")
        void shouldLoadStopsIntoCache() {
            Stop stop1 = createStop("S1", "Rynek Główny");
            Stop stop2 = createStop("S2", "Teatr Bagatela");

            when(stopRepository.findAll()).thenReturn(List.of(stop1, stop2));
            when(routeRepository.findAll()).thenReturn(List.of());
            when(tripRepository.findAll()).thenReturn(List.of());
            when(stopTimeRepository.findAll()).thenReturn(List.of());

            service.refreshCache();

            assertEquals("Rynek Główny", service.getStopName("S1"));
            assertEquals("Teatr Bagatela", service.getStopName("S2"));
        }

        @Test
        @DisplayName("should load routes into cache")
        void shouldLoadRoutesIntoCache() {
            Route route1 = createRoute("R1", "152");
            Route route2 = createRoute("R2", "M1");

            when(stopRepository.findAll()).thenReturn(List.of());
            when(routeRepository.findAll()).thenReturn(List.of(route1, route2));
            when(tripRepository.findAll()).thenReturn(List.of());
            when(stopTimeRepository.findAll()).thenReturn(List.of());

            service.refreshCache();

            assertEquals("152", service.getRouteShortName("R1"));
            assertEquals("M1", service.getRouteShortName("R2"));
        }

        @Test
        @DisplayName("should load trips into cache")
        void shouldLoadTripsIntoCache() {
            Trip trip1 = createTrip("T1", "R1");
            Trip trip2 = createTrip("T2", "R2");

            when(stopRepository.findAll()).thenReturn(List.of());
            when(routeRepository.findAll()).thenReturn(List.of());
            when(tripRepository.findAll()).thenReturn(List.of(trip1, trip2));
            when(stopTimeRepository.findAll()).thenReturn(List.of());

            service.refreshCache();

            assertEquals("R1", service.getRouteIdForTrip("T1"));
            assertEquals("R2", service.getRouteIdForTrip("T2"));
        }

        @Test
        @DisplayName("should load stop times into cache")
        void shouldLoadStopTimesIntoCache() {
            StopTime st1 = createStopTime("T1", "S1", 1);
            StopTime st2 = createStopTime("T1", "S2", 2);

            when(stopRepository.findAll()).thenReturn(List.of());
            when(routeRepository.findAll()).thenReturn(List.of());
            when(tripRepository.findAll()).thenReturn(List.of());
            when(stopTimeRepository.findAll()).thenReturn(List.of(st1, st2));

            service.refreshCache();

            assertEquals("S1", service.getStopIdForTripAndSequence("T1", 1));
            assertEquals("S2", service.getStopIdForTripAndSequence("T1", 2));
        }

        @Test
        @DisplayName("should handle multiple stops with same name")
        void shouldHandleMultipleStopsWithSameName() {
            Stop stop1 = createStop("S1", "Dworzec");
            Stop stop2 = createStop("S2", "Dworzec");

            when(stopRepository.findAll()).thenReturn(List.of(stop1, stop2));
            when(routeRepository.findAll()).thenReturn(List.of());
            when(tripRepository.findAll()).thenReturn(List.of());
            when(stopTimeRepository.findAll()).thenReturn(List.of());

            service.refreshCache();

            List<String> stopIds = service.getStopIdsByStopName("Dworzec");
            assertEquals(2, stopIds.size());
            assertTrue(stopIds.contains("S1"));
            assertTrue(stopIds.contains("S2"));
        }

        @Test
        @DisplayName("should clear old cache data on refresh")
        void shouldClearOldCacheDataOnRefresh() {
            Stop stop1 = createStop("S1", "Old Stop");
            when(stopRepository.findAll()).thenReturn(List.of(stop1));
            when(routeRepository.findAll()).thenReturn(List.of());
            when(tripRepository.findAll()).thenReturn(List.of());
            when(stopTimeRepository.findAll()).thenReturn(List.of());

            service.refreshCache();
            assertEquals("Old Stop", service.getStopName("S1"));

            Stop stop2 = createStop("S2", "New Stop");
            when(stopRepository.findAll()).thenReturn(List.of(stop2));

            service.refreshCache();
            assertNull(service.getStopName("S1"));
            assertEquals("New Stop", service.getStopName("S2"));
        }
    }

    @Nested
    @DisplayName("Data retrieval")
    class DataRetrievalTests {

        @BeforeEach
        void setUp() {
            when(stopRepository.findAll()).thenReturn(List.of());
            when(routeRepository.findAll()).thenReturn(List.of());
            when(tripRepository.findAll()).thenReturn(List.of());
            when(stopTimeRepository.findAll()).thenReturn(List.of());
            service.refreshCache();
        }

        @Test
        @DisplayName("should return null for non-existent stop")
        void shouldReturnNullForNonExistentStop() {
            assertNull(service.getStopName("NONEXISTENT"));
        }

        @Test
        @DisplayName("should return null for non-existent route")
        void shouldReturnNullForNonExistentRoute() {
            assertNull(service.getRouteShortName("NONEXISTENT"));
        }

        @Test
        @DisplayName("should return null for non-existent trip")
        void shouldReturnNullForNonExistentTrip() {
            assertNull(service.getRouteIdForTrip("NONEXISTENT"));
        }

        @Test
        @DisplayName("should return null for non-existent stop sequence")
        void shouldReturnNullForNonExistentStopSequence() {
            assertNull(service.getStopIdForTripAndSequence("T1", 999));
        }

        @Test
        @DisplayName("should return empty list for non-existent stop name")
        void shouldReturnEmptyListForNonExistentStopName() {
            List<String> result = service.getStopIdsByStopName("NonExistent");
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Data count methods")
    class DataCountTests {

        @Test
        @DisplayName("should return correct stop count")
        void shouldReturnCorrectStopCount() {
            when(stopRepository.count()).thenReturn(150L);

            assertEquals(150L, service.getStopCount());
        }

        @Test
        @DisplayName("should return correct trip count")
        void shouldReturnCorrectTripCount() {
            when(tripRepository.count()).thenReturn(500L);

            assertEquals(500L, service.getTripCount());
        }

        @Test
        @DisplayName("should return correct route count")
        void shouldReturnCorrectRouteCount() {
            when(routeRepository.count()).thenReturn(25L);

            assertEquals(25L, service.getRouteCount());
        }

        @Test
        @DisplayName("should return true when data exists")
        void shouldReturnTrueWhenDataExists() {
            when(stopRepository.count()).thenReturn(100L);

            assertTrue(service.hasData());
        }

        @Test
        @DisplayName("should return false when no data exists")
        void shouldReturnFalseWhenNoDataExists() {
            when(stopRepository.count()).thenReturn(0L);

            assertFalse(service.hasData());
        }
    }

    @Nested
    @DisplayName("Active service IDs")
    class ActiveServiceIdsTests {

        @Test
        @DisplayName("should delegate to ServiceCalendarService")
        void shouldDelegateToServiceCalendarService() {
            String feedType = "M";
            LocalDate date = LocalDate.of(2026, 1, 9);
            Set<String> expected = Set.of("S1", "S2");

            when(serviceCalendarService.activeServiceIds(feedType, date)).thenReturn(expected);

            Set<String> result = service.getActiveServiceIds(feedType, date);

            assertEquals(expected, result);
            verify(serviceCalendarService).activeServiceIds(feedType, date);
        }
    }

    @Nested
    @DisplayName("Direct trip search")
    class DirectTripSearchTests {

        @Test
        @DisplayName("should find best direct trip")
        void shouldFindBestDirectTrip() {
            String feedType = "M";
            List<String> fromStops = List.of("S1");
            List<String> toStops = List.of("S2");
            LocalDateTime at = LocalDateTime.of(2026, 1, 9, 12, 0);
            Set<String> serviceIds = Set.of("WD1");

            DirectConnectionRepository.DirectTripRow row =
                    new DirectConnectionRepository.DirectTripRow("T1", "R1", "12:30:00", "12:45:00");

            when(directConnectionRepository.findBestDirectTrip(
                    feedType, fromStops, toStops, "12:00:00", serviceIds))
                    .thenReturn(row);

            GtfsDataService.DirectTripResult result =
                    service.findBestDirectTrip(feedType, fromStops, toStops, at, serviceIds);

            assertNotNull(result);
            assertEquals("T1", result.tripId());
            assertEquals("R1", result.routeId());
            assertEquals("12:30:00", result.departureTime());
            assertEquals("12:45:00", result.arrivalTime());
        }

        @Test
        @DisplayName("should return null when no direct trip found")
        void shouldReturnNullWhenNoDirectTripFound() {
            String feedType = "M";
            List<String> fromStops = List.of("S1");
            List<String> toStops = List.of("S2");
            LocalDateTime at = LocalDateTime.of(2026, 1, 9, 23, 55);
            Set<String> serviceIds = Set.of("WD1");

            when(directConnectionRepository.findBestDirectTrip(
                    feedType, fromStops, toStops, "23:55:00", serviceIds))
                    .thenReturn(null);

            GtfsDataService.DirectTripResult result =
                    service.findBestDirectTrip(feedType, fromStops, toStops, at, serviceIds);

            assertNull(result);
        }

        @Test
        @DisplayName("should format time correctly for query")
        void shouldFormatTimeCorrectlyForQuery() {
            String feedType = "M";
            List<String> fromStops = List.of("S1");
            List<String> toStops = List.of("S2");
            LocalDateTime at = LocalDateTime.of(2026, 1, 9, 8, 5, 30);
            Set<String> serviceIds = Set.of("WD1");

            service.findBestDirectTrip(feedType, fromStops, toStops, at, serviceIds);

            verify(directConnectionRepository).findBestDirectTrip(
                    feedType, fromStops, toStops, "08:05:30", serviceIds);
        }
    }

    // Helper methods
    private Stop createStop(String stopId, String stopName) {
        Stop stop = new Stop();
        stop.setStopId(stopId);
        stop.setStopName(stopName);
        return stop;
    }

    private Route createRoute(String routeId, String shortName) {
        Route route = new Route();
        route.setRouteId(routeId);
        route.setRouteShortName(shortName);
        return route;
    }

    private Trip createTrip(String tripId, String routeId) {
        Trip trip = new Trip();
        trip.setTripId(tripId);
        trip.setRouteId(routeId);
        return trip;
    }

    private StopTime createStopTime(String tripId, String stopId, int sequence) {
        StopTime st = new StopTime();
        st.setTripId(tripId);
        st.setStopId(stopId);
        st.setStopSequence(sequence);
        return st;
    }
}