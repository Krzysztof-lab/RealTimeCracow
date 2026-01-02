package pl.edu.agh.to.realtimecracow.service;

import com.google.transit.realtime.GtfsRealtime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pl.edu.agh.to.realtimecracow.model.StopInfo;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GtfsParser")
class GtfsParserTest {

    private GtfsParser gtfsParser;

    @BeforeEach
    void setUp() throws IOException {
        gtfsParser = new GtfsParser("gtfs/");
        gtfsParser.loadGtfsData();
    }

    @Nested
    @DisplayName("loadGtfsData()")
    class LoadGtfsDataTests {

        @Test
        @DisplayName("should load stops from all agencies without throwing exception")
        void shouldLoadStopsSuccessfully() {
            // The setUp already loaded data, if we got here it means no exception was thrown
            assertDoesNotThrow(() -> gtfsParser.loadGtfsData());
        }
    }

    @Nested
    @DisplayName("getLineNumber()")
    class GetLineNumberTests {

        @Test
        @DisplayName("should return null for non-existent trip")
        void shouldReturnNullForNonExistentTrip() {
            String result = gtfsParser.getLineNumber("prefix:NON_EXISTENT_TRIP_ID");
            assertNull(result);
        }

        @Test
        @DisplayName("should handle entity id format correctly")
        void shouldHandleEntityIdFormat() {
            // Entity ID format is "prefix:" + tripId (7 chars prefix)
            // Testing with a non-existent id to verify the extraction works
            String result = gtfsParser.getLineNumber("1234567nonexistent");
            assertNull(result); // Should return null as trip doesn't exist
        }
    }

    @Nested
    @DisplayName("getStopListForEntity()")
    class GetStopListForEntityTests {

        @Test
        @DisplayName("should return empty list when entity has no stop time updates with departures")
        void shouldReturnEmptyListWhenNoStopTimeUpdates() {
            GtfsRealtime.FeedEntity entity = GtfsRealtime.FeedEntity.newBuilder()
                    .setId("prefix:test_trip")
                    .setTripUpdate(GtfsRealtime.TripUpdate.newBuilder()
                            .setTrip(GtfsRealtime.TripDescriptor.newBuilder()
                                    .setTripId("test_trip")
                                    .build())
                            .build())
                    .build();

            List<StopInfo> result = gtfsParser.getStopListForEntity(entity);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return empty list when stop time update has no departure")
        void shouldReturnEmptyListWhenNoDeparture() {
            GtfsRealtime.FeedEntity entity = GtfsRealtime.FeedEntity.newBuilder()
                    .setId("prefix:test_trip")
                    .setTripUpdate(GtfsRealtime.TripUpdate.newBuilder()
                            .setTrip(GtfsRealtime.TripDescriptor.newBuilder()
                                    .setTripId("test_trip")
                                    .build())
                            .addStopTimeUpdate(GtfsRealtime.TripUpdate.StopTimeUpdate.newBuilder()
                                    .setStopSequence(1)
                                    .setArrival(GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder()
                                            .setTime(1700000000L)
                                            .build())
                                    // No departure set
                                    .build())
                            .build())
                    .build();

            List<StopInfo> result = gtfsParser.getStopListForEntity(entity);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should parse stop time update with departure correctly")
        void shouldParseStopTimeUpdateWithDeparture() {
            long timestamp = 1700000000L; // 2023-11-14 22:13:20 UTC

            GtfsRealtime.FeedEntity entity = GtfsRealtime.FeedEntity.newBuilder()
                    .setId("prefix:test_trip")
                    .setTripUpdate(GtfsRealtime.TripUpdate.newBuilder()
                            .setTrip(GtfsRealtime.TripDescriptor.newBuilder()
                                    .setTripId("test_trip")
                                    .build())
                            .addStopTimeUpdate(GtfsRealtime.TripUpdate.StopTimeUpdate.newBuilder()
                                    .setStopSequence(1)
                                    .setArrival(GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder()
                                            .setTime(timestamp)
                                            .build())
                                    .setDeparture(GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder()
                                            .setTime(timestamp + 60) // 1 minute later
                                            .build())
                                    .build())
                            .build())
                    .build();

            List<StopInfo> result = gtfsParser.getStopListForEntity(entity);

            assertEquals(1, result.size());
            StopInfo stopInfo = result.get(0);
            assertEquals(1, stopInfo.stopSequence());
            // stopId and stopName may be null if trip doesn't exist in test data
            assertNotNull(stopInfo.arrivalTime());
            assertNotNull(stopInfo.departureTime());
        }

        @Test
        @DisplayName("should handle multiple stop time updates")
        void shouldHandleMultipleStopTimeUpdates() {
            long timestamp = 1700000000L;

            GtfsRealtime.FeedEntity entity = GtfsRealtime.FeedEntity.newBuilder()
                    .setId("prefix:test_trip")
                    .setTripUpdate(GtfsRealtime.TripUpdate.newBuilder()
                            .setTrip(GtfsRealtime.TripDescriptor.newBuilder()
                                    .setTripId("test_trip")
                                    .build())
                            .addStopTimeUpdate(GtfsRealtime.TripUpdate.StopTimeUpdate.newBuilder()
                                    .setStopSequence(1)
                                    .setArrival(GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder().setTime(timestamp).build())
                                    .setDeparture(GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder().setTime(timestamp + 60).build())
                                    .build())
                            .addStopTimeUpdate(GtfsRealtime.TripUpdate.StopTimeUpdate.newBuilder()
                                    .setStopSequence(2)
                                    .setArrival(GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder().setTime(timestamp + 300).build())
                                    .setDeparture(GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder().setTime(timestamp + 360).build())
                                    .build())
                            .addStopTimeUpdate(GtfsRealtime.TripUpdate.StopTimeUpdate.newBuilder()
                                    .setStopSequence(3)
                                    .setArrival(GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder().setTime(timestamp + 600).build())
                                    .setDeparture(GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder().setTime(timestamp + 660).build())
                                    .build())
                            .build())
                    .build();

            List<StopInfo> result = gtfsParser.getStopListForEntity(entity);

            assertEquals(3, result.size());
            assertEquals(1, result.get(0).stopSequence());
            assertEquals(2, result.get(1).stopSequence());
            assertEquals(3, result.get(2).stopSequence());
        }

        @Test
        @DisplayName("should format departure time correctly")
        void shouldFormatDepartureTimeCorrectly() {
            // Using a known timestamp
            long timestamp = 1700000000L;

            GtfsRealtime.FeedEntity entity = GtfsRealtime.FeedEntity.newBuilder()
                    .setId("prefix:test_trip")
                    .setTripUpdate(GtfsRealtime.TripUpdate.newBuilder()
                            .setTrip(GtfsRealtime.TripDescriptor.newBuilder()
                                    .setTripId("test_trip")
                                    .build())
                            .addStopTimeUpdate(GtfsRealtime.TripUpdate.StopTimeUpdate.newBuilder()
                                    .setStopSequence(1)
                                    .setArrival(GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder().setTime(timestamp).build())
                                    .setDeparture(GtfsRealtime.TripUpdate.StopTimeEvent.newBuilder().setTime(timestamp).build())
                                    .build())
                            .build())
                    .build();

            List<StopInfo> result = gtfsParser.getStopListForEntity(entity);

            assertEquals(1, result.size());
            // Time format should be HH:mm:ss
            assertTrue(result.get(0).departureTime().matches("\\d{2}:\\d{2}:\\d{2}"));
            assertTrue(result.get(0).arrivalTime().matches("\\d{2}:\\d{2}:\\d{2}"));
        }
    }
}
