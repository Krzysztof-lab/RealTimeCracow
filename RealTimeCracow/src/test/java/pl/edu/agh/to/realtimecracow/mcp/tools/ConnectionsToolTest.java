package pl.edu.agh.to.realtimecracow.mcp.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.to.realtimecracow.mcp.dto.ConnectionDto;
import pl.edu.agh.to.realtimecracow.service.GtfsDataService;
import pl.edu.agh.to.realtimecracow.service.TripService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectionsToolTest {

    @Mock
    private GtfsDataService gtfsDataService;

    @Mock
    private TripService tripService;

    private ConnectionsTool connectionsTool;

    @BeforeEach
    void setUp() {
        connectionsTool = new ConnectionsTool(gtfsDataService, tripService);
    }

    @Test
    void shouldReturnTopThreeConnections() {
        // given
        String feedType = "T";
        String fromStop = "AGH / UR";
        String toStop = "Rondo Mogilskie";
        LocalDateTime dateTime = LocalDateTime.of(2025, 1, 23, 14, 0);

        List<String> fromIds = List.of("from1");
        List<String> toIds = List.of("to1");
        Set<String> serviceIds = Set.of("service1");

        var trip1 = new GtfsDataService.DirectTripResult(
                "trip1", "route1", "14:10:00", "14:20:00"
        );
        var trip2 = new GtfsDataService.DirectTripResult(
                "trip2", "route1", "14:25:00", "14:35:00"
        );
        var trip3 = new GtfsDataService.DirectTripResult(
                "trip3", "route1", "14:40:00", "14:50:00"
        );

        when(gtfsDataService.getStopIdsByStopName(fromStop)).thenReturn(fromIds);
        when(gtfsDataService.getStopIdsByStopName(toStop)).thenReturn(toIds);
        when(gtfsDataService.getActiveServiceIds(feedType, dateTime.toLocalDate())).thenReturn(serviceIds);
        when(gtfsDataService.findTopDirectTrips(feedType, fromIds, toIds, dateTime, serviceIds, 3))
                .thenReturn(List.of(trip1, trip2, trip3));

        when(tripService.getDepartureDelaySeconds(eq("trip1"), eq(fromIds), any())).thenReturn(60);
        when(tripService.getDepartureDelaySeconds(eq("trip2"), eq(fromIds), any())).thenReturn(null);
        when(tripService.getDepartureDelaySeconds(eq("trip3"), eq(fromIds), any())).thenReturn(-30);

        // when
        List<ConnectionDto> result = connectionsTool.findConnections(
                feedType, fromStop, toStop, dateTime.toString()
        );

        // then
        assertEquals(3, result.size());

        assertEquals("14:10:00", result.getFirst().departureTimePlanned());
        assertEquals("14:20:00", result.get(0).arrivalTimePlanned());
        assertEquals(60, result.get(0).delaySeconds());

        assertEquals("14:25:00", result.get(1).departureTimePlanned());
        assertEquals("14:35:00", result.get(1).arrivalTimePlanned());
        assertNull(result.get(1).delaySeconds());

        assertEquals("14:40:00", result.get(2).departureTimePlanned());
        assertEquals("14:50:00", result.get(2).arrivalTimePlanned());
        assertEquals(-30, result.get(2).delaySeconds());

        verify(gtfsDataService, times(1)).findTopDirectTrips(feedType, fromIds, toIds, dateTime, serviceIds, 3);
    }

    @Test
    void shouldUseCurrentTimeWhenDateTimeNotProvided() {
        // given
        when(gtfsDataService.getStopIdsByStopName(anyString())).thenReturn(List.of("stop1"));
        when(gtfsDataService.getActiveServiceIds(anyString(), any())).thenReturn(Set.of("service1"));
        when(gtfsDataService.findTopDirectTrips(anyString(), anyList(), anyList(), any(), anySet(), anyInt()))
                .thenReturn(List.of());

        // when
        List<ConnectionDto> result = connectionsTool.findConnections("T", "AGH / UR", "Dworzec", null);

        // then
        assertNotNull(result);
        verify(gtfsDataService, times(1)).findTopDirectTrips(
                eq("T"),
                anyList(),
                anyList(),
                any(LocalDateTime.class),
                anySet(),
                eq(3)
        );
    }

    @Test
    void shouldReturnEmptyListWhenFromStopNotFound() {
        // given
        when(gtfsDataService.getStopIdsByStopName("NonExistent")).thenReturn(List.of());
        when(gtfsDataService.getStopIdsByStopName("Dworzec")).thenReturn(List.of("stop1"));

        // when
        List<ConnectionDto> result = connectionsTool.findConnections("T", "NonExistent", "Dworzec", null);

        // then
        assertTrue(result.isEmpty());
        verify(gtfsDataService, never()).getActiveServiceIds(anyString(), any());
        verify(gtfsDataService, never()).findTopDirectTrips(anyString(), anyList(), anyList(), any(), anySet(), anyInt());
    }

    @Test
    void shouldReturnEmptyListWhenToStopNotFound() {
        // given
        when(gtfsDataService.getStopIdsByStopName("AGH / UR")).thenReturn(List.of("stop1"));
        when(gtfsDataService.getStopIdsByStopName("NonExistent")).thenReturn(List.of());

        // when
        List<ConnectionDto> result = connectionsTool.findConnections("T", "AGH / UR", "NonExistent", null);

        // then
        assertTrue(result.isEmpty());
        verify(gtfsDataService, never()).getActiveServiceIds(anyString(), any());
        verify(gtfsDataService, never()).findTopDirectTrips(anyString(), anyList(), anyList(), any(), anySet(), anyInt());
    }

    @Test
    void shouldThrowExceptionWhenFeedTypeIsMissing() {
        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> connectionsTool.findConnections(null, "AGH / UR", "Dworzec", null)
        );
        assertTrue(exception.getMessage().contains("feedType"));
    }

    @Test
    void shouldThrowExceptionWhenFromStopNameIsMissing() {
        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> connectionsTool.findConnections("T", null, "Dworzec", null)
        );
        assertTrue(exception.getMessage().contains("fromStopName"));
    }

    @Test
    void shouldThrowExceptionWhenToStopNameIsMissing() {
        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> connectionsTool.findConnections("T", "AGH / UR", null, null)
        );
        assertTrue(exception.getMessage().contains("toStopName"));
    }

    @Test
    void shouldThrowExceptionWhenMultipleParametersAreMissing() {
        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> connectionsTool.findConnections(null, null, "Dworzec", null)
        );
        assertTrue(exception.getMessage().contains("feedType"));
        assertTrue(exception.getMessage().contains("fromStopName"));
    }

    @Test
    void shouldHandleBlankParameters() {
        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> connectionsTool.findConnections("", "AGH / UR", "Dworzec", null));
        assertThrows(IllegalArgumentException.class,
                () -> connectionsTool.findConnections("T", "", "Dworzec", null));
        assertThrows(IllegalArgumentException.class,
                () -> connectionsTool.findConnections("T", "AGH / UR", "", null));
    }
}