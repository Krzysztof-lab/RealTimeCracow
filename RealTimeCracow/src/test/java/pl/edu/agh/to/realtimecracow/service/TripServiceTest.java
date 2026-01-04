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
import pl.edu.agh.to.realtimecracow.model.StopInfo;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TripService")
class TripServiceTest {

    @Mock
    private GtfsClient gtfsClient;

    @Mock
    private GtfsDataService gtfsDataService;

    @Mock
    private GtfsParser gtfsParser;

    private TripService tripService;

    @BeforeEach
    void setUp() {
        tripService = new TripService(gtfsClient, gtfsDataService, gtfsParser);
        // Default: no data in database, use fallback GtfsParser
        when(gtfsDataService.hasData()).thenReturn(false);
    }

    @Nested
    @DisplayName("getRandomDeparture()")
    class GetRandomDepartureTests {

        @Test
        @DisplayName("should return 'No data' when feed has no entities")
        void shouldReturnNoDataWhenFeedIsEmpty() throws IOException {
            GtfsRealtime.FeedMessage emptyFeed = GtfsRealtime.FeedMessage.newBuilder()
                    .setHeader(GtfsRealtime.FeedHeader.newBuilder()
                            .setGtfsRealtimeVersion("2.0")
                            .build())
                    .build();

            when(gtfsClient.getTripUpdatesFeed()).thenReturn(emptyFeed);

            Departure result = tripService.getRandomDeparture();

            assertEquals("No data", result.stopName());
            assertEquals("-", result.lineNumber());
            assertEquals("-", result.departureTime());
            verify(gtfsClient).getTripUpdatesFeed();
            verify(gtfsParser, never()).getStopListForEntity(any());
        }

        @Test
        @DisplayName("should return 'No data' when stops list is empty")
        void shouldReturnNoDataWhenStopsListIsEmpty() throws IOException {
            GtfsRealtime.FeedEntity entity = GtfsRealtime.FeedEntity.newBuilder()
                    .setId("prefix:test_trip")
                    .setTripUpdate(GtfsRealtime.TripUpdate.newBuilder()
                            .setTrip(GtfsRealtime.TripDescriptor.newBuilder()
                                    .setTripId("test_trip")
                                    .build())
                            .build())
                    .build();

            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.newBuilder()
                    .setHeader(GtfsRealtime.FeedHeader.newBuilder()
                            .setGtfsRealtimeVersion("2.0")
                            .build())
                    .addEntity(entity)
                    .build();

            when(gtfsClient.getTripUpdatesFeed()).thenReturn(feed);
            when(gtfsParser.getStopListForEntity(entity)).thenReturn(Collections.emptyList());

            Departure result = tripService.getRandomDeparture();

            assertEquals("No data", result.stopName());
            assertEquals("-", result.lineNumber());
            assertEquals("-", result.departureTime());
        }

        @Test
        @DisplayName("should return departure with correct data from first entity")
        void shouldReturnDepartureFromFirstEntity() throws IOException {
            GtfsRealtime.FeedEntity entity = GtfsRealtime.FeedEntity.newBuilder()
                    .setId("prefix:trip123")
                    .setTripUpdate(GtfsRealtime.TripUpdate.newBuilder()
                            .setTrip(GtfsRealtime.TripDescriptor.newBuilder()
                                    .setTripId("trip123")
                                    .build())
                            .build())
                    .build();

            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.newBuilder()
                    .setHeader(GtfsRealtime.FeedHeader.newBuilder()
                            .setGtfsRealtimeVersion("2.0")
                            .build())
                    .addEntity(entity)
                    .build();

            List<StopInfo> stops = List.of(
                    new StopInfo("stop1", "Rynek Główny", "12:30:00", "12:31:00", 1),
                    new StopInfo("stop2", "Plac Wszystkich Świętych", "12:35:00", "12:36:00", 2)
            );

            when(gtfsClient.getTripUpdatesFeed()).thenReturn(feed);
            when(gtfsParser.getStopListForEntity(entity)).thenReturn(stops);
            when(gtfsParser.getLineNumber("prefix:trip123")).thenReturn("152");

            Departure result = tripService.getRandomDeparture();

            assertEquals("Rynek Główny", result.stopName());
            assertEquals("152", result.lineNumber());
            assertEquals("12:31:00", result.departureTime());
        }

