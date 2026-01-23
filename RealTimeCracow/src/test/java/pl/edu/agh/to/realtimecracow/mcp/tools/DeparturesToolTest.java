package pl.edu.agh.to.realtimecracow.mcp.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.to.realtimecracow.mcp.dto.DepartureDto;
import pl.edu.agh.to.realtimecracow.repository.NextDeparturesRepository;
import pl.edu.agh.to.realtimecracow.service.GtfsDataService;
import pl.edu.agh.to.realtimecracow.service.TripService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeparturesToolTest {

    @Mock
    private GtfsDataService gtfsDataService;

    @Mock
    private TripService tripService;

    private DeparturesTool departuresTool;

    @BeforeEach
    void setUp() {
        departuresTool = new DeparturesTool(gtfsDataService, tripService);
    }

    @Test
    void shouldReturnNextDepartures() {
        // given
        String feedType = "T";
        String stopName = "AGH / UR";
        String line = "4";
        LocalDateTime now = LocalDateTime.of(2025, 1, 23, 14, 30);

        List<String> stopIds = List.of("stop1", "stop2");
        Set<String> serviceIds = Set.of("service1");

        var row1 = new NextDeparturesRepository.DepartureRow("trip1", "route1", "stop1", "14:35:00");
        var row2 = new NextDeparturesRepository.DepartureRow("trip2", "route1", "stop1", "14:50:00");

        when(gtfsDataService.getStopIdsByStopName(stopName)).thenReturn(stopIds);
        when(gtfsDataService.getActiveServiceIds(feedType, now.toLocalDate())).thenReturn(serviceIds);
        when(gtfsDataService.findNextDeparturesRows(feedType, stopIds, line, now, serviceIds, 5))
                .thenReturn(List.of(row1, row2));
        when(tripService.getDepartureDelaySeconds(eq("trip1"), eq(stopIds), any()))
                .thenReturn(120); // 2 min delay
        when(tripService.getDepartureDelaySeconds(eq("trip2"), eq(stopIds), any()))
                .thenReturn(null);

        // when
        List<DepartureDto> result = departuresTool.nextDepartures(feedType, stopName, line, now.toString());

        // then
        assertEquals(2, result.size());
        assertEquals("14:35:00", result.get(0).departureTimePlanned());
        assertEquals(120, result.get(0).delaySeconds());
        assertEquals("14:50:00", result.get(1).departureTimePlanned());
        assertNull(result.get(1).delaySeconds());

        verify(gtfsDataService, times(1)).getStopIdsByStopName(stopName);
        verify(gtfsDataService, times(1)).getActiveServiceIds(feedType, now.toLocalDate());
        verify(gtfsDataService, times(1)).findNextDeparturesRows(feedType, stopIds, line, now, serviceIds, 5);
    }

    @Test
    void shouldUseCurrentTimeWhenDateTimeNotProvided() {
        // given
        String feedType = "A";
        String stopName = "Agatowa";
        String line = "164";

        when(gtfsDataService.getStopIdsByStopName(anyString())).thenReturn(List.of("stop1"));
        when(gtfsDataService.getActiveServiceIds(anyString(), any())).thenReturn(Set.of("service1"));
        when(gtfsDataService.findNextDeparturesRows(anyString(), anyList(), anyString(), any(), anySet(), anyInt()))
                .thenReturn(List.of());

        // when
        List<DepartureDto> result = departuresTool.nextDepartures(feedType, stopName, line, null);

        // then
        assertNotNull(result);
        verify(gtfsDataService, times(1)).findNextDeparturesRows(
                eq(feedType),
                anyList(),
                eq(line),
                any(LocalDateTime.class),
                anySet(),
                eq(5)
        );
    }

    @Test
    void shouldReturnEmptyListWhenNoStopsFound() {
        // given
        when(gtfsDataService.getStopIdsByStopName(anyString())).thenReturn(List.of());

        // when
        List<DepartureDto> result = departuresTool.nextDepartures("T", "NonExistent", "4", null);

        // then
        assertTrue(result.isEmpty());
        verify(gtfsDataService, never()).getActiveServiceIds(anyString(), any());
        verify(gtfsDataService, never()).findNextDeparturesRows(anyString(), anyList(), anyString(), any(), anySet(), anyInt());
    }

    @Test
    void shouldReturnEmptyListWhenNoActiveServices() {
        // given
        when(gtfsDataService.getStopIdsByStopName(anyString())).thenReturn(List.of("stop1"));
        when(gtfsDataService.getActiveServiceIds(anyString(), any())).thenReturn(Set.of());

        // when
        List<DepartureDto> result = departuresTool.nextDepartures("T", "AGH / UR", "4", null);

        // then
        assertTrue(result.isEmpty());
        verify(gtfsDataService, never()).findNextDeparturesRows(anyString(), anyList(), anyString(), any(), anySet(), anyInt());
    }

    @Test
    void shouldThrowExceptionWhenRequiredParametersAreMissing() {
        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> departuresTool.nextDepartures(null, "AGH / UR", "4", null));
        assertThrows(IllegalArgumentException.class,
                () -> departuresTool.nextDepartures("T", null, "4", null));
        assertThrows(IllegalArgumentException.class,
                () -> departuresTool.nextDepartures("T", "AGH / UR", null, null));
    }
}