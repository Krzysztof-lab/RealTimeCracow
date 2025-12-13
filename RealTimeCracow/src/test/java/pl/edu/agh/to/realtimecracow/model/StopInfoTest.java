package pl.edu.agh.to.realtimecracow.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StopInfo")
class StopInfoTest {

    @Nested
    @DisplayName("Record functionality")
    class RecordFunctionalityTests {

        @Test
        @DisplayName("should create StopInfo with all fields")
        void shouldCreateStopInfoWithAllFields() {
            StopInfo stopInfo = new StopInfo("stop123", "Rynek Główny", "12:30:00", "12:31:00", 5);

            assertEquals("stop123", stopInfo.stopId());
            assertEquals("Rynek Główny", stopInfo.stopName());
            assertEquals("12:30:00", stopInfo.arrivalTime());
            assertEquals("12:31:00", stopInfo.departureTime());
            assertEquals(5, stopInfo.stopSequence());
        }

        @Test
        @DisplayName("should allow null string values")
        void shouldAllowNullStringValues() {
            StopInfo stopInfo = new StopInfo(null, null, null, null, 1);

            assertNull(stopInfo.stopId());
            assertNull(stopInfo.stopName());
            assertNull(stopInfo.arrivalTime());
            assertNull(stopInfo.departureTime());
            assertEquals(1, stopInfo.stopSequence());
        }

        @Test
        @DisplayName("should be equal when same values")
        void shouldBeEqualWhenSameValues() {
            StopInfo stopInfo1 = new StopInfo("id", "name", "10:00:00", "10:01:00", 1);
            StopInfo stopInfo2 = new StopInfo("id", "name", "10:00:00", "10:01:00", 1);

            assertEquals(stopInfo1, stopInfo2);
            assertEquals(stopInfo1.hashCode(), stopInfo2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when different stopSequence")
        void shouldNotBeEqualWhenDifferentStopSequence() {
            StopInfo stopInfo1 = new StopInfo("id", "name", "10:00:00", "10:01:00", 1);
            StopInfo stopInfo2 = new StopInfo("id", "name", "10:00:00", "10:01:00", 2);

            assertNotEquals(stopInfo1, stopInfo2);
        }

        @Test
        @DisplayName("should not be equal when different stopId")
        void shouldNotBeEqualWhenDifferentStopId() {
            StopInfo stopInfo1 = new StopInfo("id1", "name", "10:00:00", "10:01:00", 1);
            StopInfo stopInfo2 = new StopInfo("id2", "name", "10:00:00", "10:01:00", 1);

            assertNotEquals(stopInfo1, stopInfo2);
        }

        @Test
        @DisplayName("should handle zero stop sequence")
        void shouldHandleZeroStopSequence() {
            StopInfo stopInfo = new StopInfo("id", "name", "10:00:00", "10:01:00", 0);

            assertEquals(0, stopInfo.stopSequence());
        }

        @Test
        @DisplayName("should handle negative stop sequence")
        void shouldHandleNegativeStopSequence() {
            StopInfo stopInfo = new StopInfo("id", "name", "10:00:00", "10:01:00", -1);

            assertEquals(-1, stopInfo.stopSequence());
        }

        @Test
        @DisplayName("should have readable toString")
        void shouldHaveReadableToString() {
            StopInfo stopInfo = new StopInfo("stop123", "Rynek", "12:00:00", "12:01:00", 3);

            String toString = stopInfo.toString();

            assertTrue(toString.contains("stop123"));
            assertTrue(toString.contains("Rynek"));
            assertTrue(toString.contains("12:00:00"));
            assertTrue(toString.contains("12:01:00"));
            assertTrue(toString.contains("3"));
        }

        @Test
        @DisplayName("should handle same arrival and departure time")
        void shouldHandleSameArrivalAndDepartureTime() {
            StopInfo stopInfo = new StopInfo("id", "Terminal Stop", "15:00:00", "15:00:00", 1);

            assertEquals(stopInfo.arrivalTime(), stopInfo.departureTime());
        }

        @Test
        @DisplayName("should handle large stop sequence numbers")
        void shouldHandleLargeStopSequenceNumbers() {
            StopInfo stopInfo = new StopInfo("id", "name", "10:00:00", "10:01:00", Integer.MAX_VALUE);

            assertEquals(Integer.MAX_VALUE, stopInfo.stopSequence());
        }
    }
}