        @Test
        @DisplayName("should use first entity when multiple entities exist")
        void shouldUseFirstEntityWhenMultipleExist() throws IOException {
            GtfsRealtime.FeedEntity entity1 = GtfsRealtime.FeedEntity.newBuilder()
                    .setId("prefix:trip1")
                    .setTripUpdate(GtfsRealtime.TripUpdate.newBuilder()
                            .setTrip(GtfsRealtime.TripDescriptor.newBuilder()
                                    .setTripId("trip1")
                                    .build())
                            .build())
                    .build();

            GtfsRealtime.FeedEntity entity2 = GtfsRealtime.FeedEntity.newBuilder()
                    .setId("prefix:trip2")
                    .setTripUpdate(GtfsRealtime.TripUpdate.newBuilder()
                            .setTrip(GtfsRealtime.TripDescriptor.newBuilder()
                                    .setTripId("trip2")
                                    .build())
                            .build())
                    .build();

            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.newBuilder()
                    .setHeader(GtfsRealtime.FeedHeader.newBuilder()
                            .setGtfsRealtimeVersion("2.0")
                            .build())
                    .addEntity(entity1)
                    .addEntity(entity2)
                    .build();

            List<StopInfo> stops = List.of(
                    new StopInfo("stop1", "First Stop", "10:00:00", "10:01:00", 1)
            );

            when(gtfsClient.getTripUpdatesFeed()).thenReturn(feed);
            when(gtfsParser.getStopListForEntity(entity1)).thenReturn(stops);
            when(gtfsParser.getLineNumber("prefix:trip1")).thenReturn("1");

            Departure result = tripService.getRandomDeparture();

            assertEquals("First Stop", result.stopName());
            assertEquals("1", result.lineNumber());
            verify(gtfsParser).getStopListForEntity(entity1);
            verify(gtfsParser, never()).getStopListForEntity(entity2);
        }

        @Test
        @DisplayName("should propagate exception from GtfsClient")
        void shouldPropagateExceptionFromGtfsClient() throws Exception {
            when(gtfsClient.getTripUpdatesFeed()).thenThrow(new RuntimeException("Network error"));

            assertThrows(RuntimeException.class, () -> tripService.getRandomDeparture());
        }

        @Test
        @DisplayName("should handle null line number from fallback")
        void shouldHandleNullLineNumber() throws IOException {
            GtfsRealtime.FeedEntity entity = GtfsRealtime.FeedEntity.newBuilder()
                    .setId("prefix:unknown_trip")
                    .setTripUpdate(GtfsRealtime.TripUpdate.newBuilder()
                            .setTrip(GtfsRealtime.TripDescriptor.newBuilder()
                                    .setTripId("unknown_trip")
                                    .build())
                            .build())
                    .build();

            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.newBuilder()
                    .setHeader(GtfsRealtime.FeedHeader.newBuilder()
                            .setGtfsRealtimeVersion("2.0")
                            .build())
                    .addEntity(entity)
                    .build();

            List<StopInfo> stops = List.of(
                    new StopInfo("stop1", "Some Stop", "10:00:00", "10:01:00", 1)
            );

            when(gtfsClient.getTripUpdatesFeed()).thenReturn(feed);
            when(gtfsParser.getStopListForEntity(entity)).thenReturn(stops);
            when(gtfsParser.getLineNumber("prefix:unknown_trip")).thenReturn(null);

            Departure result = tripService.getRandomDeparture();

            assertEquals("Some Stop", result.stopName());
            assertNull(result.lineNumber());
            assertEquals("10:01:00", result.departureTime());
        }

