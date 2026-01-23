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
import pl.edu.agh.to.realtimecracow.service.GtfsClient;
import pl.edu.agh.to.realtimecracow.service.GtfsDataService;
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


    @Nested
    @DisplayName("Full application flow")
    class FullApplicationFlowTests {

        @Test
        @DisplayName("should return 404 when stop names not found in empty database")
        void shouldReturn404WhenStopNamesNotFound() throws Exception {
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
            assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, response.getStatusCode());
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
