package pl.edu.agh.to.realtimecracow.mcp.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.to.realtimecracow.mcp.dto.DepartureDto;
import pl.edu.agh.to.realtimecracow.mcp.service.DepartureService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeparturesToolTest {

    @Mock
    private DepartureService departureService;

    private DeparturesTool departuresTool;

    @BeforeEach
    void setUp() {
        departuresTool = new DeparturesTool(departureService);
    }

    @Test
    void shouldReturnNextDepartures() {
        // given
        String feedType = "T";
        String stopName = "AGH / UR";
        String line = "4";
        String dateTimeStr = "2025-01-23T14:30:00";
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr);

        DepartureDto dep1 = new DepartureDto("14:35:00", 120);
        DepartureDto dep2 = new DepartureDto("14:50:00", null);

        when(departureService.findNextDepartures(feedType, stopName, line, dateTime))
                .thenReturn(List.of(dep1, dep2));

        // when
        List<DepartureDto> result = departuresTool.nextDepartures(feedType, stopName, line, dateTimeStr);

        // then
        assertEquals(2, result.size());
        assertEquals("14:35:00", result.get(0).departureTimePlanned());
        assertEquals(120, result.get(0).delaySeconds());
        assertEquals("14:50:00", result.get(1).departureTimePlanned());
        assertNull(result.get(1).delaySeconds());

        verify(departureService, times(1)).findNextDepartures(feedType, stopName, line, dateTime);
    }

    @Test
    void shouldUseCurrentTimeWhenDateTimeNotProvided() {
        // given
        String feedType = "A";
        String stopName = "Agatowa";
        String line = "164";

        when(departureService.findNextDepartures(eq(feedType), eq(stopName), eq(line), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // when
        List<DepartureDto> result = departuresTool.nextDepartures(feedType, stopName, line, null);

        // then
        assertNotNull(result);
        verify(departureService, times(1)).findNextDepartures(
                eq(feedType),
                eq(stopName),
                eq(line),
                any(LocalDateTime.class)
        );
    }

    @Test
    void shouldReturnEmptyListWhenNoStopsFound() {
        // given
        String feedType = "T";
        String stopName = "NonExistent";
        String line = "4";

        when(departureService.findNextDepartures(eq(feedType), eq(stopName), eq(line), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // when
        List<DepartureDto> result = departuresTool.nextDepartures(feedType, stopName, line, null);

        // then
        assertTrue(result.isEmpty());
        verify(departureService, times(1)).findNextDepartures(
                eq(feedType),
                eq(stopName),
                eq(line),
                any(LocalDateTime.class)
        );
    }

    @Test
    void shouldReturnEmptyListWhenNoActiveServices() {
        // given
        String feedType = "T";
        String stopName = "AGH / UR";
        String line = "4";

        when(departureService.findNextDepartures(eq(feedType), eq(stopName), eq(line), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // when
        List<DepartureDto> result = departuresTool.nextDepartures(feedType, stopName, line, null);

        // then
        assertTrue(result.isEmpty());
        verify(departureService, times(1)).findNextDepartures(
                eq(feedType),
                eq(stopName),
                eq(line),
                any(LocalDateTime.class)
        );
    }

    @Test
    void shouldThrowExceptionWhenFeedTypeIsMissing() {
        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> departuresTool.nextDepartures(null, "AGH / UR", "4", null)
        );
        assertTrue(exception.getMessage().contains("feedType"));
        verifyNoInteractions(departureService);
    }

    @Test
    void shouldThrowExceptionWhenStopNameIsMissing() {
        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> departuresTool.nextDepartures("T", null, "4", null)
        );
        assertTrue(exception.getMessage().contains("stopName"));
        verifyNoInteractions(departureService);
    }

    @Test
    void shouldThrowExceptionWhenLineIsMissing() {
        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> departuresTool.nextDepartures("T", "AGH / UR", null, null)
        );
        assertTrue(exception.getMessage().contains("line"));
        verifyNoInteractions(departureService);
    }

    @Test
    void shouldThrowExceptionWhenMultipleParametersAreMissing() {
        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> departuresTool.nextDepartures(null, null, "4", null)
        );
        assertTrue(exception.getMessage().contains("feedType"));
        assertTrue(exception.getMessage().contains("stopName"));
        verifyNoInteractions(departureService);
    }

    @Test
    void shouldHandleBlankParameters() {
        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> departuresTool.nextDepartures("", "AGH / UR", "4", null));
        assertThrows(IllegalArgumentException.class,
                () -> departuresTool.nextDepartures("T", "", "4", null));
        assertThrows(IllegalArgumentException.class,
                () -> departuresTool.nextDepartures("T", "AGH / UR", "", null));

        verifyNoInteractions(departureService);
    }
}