        @Test
        @DisplayName("should return first stop's data when multiple stops exist")
        void shouldReturnFirstStopData() throws IOException {
            GtfsRealtime.FeedEntity entity = GtfsRealtime.FeedEntity.newBuilder()
                    .setId("prefix:trip1")
                    .setTripUpdate(GtfsRealtime.TripUpdate.newBuilder()
                            .setTrip(GtfsRealtime.TripDescriptor.newBuilder()
                                    .setTripId("trip1")
                                    .build())
                            .build())
                    .build();

            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.newBuilder()
                    .setHeader(GtfsRealtime.FeedHeader.newBuilder()
                            .setGtfsRealtimeVersion("2.0")
                            .build())
                    .addEntity(entity)
                    .build();

            List<StopInfo> stops = List.of(
                    new StopInfo("stop1", "First Stop", "10:00:00", "10:01:00", 1),
                    new StopInfo("stop2", "Second Stop", "10:05:00", "10:06:00", 2),
                    new StopInfo("stop3", "Third Stop", "10:10:00", "10:11:00", 3)
            );

            when(gtfsClient.getTripUpdatesFeed()).thenReturn(feed);
            when(gtfsParser.getStopListForEntity(entity)).thenReturn(stops);
            when(gtfsParser.getLineNumber("prefix:trip1")).thenReturn("A");

            Departure result = tripService.getRandomDeparture();

            assertEquals("First Stop", result.stopName());
            assertEquals("10:01:00", result.departureTime());
        }
    }

    @Nested
    @DisplayName("getRandomDeparture() with database")
    class GetRandomDepartureWithDatabaseTests {

        @Test
        @DisplayName("should use database when data is available")
        void shouldUseDatabaseWhenDataAvailable() throws IOException {
            when(gtfsDataService.hasData()).thenReturn(true);

            GtfsRealtime.FeedEntity entity = GtfsRealtime.FeedEntity.newBuilder()
                    .setId("prefix:trip123")
                    .setTripUpdate(GtfsRealtime.TripUpdate.newBuilder()
                            .setTrip(GtfsRealtime.TripDescriptor.newBuilder()
                                    .setTripId("trip123")
                                    .build())
                            .addStopTimeUpdate(GtfsRealtime.TripUpdate.StopTimeUpdate.newBuilder()
                                    .setStopSequence(1)
                                    .setArrival(GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder()
                                            .setTime(1704067200) // 2024-01-01 00:00:00 UTC
                                            .build())
                                    .setDeparture(GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder()
                                            .setTime(1704067260) // 2024-01-01 00:01:00 UTC
                                            .build())
                                    .build())
                            .build())
                    .build();

            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.newBuilder()
                    .setHeader(GtfsRealtime.FeedHeader.newBuilder()
                            .setGtfsRealtimeVersion("2.0")
                            .build())
                    .addEntity(entity)
                    .build();

            when(gtfsClient.getTripUpdatesFeed()).thenReturn(feed);
            when(gtfsDataService.getStopIdForTripAndSequence("trip123", 1)).thenReturn("stop1");
            when(gtfsDataService.getStopName("stop1")).thenReturn("Database Stop");
            when(gtfsDataService.getRouteIdForTrip("trip123")).thenReturn("route42");

            Departure result = tripService.getRandomDeparture();

            assertEquals("Database Stop", result.stopName());
            assertEquals("route42", result.lineNumber());
            verify(gtfsParser, never()).getStopListForEntity(any());
        }

        @Test
        @DisplayName("should fallback to parser when database has no matching data")
        void shouldFallbackWhenDatabaseHasNoMatchingData() throws IOException {
            when(gtfsDataService.hasData()).thenReturn(true);

            GtfsRealtime.FeedEntity entity = GtfsRealtime.FeedEntity.newBuilder()
                    .setId("prefix:trip123")
                    .setTripUpdate(GtfsRealtime.TripUpdate.newBuilder()
                            .setTrip(GtfsRealtime.TripDescriptor.newBuilder()
                                    .setTripId("trip123")
                                    .build())
                            .build())
                    .build();

            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.newBuilder()
                    .setHeader(GtfsRealtime.FeedHeader.newBuilder()
                            .setGtfsRealtimeVersion("2.0")
                            .build())
                    .addEntity(entity)
                    .build();

            List<StopInfo> stops = List.of(
                    new StopInfo("stop1", "Parser Stop", "12:30:00", "12:31:00", 1)
            );

            when(gtfsClient.getTripUpdatesFeed()).thenReturn(feed);
            when(gtfsParser.getStopListForEntity(entity)).thenReturn(stops);
            when(gtfsParser.getLineNumber("prefix:trip123")).thenReturn("152");

            Departure result = tripService.getRandomDeparture();

            assertEquals("Parser Stop", result.stopName());
            assertEquals("152", result.lineNumber());
        }
    }
}
