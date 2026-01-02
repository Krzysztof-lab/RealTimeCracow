package pl.edu.agh.to.realtimecracow.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.to.realtimecracow.model.Departure;
import pl.edu.agh.to.realtimecracow.service.TripService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TripController")
class TripControllerTest {

    @Mock
    private TripService tripService;

    @InjectMocks
    private TripController tripController;

    @Nested
    @DisplayName("getRandomDeparture()")
    class GetRandomDepartureTests {

        @Test
        @DisplayName("should return departure from service")
        void shouldReturnDepartureFromService() throws IOException {
            Departure expected = new Departure("Rynek Główny", "152", "12:30:00");
            when(tripService.getRandomDeparture()).thenReturn(expected);

            Departure result = tripController.getRandomDeparture();

            assertEquals(expected, result);
            assertEquals("Rynek Główny", result.stopName());
            assertEquals("152", result.lineNumber());
            assertEquals("12:30:00", result.departureTime());
        }

        @Test
        @DisplayName("should return 'No data' departure when no data available")
        void shouldReturnNoDataDeparture() throws IOException {
            Departure noDataDeparture = new Departure("No data", "-", "-");
            when(tripService.getRandomDeparture()).thenReturn(noDataDeparture);

            Departure result = tripController.getRandomDeparture();

            assertEquals("No data", result.stopName());
            assertEquals("-", result.lineNumber());
            assertEquals("-", result.departureTime());
        }

        @Test
        @DisplayName("should handle Polish characters in stop name")
        void shouldHandlePolishCharacters() throws IOException {
            Departure departure = new Departure("Plac Wszystkich Świętych", "M1", "08:15:00");
            when(tripService.getRandomDeparture()).thenReturn(departure);

            Departure result = tripController.getRandomDeparture();

            assertEquals("Plac Wszystkich Świętych", result.stopName());
            assertEquals("M1", result.lineNumber());
        }

        @Test
        @DisplayName("should handle null line number")
        void shouldHandleNullLineNumber() throws IOException {
            Departure departure = new Departure("Some Stop", null, "10:00:00");
            when(tripService.getRandomDeparture()).thenReturn(departure);

            Departure result = tripController.getRandomDeparture();

            assertEquals("Some Stop", result.stopName());
            assertNull(result.lineNumber());
            assertEquals("10:00:00", result.departureTime());
        }

        @Test
        @DisplayName("should propagate IOException from service")
        void shouldPropagateIOExceptionFromService() throws IOException {
            when(tripService.getRandomDeparture()).thenThrow(new IOException("Connection failed"));

            assertThrows(IOException.class, () -> tripController.getRandomDeparture());
        }

        @Test
        @DisplayName("should handle different line number formats")
        void shouldHandleDifferentLineNumberFormats() throws IOException {
            Departure tramDeparture = new Departure("Teatr Bagatela", "1", "14:20:00");
            when(tripService.getRandomDeparture()).thenReturn(tramDeparture);

            Departure result = tripController.getRandomDeparture();

            assertEquals("1", result.lineNumber());
        }

        @Test
        @DisplayName("should delegate to TripService correctly")
        void shouldDelegateToTripServiceCorrectly() throws IOException {
            Departure departure = new Departure("Test Stop", "999", "23:59:59");
            when(tripService.getRandomDeparture()).thenReturn(departure);

            Departure result = tripController.getRandomDeparture();

            assertNotNull(result);
            assertEquals(departure.stopName(), result.stopName());
            assertEquals(departure.lineNumber(), result.lineNumber());
            assertEquals(departure.departureTime(), result.departureTime());
        }
    }
}
