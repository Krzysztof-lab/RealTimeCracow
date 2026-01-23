package pl.edu.agh.to.realtimecracow.service;

import com.google.transit.realtime.GtfsRealtime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import pl.edu.agh.to.realtimecracow.model.Departure;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TripService")
class TripServiceTest {

    @Mock
    private GtfsClient gtfsClient;

    @Mock
    private GtfsDataService gtfsDataService;

    private TripService tripService;

    @BeforeEach
    void setUp() {
        tripService = new TripService(gtfsClient, gtfsDataService);
    }

    @Nested
    @DisplayName("getNextDirectDeparture()")
    class GetNextDirectDepartureTests {

        @Test
        @DisplayName("Should return departure with realtime data when available")
        void shouldReturnDepartureWithRealtimeData() throws IOException {
            // Given
            String feedType = "M";
            String fromStopName = "Rynek Główny";
            String toStopName = "Teatr Bagatela";
            LocalDateTime at = LocalDateTime.of(2026, 1, 14, 12, 0);

            List<String> fromStopIds = List.of("stop_4825", "stop_4826");
            List<String> toStopIds = List.of("stop_1122");
            Set<String> activeServiceIds = Set.of("service_weekday");

            GtfsDataService.DirectTripResult bestTrip =
                    new GtfsDataService.DirectTripResult("trip_456", "route_152", "12:30:00", "12:45:00");

            when(gtfsDataService.getStopIdsByStopName(fromStopName)).thenReturn(fromStopIds);
            when(gtfsDataService.getStopIdsByStopName(toStopName)).thenReturn(toStopIds);
            when(gtfsDataService.getActiveServiceIds(feedType, at.toLocalDate())).thenReturn(activeServiceIds);
            when(gtfsDataService.findBestDirectTrip(feedType, fromStopIds, toStopIds, at, activeServiceIds))
                    .thenReturn(bestTrip);
            when(gtfsDataService.getRouteShortName("route_152")).thenReturn("152");
            when(gtfsDataService.getStopIdForTripAndSequence("trip_456", 1)).thenReturn("stop_4825");

            long departureTimestamp = LocalDateTime.of(2026, 1, 14, 12, 33, 0)
                    .atZone(ZoneId.systemDefault())
                    .toEpochSecond();
            GtfsRealtime.FeedMessage realtimeFeed = createRealtimeFeed("trip_456", 1, departureTimestamp);
            when(gtfsClient.getTripUpdatesFeed()).thenReturn(realtimeFeed);

            // When
            Optional<Departure> result = tripService.getNextDirectDeparture(feedType, fromStopName, toStopName, at);

            // Then
            assertThat(result).isPresent();
            Departure departure = result.get();
            assertThat(departure.stopName()).isEqualTo(fromStopName);
            assertThat(departure.lineNumber()).isEqualTo("152");
            assertThat(departure.departureTime()).isEqualTo("12:33:00");
        }

        @Test
        @DisplayName("Should return departure with scheduled time when realtime unavailable")
        void shouldReturnDepartureWithScheduledTimeWhenRealtimeUnavailable() throws IOException {
            // Given
            String feedType = "M";
            String fromStopName = "Rynek Główny";
            String toStopName = "Teatr Bagatela";
            LocalDateTime at = LocalDateTime.of(2026, 1, 14, 12, 0);

            List<String> fromStopIds = List.of("stop_4825");
            List<String> toStopIds = List.of("stop_1122");
            Set<String> activeServiceIds = Set.of("service_weekday");

            GtfsDataService.DirectTripResult bestTrip =
                    new GtfsDataService.DirectTripResult("trip_456", "route_152", "12:30:00", "12:45:00");

            when(gtfsDataService.getStopIdsByStopName(fromStopName)).thenReturn(fromStopIds);
            when(gtfsDataService.getStopIdsByStopName(toStopName)).thenReturn(toStopIds);
            when(gtfsDataService.getActiveServiceIds(feedType, at.toLocalDate())).thenReturn(activeServiceIds);
            when(gtfsDataService.findBestDirectTrip(feedType, fromStopIds, toStopIds, at, activeServiceIds))
                    .thenReturn(bestTrip);
            when(gtfsDataService.getRouteShortName("route_152")).thenReturn("152");

            GtfsRealtime.FeedMessage emptyFeed = GtfsRealtime.FeedMessage.newBuilder()
                    .setHeader(GtfsRealtime.FeedHeader.newBuilder()
                            .setGtfsRealtimeVersion("2.0")
                            .setTimestamp(System.currentTimeMillis() / 1000)
                            .build())
                    .build();
            when(gtfsClient.getTripUpdatesFeed()).thenReturn(emptyFeed);

            // When
            Optional<Departure> result = tripService.getNextDirectDeparture(feedType, fromStopName, toStopName, at);

            // Then
            assertThat(result).isPresent();
            Departure departure = result.get();
            assertThat(departure.stopName()).isEqualTo(fromStopName);
            assertThat(departure.lineNumber()).isEqualTo("152");
            assertThat(departure.departureTime()).isEqualTo("12:30:00");
        }

