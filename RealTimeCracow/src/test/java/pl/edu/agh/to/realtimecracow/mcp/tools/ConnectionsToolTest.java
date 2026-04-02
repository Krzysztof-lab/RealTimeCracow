package pl.edu.agh.to.realtimecracow.mcp.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.to.realtimecracow.mcp.dto.ConnectionDto;
import pl.edu.agh.to.realtimecracow.mcp.service.ConnectionService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectionsToolTest {

    @Mock
    private ConnectionService connectionService;

    private ConnectionsTool connectionsTool;

    @BeforeEach
    void setUp() {
        connectionsTool = new ConnectionsTool(connectionService);
    }

    @Test
    void shouldReturnTopThreeConnections() {
        // given
        String feedType = "T";
        String fromStop = "AGH / UR";
        String toStop = "Rondo Mogilskie";
        String dateTimeStr = "2025-01-23T14:00:00";
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr);

        ConnectionDto conn1 = new ConnectionDto("14:10:00", "14:20:00", 60);
        ConnectionDto conn2 = new ConnectionDto("14:25:00", "14:35:00", null);
        ConnectionDto conn3 = new ConnectionDto("14:40:00", "14:50:00", -30);

        when(connectionService.findConnections(feedType, fromStop, toStop, dateTime))
                .thenReturn(List.of(conn1, conn2, conn3));

        // when
        List<ConnectionDto> result = connectionsTool.findConnections(
                feedType, fromStop, toStop, dateTimeStr
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

        verify(connectionService, times(1)).findConnections(feedType, fromStop, toStop, dateTime);
    }

    @Test
    void shouldUseCurrentTimeWhenDateTimeNotProvided() {
        // given
        String feedType = "T";
        String fromStop = "AGH / UR";
        String toStop = "Dworzec";

        when(connectionService.findConnections(eq(feedType), eq(fromStop), eq(toStop), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // when
        List<ConnectionDto> result = connectionsTool.findConnections(feedType, fromStop, toStop, null);

        // then
        assertNotNull(result);
        verify(connectionService, times(1)).findConnections(
                eq(feedType),
                eq(fromStop),
                eq(toStop),
                any(LocalDateTime.class)
        );
    }

    @Test
    void shouldReturnEmptyListWhenFromStopNotFound() {
        // given
        String feedType = "T";
        String fromStop = "NonExistent";
        String toStop = "Dworzec";

        when(connectionService.findConnections(eq(feedType), eq(fromStop), eq(toStop), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // when
        List<ConnectionDto> result = connectionsTool.findConnections(feedType, fromStop, toStop, null);

        // then
        assertTrue(result.isEmpty());
        verify(connectionService, times(1)).findConnections(
                eq(feedType),
                eq(fromStop),
                eq(toStop),
                any(LocalDateTime.class)
        );
    }

    @Test
    void shouldReturnEmptyListWhenToStopNotFound() {
        // given
        String feedType = "T";
        String fromStop = "AGH / UR";
        String toStop = "NonExistent";

        when(connectionService.findConnections(eq(feedType), eq(fromStop), eq(toStop), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // when
        List<ConnectionDto> result = connectionsTool.findConnections(feedType, fromStop, toStop, null);

        // then
        assertTrue(result.isEmpty());
        verify(connectionService, times(1)).findConnections(
                eq(feedType),
                eq(fromStop),
                eq(toStop),
                any(LocalDateTime.class)
        );
    }

    @Test
    void shouldThrowExceptionWhenFeedTypeIsMissing() {
        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> connectionsTool.findConnections(null, "AGH / UR", "Dworzec", null)
        );
        assertTrue(exception.getMessage().contains("feedType"));
        verifyNoInteractions(connectionService);
    }

    @Test
    void shouldThrowExceptionWhenFromStopNameIsMissing() {
        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> connectionsTool.findConnections("T", null, "Dworzec", null)
        );
        assertTrue(exception.getMessage().contains("fromStopName"));
        verifyNoInteractions(connectionService);
    }

    @Test
    void shouldThrowExceptionWhenToStopNameIsMissing() {
        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> connectionsTool.findConnections("T", "AGH / UR", null, null)
        );
        assertTrue(exception.getMessage().contains("toStopName"));
        verifyNoInteractions(connectionService);
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
        verifyNoInteractions(connectionService);
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

        verifyNoInteractions(connectionService);
    }
}