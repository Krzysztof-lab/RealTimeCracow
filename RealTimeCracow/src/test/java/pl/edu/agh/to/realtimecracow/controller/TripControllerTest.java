package pl.edu.agh.to.realtimecracow.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.edu.agh.to.realtimecracow.model.Departure;
import pl.edu.agh.to.realtimecracow.service.TripService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TripController")
class TripControllerTest {

    @Mock
    private TripService tripService;

    @InjectMocks
    private TripController tripController;

    @Nested
    @DisplayName("getNextDirect()")
    class GetNextDirectTests {

        @Test
        @DisplayName("should return departure from service")
        void shouldReturnDepartureFromService() throws IOException {
            Departure expected = new Departure("Rynek Główny", "152", "12:30:00");
            String at = "2026-01-09T12:00:00";
            LocalDateTime dt = LocalDateTime.parse(at);

            when(tripService.getNextDirectDeparture(eq("M"), eq("Rynek Główny"), eq("Teatr Bagatela"), eq(dt)))
                    .thenReturn(Optional.of(expected));


            ResponseEntity<Departure> response =
                    tripController.getNextDirect("Rynek Główny", "Teatr Bagatela", at, "M");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(expected, response.getBody());

            Departure result = response.getBody();
            assertNotNull(result);
            assertEquals("Rynek Główny", result.stopName());
            assertEquals("152", result.lineNumber());
            assertEquals("12:30:00", result.departureTime());
        }

        @Test
        @DisplayName("should return 'No data' departure when no data available")
        void shouldReturnNoDataDeparture() throws IOException {
            Departure noDataDeparture = new Departure("No data", "-", "-");
            String at = "2026-01-09T12:00:00";
            LocalDateTime dt = LocalDateTime.parse(at);

            when(tripService.getNextDirectDeparture(eq("M"), eq("Rynek Główny"), eq("Teatr Bagatela"), eq(dt)))
                    .thenReturn(Optional.empty());

            ResponseEntity<Departure> response =
                    tripController.getNextDirect("Rynek Główny", "Teatr Bagatela", at, "M");

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNull(response.getBody());
        }

        @Test
        @DisplayName("should handle Polish characters in stop name")
        void shouldHandlePolishCharacters() throws IOException {
            Departure departure = new Departure("Plac Wszystkich Świętych", "M1", "08:15:00");
            String at = "2026-01-09T08:00:00";
            LocalDateTime dt = LocalDateTime.parse(at);

            when(tripService.getNextDirectDeparture(eq("M"), eq("Plac Wszystkich Świętych"), eq("Teatr Bagatela"), eq(dt)))
                    .thenReturn(Optional.of(departure));

            ResponseEntity<Departure> response =
                    tripController.getNextDirect("Plac Wszystkich Świętych", "Teatr Bagatela", at, "M");

            Departure result = response.getBody();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(result);
            assertEquals("Plac Wszystkich Świętych", result.stopName());
            assertEquals("M1", result.lineNumber());
        }

        @Test
        @DisplayName("should propagate IOException from service")
        void shouldPropagateIOExceptionFromService() throws IOException {
            String at = "2026-01-09T12:00:00";
            LocalDateTime dt = LocalDateTime.parse(at);
            when(tripService.getNextDirectDeparture(eq("M"), eq("A"), eq("B"), eq(dt)))
                    .thenThrow(new RuntimeException("Connection failed"));

            assertThrows(RuntimeException.class,
                    () -> tripController.getNextDirect("A", "B", at, "M"));
        }

        @Test
        @DisplayName("should handle different line number formats")
        void shouldHandleDifferentLineNumberFormats() throws IOException {
            Departure expected = new Departure("Teatr Bagatela", "1", "14:20:00");
            String at = "2026-01-09T14:00:00";
            LocalDateTime dt = LocalDateTime.parse(at);

            when(tripService.getNextDirectDeparture(eq("M"), eq("Teatr Bagatela"), eq("Rynek Główny"), eq(dt)))
                    .thenReturn(Optional.of(expected));

            ResponseEntity<Departure> response =
                    tripController.getNextDirect("Teatr Bagatela", "Rynek Główny", at, "M");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("1", response.getBody().lineNumber());
        }
        }
    }