        @Test
        @DisplayName("Should return departure with scheduled time when realtime API throws IOException")
        void shouldReturnScheduledTimeWhenRealtimeThrowsException() throws IOException {
            // Given
            String feedType = "M";
            String fromStopName = "Rynek Główny";
            String toStopName = "Teatr Bagatela";
            LocalDateTime at = LocalDateTime.of(2026, 1, 14, 12, 0);

            List<String> fromStopIds = List.of("stop_4825");
            List<String> toStopIds = List.of("stop_1122");
            Set<String> activeServiceIds = Set.of("service_weekday");

            GtfsDataService.DirectTripResult bestTrip =
                    new GtfsDataService.DirectTripResult("trip_456", "route_152", "12:30:00", "12:45:00");

            when(gtfsDataService.getStopIdsByStopName(fromStopName)).thenReturn(fromStopIds);
            when(gtfsDataService.getStopIdsByStopName(toStopName)).thenReturn(toStopIds);
            when(gtfsDataService.getActiveServiceIds(feedType, at.toLocalDate())).thenReturn(activeServiceIds);
            when(gtfsDataService.findBestDirectTrip(feedType, fromStopIds, toStopIds, at, activeServiceIds))
                    .thenReturn(bestTrip);
            when(gtfsDataService.getRouteShortName("route_152")).thenReturn("152");

            when(gtfsClient.getTripUpdatesFeed()).thenThrow(new com.google.protobuf.InvalidProtocolBufferException("Network error"));

            // When
            Optional<Departure> result = tripService.getNextDirectDeparture(feedType, fromStopName, toStopName, at);

            // Then
            assertThat(result).isPresent();
            Departure departure = result.get();
            assertThat(departure.departureTime()).isEqualTo("12:30:00");
        }

