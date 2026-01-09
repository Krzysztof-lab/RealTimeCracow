package pl.edu.agh.to.realtimecracow.integration;

import com.google.transit.realtime.GtfsRealtime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.agh.to.realtimecracow.controller.TripController;
import pl.edu.agh.to.realtimecracow.model.Departure;
import pl.edu.agh.to.realtimecracow.service.GtfsClient;
import pl.edu.agh.to.realtimecracow.service.GtfsDataService;
import pl.edu.agh.to.realtimecracow.service.GtfsParser;
import pl.edu.agh.to.realtimecracow.service.TripService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("RealTimeCracow Integration Tests")
class RealTimeCracowIntegrationTest {


    @Autowired
    private TripController tripController;

    @Autowired
    private TripService tripService;

    @MockitoBean
    private GtfsClient gtfsClient;

    @Autowired
    private GtfsDataService gtfsDataService;

    @Autowired
    private GtfsParser gtfsParser;

    @Nested
    @DisplayName("Full application flow")
    class FullApplicationFlowTests {

        @Test
        @DisplayName("should return departure when feed has valid data")
        void shouldReturnDepartureWhenFeedHasValidData() throws Exception {
            GtfsRealtime.FeedMessage feedMessage = GtfsRealtime.FeedMessage.newBuilder()
                    .setHeader(GtfsRealtime.FeedHeader.newBuilder()
                            .setGtfsRealtimeVersion("2.0")
                            .build())
                    .addEntity(GtfsRealtime.FeedEntity.newBuilder()
                            .setId("prefix:test_trip_id")
                            .setTripUpdate(GtfsRealtime.TripUpdate.newBuilder()
                                    .setTrip(GtfsRealtime.TripDescriptor.newBuilder()
                                            .setTripId("test_trip_id")
                                            .build())
                                    .addStopTimeUpdate(GtfsRealtime.TripUpdate.StopTimeUpdate.newBuilder()
                                            .setStopSequence(1)
                                            .setArrival(GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder()
                                                    .setTime(1700000000L)
                                                    .build())
                                            .setDeparture(GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder()
                                                    .setTime(1700000060L)
                                                    .build())
                                            .build())
                                    .build())
                            .build())
                    .build();

            when(gtfsClient.getTripUpdatesFeed()).thenReturn(feedMessage);

            var response = tripController.getNextDirect("Rynek Główny", "Teatr Bagatela", "2026-01-09T12:00:00", "M");
            assertEquals(org.springframework.http.HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }

        @Test
        @DisplayName("should return 'No data' when feed is empty")
        void shouldReturnNoDataWhenFeedIsEmpty() throws Exception {
            GtfsRealtime.FeedMessage emptyFeed = GtfsRealtime.FeedMessage.newBuilder()
                    .setHeader(GtfsRealtime.FeedHeader.newBuilder()
                            .setGtfsRealtimeVersion("2.0")
                            .build())
                    .build();

            when(gtfsClient.getTripUpdatesFeed()).thenReturn(emptyFeed);

            var response = tripController.getNextDirect("X", "Y", "2026-01-09T12:00:00", "M");
            assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNull(response.getBody());
        }

        @Test
        @DisplayName("should return 'No data' when entity has no stop time updates with departure")
        void shouldReturnNoDataWhenNoStopTimeUpdates() throws Exception {
            GtfsRealtime.FeedMessage feedMessage = GtfsRealtime.FeedMessage.newBuilder()
                    .setHeader(GtfsRealtime.FeedHeader.newBuilder()
                            .setGtfsRealtimeVersion("2.0")
                            .build())
                    .addEntity(GtfsRealtime.FeedEntity.newBuilder()
                            .setId("prefix:test_trip")
                            .setTripUpdate(GtfsRealtime.TripUpdate.newBuilder()
                                    .setTrip(GtfsRealtime.TripDescriptor.newBuilder()
                                            .setTripId("test_trip")
                                            .build())
                                    .build())
                            .build())
                    .build();

            when(gtfsClient.getTripUpdatesFeed()).thenReturn(feedMessage);

            var response = tripController.getNextDirect("X", "Y", "2026-01-09T12:00:00", "M");
            assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNull(response.getBody());

        }

        @Test
        @DisplayName("should correctly integrate TripService with GtfsParser")
        void shouldIntegrateTripServiceWithGtfsParser() throws Exception {
            // Create a feed with trip update
            GtfsRealtime.FeedMessage feedMessage = GtfsRealtime.FeedMessage.newBuilder()
                    .setHeader(GtfsRealtime.FeedHeader.newBuilder()
                            .setGtfsRealtimeVersion("2.0")
                            .build())
                    .addEntity(GtfsRealtime.FeedEntity.newBuilder()
                            .setId("prefix:trip123")
                            .setTripUpdate(GtfsRealtime.TripUpdate.newBuilder()
                                    .setTrip(GtfsRealtime.TripDescriptor.newBuilder()
                                            .setTripId("trip123")
                                            .build())
                                    .addStopTimeUpdate(GtfsRealtime.TripUpdate.StopTimeUpdate.newBuilder()
                                            .setStopSequence(1)
                                            .setArrival(GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder()
                                                    .setTime(1700000000L)
                                                    .build())
                                            .setDeparture(GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder()
                                                    .setTime(1700000060L)
                                                    .build())
                                            .build())
                                    .build())
                            .build())
                    .build();

            when(gtfsClient.getTripUpdatesFeed()).thenReturn(feedMessage);

            Departure result = tripService.getRandomDeparture();

            assertNotNull(result);
            // Time should be formatted as HH:mm:ss
            assertTrue(result.departureTime().matches("\\d{2}:\\d{2}:\\d{2}"));
        }
    }

    @Nested
    @DisplayName("GtfsParser integration")
    class GtfsParserIntegrationTests {

        @Test
        @DisplayName("should have loaded GTFS data on startup")
        void shouldHaveLoadedGtfsDataOnStartup() {
            // If we got here without exception, GTFS data was loaded successfully
            String result = gtfsParser.getLineNumber("prefix:nonexistent");
            // Result should be null for non-existent trip, but shouldn't throw
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Controller integration")
    class ControllerIntegrationTests {

        @Test
        @DisplayName("should have TripController properly wired")
        void shouldHaveTripControllerProperlyWired() {
            assertNotNull(tripController);
        }

        @Test
        @DisplayName("should have TripService properly wired")
        void shouldHaveTripServiceProperlyWired() {
            assertNotNull(tripService);
        }
    }
}
