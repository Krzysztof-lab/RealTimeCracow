package pl.edu.agh.to.realtimecracow.mcp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.to.realtimecracow.entity.Stop;
import pl.edu.agh.to.realtimecracow.mcp.dto.StopDto;
import pl.edu.agh.to.realtimecracow.mcp.service.StopsService;
import pl.edu.agh.to.realtimecracow.repository.StopRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class McpStopsServiceTest {

    @Mock
    private StopRepository stopRepository;

    private StopsService mcpStopsService;

    @BeforeEach
    void setUp() {
        mcpStopsService = new StopsService(stopRepository);
    }

    @Test
    void shouldReturnDistinctSortedStops() {
        // given
        Stop stop1 = createStop("1", "Rondo Mogilskie");
        Stop stop2 = createStop("2", "AGH / UR");
        Stop stop3 = createStop("3", "Rondo Mogilskie"); // duplicate
        Stop stop4 = createStop("4", "Dworzec Główny");

        when(stopRepository.findAll()).thenReturn(List.of(stop1, stop2, stop3, stop4));

        // when
        List<StopDto> result = mcpStopsService.listStops();

        // then
        assertEquals(3, result.size());
        assertEquals("AGH / UR", result.get(0).stopName());
        assertEquals("Dworzec Główny", result.get(1).stopName());
        assertEquals("Rondo Mogilskie", result.get(2).stopName());
        verify(stopRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoStops() {
        // given
        when(stopRepository.findAll()).thenReturn(List.of());

        // when
        List<StopDto> result = mcpStopsService.listStops();

        // then
        assertTrue(result.isEmpty());
        verify(stopRepository, times(1)).findAll();
    }

    @Test
    void shouldHandleSingleStop() {
        // given
        Stop stop = createStop("1", "Agatowa");
        when(stopRepository.findAll()).thenReturn(List.of(stop));

        // when
        List<StopDto> result = mcpStopsService.listStops();

        // then
        assertEquals(1, result.size());
        assertEquals("Agatowa", result.getFirst().stopName());
    }

    private Stop createStop(String id, String name) {
        Stop stop = new Stop();
        stop.setStopId(id);
        stop.setStopName(name);
        return stop;
    }
}