        @Test
        @DisplayName("Should return empty when from stop not found")
        void shouldReturnEmptyWhenFromStopNotFound() {
            // Given
            String feedType = "M";
            String fromStopName = "Nieistniejący Przystanek";
            String toStopName = "Teatr Bagatela";
            LocalDateTime at = LocalDateTime.of(2026, 1, 14, 12, 0);

            when(gtfsDataService.getStopIdsByStopName(fromStopName)).thenReturn(Collections.emptyList());
            when(gtfsDataService.getStopIdsByStopName(toStopName)).thenReturn(List.of("stop_1122"));

            // When
            Optional<Departure> result = tripService.getNextDirectDeparture(feedType, fromStopName, toStopName, at);

            // Then
            assertThat(result).isEmpty();
            verify(gtfsDataService, never()).getActiveServiceIds(any(), any());
            verify(gtfsDataService, never()).findBestDirectTrip(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should return empty when to stop not found")
        void shouldReturnEmptyWhenToStopNotFound() {
            // Given
            String feedType = "M";
            String fromStopName = "Rynek Główny";
            String toStopName = "Nieistniejący Przystanek";
            LocalDateTime at = LocalDateTime.of(2026, 1, 14, 12, 0);

            when(gtfsDataService.getStopIdsByStopName(fromStopName)).thenReturn(List.of("stop_4825"));
            when(gtfsDataService.getStopIdsByStopName(toStopName)).thenReturn(Collections.emptyList());

            // When
            Optional<Departure> result = tripService.getNextDirectDeparture(feedType, fromStopName, toStopName, at);

            // Then
            assertThat(result).isEmpty();
            verify(gtfsDataService, never()).getActiveServiceIds(any(), any());
        }

        @Test
        @DisplayName("Should return empty when no active services")
        void shouldReturnEmptyWhenNoActiveServices() {
            // Given
            String feedType = "M";
            String fromStopName = "Rynek Główny";
            String toStopName = "Teatr Bagatela";
            LocalDateTime at = LocalDateTime.of(2026, 1, 14, 12, 0);

            List<String> fromStopIds = List.of("stop_4825");
            List<String> toStopIds = List.of("stop_1122");

            when(gtfsDataService.getStopIdsByStopName(fromStopName)).thenReturn(fromStopIds);
            when(gtfsDataService.getStopIdsByStopName(toStopName)).thenReturn(toStopIds);
            when(gtfsDataService.getActiveServiceIds(feedType, at.toLocalDate())).thenReturn(Collections.emptySet());

            // When
            Optional<Departure> result = tripService.getNextDirectDeparture(feedType, fromStopName, toStopName, at);

            // Then
            assertThat(result).isEmpty();
            verify(gtfsDataService, never()).findBestDirectTrip(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should return empty when no direct connection found")
        void shouldReturnEmptyWhenNoDirectConnection() {
            // Given
            String feedType = "M";
            String fromStopName = "Rynek Główny";
            String toStopName = "Teatr Bagatela";
            LocalDateTime at = LocalDateTime.of(2026, 1, 14, 12, 0);

            List<String> fromStopIds = List.of("stop_4825");
            List<String> toStopIds = List.of("stop_1122");
            Set<String> activeServiceIds = Set.of("service_weekday");

            when(gtfsDataService.getStopIdsByStopName(fromStopName)).thenReturn(fromStopIds);
            when(gtfsDataService.getStopIdsByStopName(toStopName)).thenReturn(toStopIds);
            when(gtfsDataService.getActiveServiceIds(feedType, at.toLocalDate())).thenReturn(activeServiceIds);
            when(gtfsDataService.findBestDirectTrip(feedType, fromStopIds, toStopIds, at, activeServiceIds))
                    .thenReturn(null);

            // When
            Optional<Departure> result = tripService.getNextDirectDeparture(feedType, fromStopName, toStopName, at);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getDepartureDelaySeconds()")
    class GetDepartureDelaySecondsTests {

        @Test
        @DisplayName("Should calculate delay in seconds between scheduled and realtime")
        void shouldCalculateDelayInSeconds() throws IOException {
            // Given
            String tripId = "trip_123";
            List<String> stopIds = List.of("stop_1");
            String scheduledTime = "12:30:00";

            when(gtfsDataService.getStopIdForTripAndSequence(tripId, 1)).thenReturn("stop_1");

            long realtimeTimestamp = LocalDateTime.of(2026, 1, 14, 12, 33, 0)
                    .atZone(ZoneId.systemDefault())
                    .toEpochSecond();
            GtfsRealtime.FeedMessage realtimeFeed = createRealtimeFeed(tripId, 1, realtimeTimestamp);
            when(gtfsClient.getTripUpdatesFeed()).thenReturn(realtimeFeed);

            // When
            Integer delay = tripService.getDepartureDelaySeconds(tripId, stopIds, scheduledTime);

            // Then
            assertThat(delay).isEqualTo(180); // 3 minuty opóźnienia = 180 sekund
        }
    }

    private GtfsRealtime.FeedMessage createRealtimeFeed(String tripId, int stopSequence, long departureTime) {
        GtfsRealtime.FeedHeader header = GtfsRealtime.FeedHeader.newBuilder()
                .setGtfsRealtimeVersion("2.0")
                .setTimestamp(System.currentTimeMillis() / 1000)
                .build();

        GtfsRealtime.TripUpdate.StopTimeEvent departureEvent = GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder()
                .setTime(departureTime)
                .build();

        GtfsRealtime.TripUpdate.StopTimeUpdate stopTimeUpdate = GtfsRealtime.TripUpdate.StopTimeUpdate.newBuilder()
                .setStopSequence(stopSequence)
                .setDeparture(departureEvent)
                .build();

        GtfsRealtime.TripDescriptor tripDescriptor = GtfsRealtime.TripDescriptor.newBuilder()
                .setTripId(tripId)
                .build();

        GtfsRealtime.TripUpdate tripUpdate = GtfsRealtime.TripUpdate.newBuilder()
                .setTrip(tripDescriptor)
                .addStopTimeUpdate(stopTimeUpdate)
                .build();

        GtfsRealtime.FeedEntity entity = GtfsRealtime.FeedEntity.newBuilder()
                .setId("vehicle" + tripId)
                .setTripUpdate(tripUpdate)
                .build();

        return GtfsRealtime.FeedMessage.newBuilder()
                .setHeader(header)
                .addEntity(entity)
                .build();
    }
}
