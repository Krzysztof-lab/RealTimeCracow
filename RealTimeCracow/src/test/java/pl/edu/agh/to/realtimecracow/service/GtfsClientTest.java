package pl.edu.agh.to.realtimecracow.service;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.transit.realtime.GtfsRealtime;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import org.junit.jupiter.api.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GtfsClient")
class GtfsClientTest {

    private MockWebServer mockWebServer;
    private GtfsClient gtfsClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        // Remove trailing slash if present
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        gtfsClient = new GtfsClient(baseUrl, WebClient.builder());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Nested
    @DisplayName("getTripUpdatesFeed()")
    class GetTripUpdatesFeedTests {

        @Test
        @DisplayName("should successfully parse valid protobuf response")
        void shouldParseValidProtobufResponse() throws Exception {
            // Create a valid FeedMessage
            GtfsRealtime.FeedMessage feedMessage = GtfsRealtime.FeedMessage.newBuilder()
                    .setHeader(GtfsRealtime.FeedHeader.newBuilder()
                            .setGtfsRealtimeVersion("2.0")
                            .setTimestamp(1700000000L)
                            .build())
                    .addEntity(GtfsRealtime.FeedEntity.newBuilder()
                            .setId("prefix:trip123")
                            .setTripUpdate(GtfsRealtime.TripUpdate.newBuilder()
                                    .setTrip(GtfsRealtime.TripDescriptor.newBuilder()
                                            .setTripId("trip123")
                                            .build())
                                    .build())
                            .build())
                    .build();

            byte[] protobufBytes = feedMessage.toByteArray();

            mockWebServer.enqueue(new MockResponse()
                    .setBody(new Buffer().write(protobufBytes))
                    .addHeader("Content-Type", "application/x-protobuf"));

            GtfsRealtime.FeedMessage result = gtfsClient.getTripUpdatesFeed();

            assertNotNull(result);
            assertEquals("2.0", result.getHeader().getGtfsRealtimeVersion());
            assertEquals(1, result.getEntityCount());
            assertEquals("prefix:trip123", result.getEntity(0).getId());
        }

        @Test
        @DisplayName("should request correct endpoint")
        void shouldRequestCorrectEndpoint() throws Exception {
            GtfsRealtime.FeedMessage feedMessage = GtfsRealtime.FeedMessage.newBuilder()
                    .setHeader(GtfsRealtime.FeedHeader.newBuilder()
                            .setGtfsRealtimeVersion("2.0")
                            .build())
                    .build();

            mockWebServer.enqueue(new MockResponse()
                    .setBody(new Buffer().write(feedMessage.toByteArray()))
                    .addHeader("Content-Type", "application/x-protobuf"));

            gtfsClient.getTripUpdatesFeed();

            RecordedRequest request = mockWebServer.takeRequest();
            assertEquals("/TripUpdates.pb", request.getPath());
            assertEquals("GET", request.getMethod());
        }

        @Test
        @DisplayName("should handle empty feed correctly")
        void shouldHandleEmptyFeed() throws Exception {
            GtfsRealtime.FeedMessage emptyFeed = GtfsRealtime.FeedMessage.newBuilder()
                    .setHeader(GtfsRealtime.FeedHeader.newBuilder()
                            .setGtfsRealtimeVersion("2.0")
                            .build())
                    .build();

            mockWebServer.enqueue(new MockResponse()
                    .setBody(new Buffer().write(emptyFeed.toByteArray()))
                    .addHeader("Content-Type", "application/x-protobuf"));

            GtfsRealtime.FeedMessage result = gtfsClient.getTripUpdatesFeed();

            assertNotNull(result);
            assertEquals(0, result.getEntityCount());
        }

        @Test
        @DisplayName("should throw exception for invalid protobuf data")
        void shouldThrowExceptionForInvalidProtobuf() {
            mockWebServer.enqueue(new MockResponse()
                    .setBody("this is not valid protobuf data")
                    .addHeader("Content-Type", "application/x-protobuf"));

            assertThrows(InvalidProtocolBufferException.class, () -> gtfsClient.getTripUpdatesFeed());
        }

        @Test
        @DisplayName("should throw exception for HTTP 500 error")
        void shouldThrowExceptionForServerError() {
            mockWebServer.enqueue(new MockResponse().setResponseCode(500));

            assertThrows(WebClientResponseException.class, () -> gtfsClient.getTripUpdatesFeed());
        }

        @Test
        @DisplayName("should throw exception for HTTP 404 error")
        void shouldThrowExceptionForNotFound() {
            mockWebServer.enqueue(new MockResponse().setResponseCode(404));

            assertThrows(WebClientResponseException.class, () -> gtfsClient.getTripUpdatesFeed());
        }

        @Test
        @DisplayName("should handle feed with multiple entities")
        void shouldHandleFeedWithMultipleEntities() throws Exception {
            GtfsRealtime.FeedMessage feedMessage = GtfsRealtime.FeedMessage.newBuilder()
                    .setHeader(GtfsRealtime.FeedHeader.newBuilder()
                            .setGtfsRealtimeVersion("2.0")
                            .build())
                    .addEntity(GtfsRealtime.FeedEntity.newBuilder()
                            .setId("entity1")
                            .setTripUpdate(GtfsRealtime.TripUpdate.newBuilder()
                                    .setTrip(GtfsRealtime.TripDescriptor.newBuilder()
                                            .setTripId("trip1")
                                            .build())
                                    .build())
                            .build())
                    .addEntity(GtfsRealtime.FeedEntity.newBuilder()
                            .setId("entity2")
                            .setTripUpdate(GtfsRealtime.TripUpdate.newBuilder()
                                    .setTrip(GtfsRealtime.TripDescriptor.newBuilder()
                                            .setTripId("trip2")
                                            .build())
                                    .build())
                            .build())
                    .addEntity(GtfsRealtime.FeedEntity.newBuilder()
                            .setId("entity3")
                            .setTripUpdate(GtfsRealtime.TripUpdate.newBuilder()
                                    .setTrip(GtfsRealtime.TripDescriptor.newBuilder()
                                            .setTripId("trip3")
                                            .build())
                                    .build())
                            .build())
                    .build();

            mockWebServer.enqueue(new MockResponse()
                    .setBody(new Buffer().write(feedMessage.toByteArray()))
                    .addHeader("Content-Type", "application/x-protobuf"));

            GtfsRealtime.FeedMessage result = gtfsClient.getTripUpdatesFeed();

            assertEquals(3, result.getEntityCount());
            assertEquals("entity1", result.getEntity(0).getId());
            assertEquals("entity2", result.getEntity(1).getId());
            assertEquals("entity3", result.getEntity(2).getId());
        }

        @Test
        @DisplayName("should handle feed with stop time updates")
        void shouldHandleFeedWithStopTimeUpdates() throws Exception {
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

            mockWebServer.enqueue(new MockResponse()
                    .setBody(new Buffer().write(feedMessage.toByteArray()))
                    .addHeader("Content-Type", "application/x-protobuf"));

            GtfsRealtime.FeedMessage result = gtfsClient.getTripUpdatesFeed();

            assertEquals(1, result.getEntityCount());
            GtfsRealtime.TripUpdate tripUpdate = result.getEntity(0).getTripUpdate();
            assertEquals(1, tripUpdate.getStopTimeUpdateCount());
            assertEquals(1, tripUpdate.getStopTimeUpdate(0).getStopSequence());
            assertEquals(1700000000L, tripUpdate.getStopTimeUpdate(0).getArrival().getTime());
            assertEquals(1700000060L, tripUpdate.getStopTimeUpdate(0).getDeparture().getTime());
        }
    }
}
