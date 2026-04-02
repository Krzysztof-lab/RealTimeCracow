package pl.edu.agh.to.realtimecracow.mcp.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.to.realtimecracow.mcp.service.StopsService;
import pl.edu.agh.to.realtimecracow.mcp.dto.StopDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StopsToolTest {

    @Mock
    private StopsService mcpStopsService;

    private StopsTool stopsTool;

    @BeforeEach
    void setUp() {
        stopsTool = new StopsTool(mcpStopsService);
    }

    @Test
    void shouldReturnListOfStops() {
        // given
        List<StopDto> expectedStops = List.of(
                new StopDto("AGH / UR"),
                new StopDto("Rondo Mogilskie"),
                new StopDto("Dworzec Główny")
        );
        when(mcpStopsService.listStops()).thenReturn(expectedStops);

        // when
        List<StopDto> result = stopsTool.listStops();

        // then
        assertEquals(expectedStops, result);
        assertEquals(3, result.size());
        verify(mcpStopsService, times(1)).listStops();
    }

    @Test
    void shouldReturnEmptyListWhenNoStops() {
        // given
        when(mcpStopsService.listStops()).thenReturn(List.of());

        // when
        List<StopDto> result = stopsTool.listStops();

        // then
        assertTrue(result.isEmpty());
        verify(mcpStopsService, times(1)).listStops();
    }
}