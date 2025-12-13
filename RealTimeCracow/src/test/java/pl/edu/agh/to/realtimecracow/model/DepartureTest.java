package pl.edu.agh.to.realtimecracow.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Departure")
class DepartureTest {

    @Nested
    @DisplayName("Record functionality")
    class RecordFunctionalityTests {

        @Test
        @DisplayName("should create departure with all fields")
        void shouldCreateDepartureWithAllFields() {
            Departure departure = new Departure("Rynek Główny", "152", "12:30:00");

            assertEquals("Rynek Główny", departure.stopName());
            assertEquals("152", departure.lineNumber());
            assertEquals("12:30:00", departure.departureTime());
        }

        @Test
        @DisplayName("should allow null values")
        void shouldAllowNullValues() {
            Departure departure = new Departure(null, null, null);

            assertNull(departure.stopName());
            assertNull(departure.lineNumber());
            assertNull(departure.departureTime());
        }

        @Test
        @DisplayName("should be equal when same values")
        void shouldBeEqualWhenSameValues() {
            Departure departure1 = new Departure("Stop", "1", "10:00:00");
            Departure departure2 = new Departure("Stop", "1", "10:00:00");

            assertEquals(departure1, departure2);
            assertEquals(departure1.hashCode(), departure2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when different values")
        void shouldNotBeEqualWhenDifferentValues() {
            Departure departure1 = new Departure("Stop1", "1", "10:00:00");
            Departure departure2 = new Departure("Stop2", "1", "10:00:00");

            assertNotEquals(departure1, departure2);
        }

        @Test
        @DisplayName("should have readable toString")
        void shouldHaveReadableToString() {
            Departure departure = new Departure("Stop", "1", "10:00:00");

            String toString = departure.toString();

            assertTrue(toString.contains("Stop"));
            assertTrue(toString.contains("1"));
            assertTrue(toString.contains("10:00:00"));
        }

        @Test
        @DisplayName("should handle Polish characters")
        void shouldHandlePolishCharacters() {
            Departure departure = new Departure("Plac Wszystkich Świętych", "M1", "08:15:00");

            assertEquals("Plac Wszystkich Świętych", departure.stopName());
        }

        @Test
        @DisplayName("should handle 'No data' placeholder values")
        void shouldHandleNoDataPlaceholderValues() {
            Departure departure = new Departure("No data", "-", "-");

            assertEquals("No data", departure.stopName());
            assertEquals("-", departure.lineNumber());
            assertEquals("-", departure.departureTime());
        }
    }
}
