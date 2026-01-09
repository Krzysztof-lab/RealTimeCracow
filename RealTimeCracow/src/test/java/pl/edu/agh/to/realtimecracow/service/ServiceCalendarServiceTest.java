package pl.edu.agh.to.realtimecracow.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.to.realtimecracow.entity.ServiceCalendar;
import pl.edu.agh.to.realtimecracow.entity.ServiceCalendarDate;
import pl.edu.agh.to.realtimecracow.repository.ServiceCalendarDateRepository;
import pl.edu.agh.to.realtimecracow.repository.ServiceCalendarRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceCalendarService")
class ServiceCalendarServiceTest {

    @Mock
    private ServiceCalendarRepository calRepo;

    @Mock
    private ServiceCalendarDateRepository calDateRepo;

    @InjectMocks
    private ServiceCalendarService service;

    @Nested
    @DisplayName("activeServiceIds()")
    class ActiveServiceIdsTests {

        @Test
        @DisplayName("should return services active on Monday")
        void shouldReturnServicesActiveOnMonday() {
            LocalDate monday = LocalDate.of(2026, 1, 5); // Monday
            String feedType = "M";

            ServiceCalendar weekdayService = createServiceCalendar(
                    "WD1", feedType, "20260101", "20261231",
                    1, 1, 1, 1, 1, 0, 0);

            when(calRepo.findByFeedType(feedType)).thenReturn(List.of(weekdayService));
            when(calDateRepo.findByFeedTypeAndDate(feedType, "20260105")).thenReturn(List.of());

            Set<String> result = service.activeServiceIds(feedType, monday);

            assertEquals(1, result.size());
            assertTrue(result.contains("WD1"));
        }

        @Test
        @DisplayName("should return services active on Saturday")
        void shouldReturnServicesActiveOnSaturday() {
            LocalDate saturday = LocalDate.of(2026, 1, 10); // Saturday
            String feedType = "M";

            ServiceCalendar weekendService = createServiceCalendar(
                    "WE1", feedType, "20260101", "20261231",
                    0, 0, 0, 0, 0, 1, 1);

            when(calRepo.findByFeedType(feedType)).thenReturn(List.of(weekendService));
            when(calDateRepo.findByFeedTypeAndDate(feedType, "20260110")).thenReturn(List.of());

            Set<String> result = service.activeServiceIds(feedType, saturday);

            assertEquals(1, result.size());
            assertTrue(result.contains("WE1"));
        }

        @Test
        @DisplayName("should exclude services outside date range")
        void shouldExcludeServicesOutsideDateRange() {
            LocalDate date = LocalDate.of(2026, 1, 9);
            String feedType = "M";

            ServiceCalendar expiredService = createServiceCalendar(
                    "OLD1", feedType, "20250101", "20251231",
                    1, 1, 1, 1, 1, 1, 1);

            ServiceCalendar futureService = createServiceCalendar(
                    "NEW1", feedType, "20270101", "20271231",
                    1, 1, 1, 1, 1, 1, 1);

            when(calRepo.findByFeedType(feedType)).thenReturn(List.of(expiredService, futureService));
            when(calDateRepo.findByFeedTypeAndDate(feedType, "20260109")).thenReturn(List.of());

            Set<String> result = service.activeServiceIds(feedType, date);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should add service with exception type 1")
        void shouldAddServiceWithExceptionType1() {
            LocalDate date = LocalDate.of(2026, 1, 9);
            String feedType = "M";

            ServiceCalendarDate exception = new ServiceCalendarDate();
            exception.setServiceId("EXTRA1");
            exception.setDate("20260109");
            exception.setExceptionType(1);
            exception.setFeedType(feedType);

            when(calRepo.findByFeedType(feedType)).thenReturn(List.of());
            when(calDateRepo.findByFeedTypeAndDate(feedType, "20260109")).thenReturn(List.of(exception));

            Set<String> result = service.activeServiceIds(feedType, date);

            assertEquals(1, result.size());
            assertTrue(result.contains("EXTRA1"));
        }

        @Test
        @DisplayName("should remove service with exception type 2")
        void shouldRemoveServiceWithExceptionType2() {
            LocalDate date = LocalDate.of(2026, 1, 9); // Friday
            String feedType = "M";

            ServiceCalendar weekdayService = createServiceCalendar(
                    "WD1", feedType, "20260101", "20261231",
                    1, 1, 1, 1, 1, 0, 0);

            ServiceCalendarDate exception = new ServiceCalendarDate();
            exception.setServiceId("WD1");
            exception.setDate("20260109");
            exception.setExceptionType(2);
            exception.setFeedType(feedType);

            when(calRepo.findByFeedType(feedType)).thenReturn(List.of(weekdayService));
            when(calDateRepo.findByFeedTypeAndDate(feedType, "20260109")).thenReturn(List.of(exception));

            Set<String> result = service.activeServiceIds(feedType, date);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should handle multiple services and exceptions")
        void shouldHandleMultipleServicesAndExceptions() {
            LocalDate date = LocalDate.of(2026, 1, 9); // Friday
            String feedType = "M";

            ServiceCalendar service1 = createServiceCalendar(
                    "WD1", feedType, "20260101", "20261231",
                    1, 1, 1, 1, 1, 0, 0);

            ServiceCalendar service2 = createServiceCalendar(
                    "WD2", feedType, "20260101", "20261231",
                    1, 1, 1, 1, 1, 0, 0);

            ServiceCalendarDate addException = new ServiceCalendarDate();
            addException.setServiceId("EXTRA1");
            addException.setDate("20260109");
            addException.setExceptionType(1);
            addException.setFeedType(feedType);

            ServiceCalendarDate removeException = new ServiceCalendarDate();
            removeException.setServiceId("WD2");
            removeException.setDate("20260109");
            removeException.setExceptionType(2);
            removeException.setFeedType(feedType);

            when(calRepo.findByFeedType(feedType)).thenReturn(List.of(service1, service2));
            when(calDateRepo.findByFeedTypeAndDate(feedType, "20260109"))
                    .thenReturn(List.of(addException, removeException));

            Set<String> result = service.activeServiceIds(feedType, date);

            assertEquals(2, result.size());
            assertTrue(result.contains("WD1"));
            assertTrue(result.contains("EXTRA1"));
            assertFalse(result.contains("WD2"));
        }

        @Test
        @DisplayName("should handle all days of week correctly")
        void shouldHandleAllDaysOfWeekCorrectly() {
            String feedType = "M";

            ServiceCalendar allDaysService = createServiceCalendar(
                    "ALL1", feedType, "20260101", "20261231",
                    1, 1, 1, 1, 1, 1, 1);

            when(calRepo.findByFeedType(feedType)).thenReturn(List.of(allDaysService));

            // Test each day of the week
            LocalDate monday = LocalDate.of(2026, 1, 5);
            LocalDate tuesday = LocalDate.of(2026, 1, 6);
            LocalDate wednesday = LocalDate.of(2026, 1, 7);
            LocalDate thursday = LocalDate.of(2026, 1, 8);
            LocalDate friday = LocalDate.of(2026, 1, 9);
            LocalDate saturday = LocalDate.of(2026, 1, 10);
            LocalDate sunday = LocalDate.of(2026, 1, 11);

            when(calDateRepo.findByFeedTypeAndDate(feedType, "20260105")).thenReturn(List.of());
            when(calDateRepo.findByFeedTypeAndDate(feedType, "20260106")).thenReturn(List.of());
            when(calDateRepo.findByFeedTypeAndDate(feedType, "20260107")).thenReturn(List.of());
            when(calDateRepo.findByFeedTypeAndDate(feedType, "20260108")).thenReturn(List.of());
            when(calDateRepo.findByFeedTypeAndDate(feedType, "20260109")).thenReturn(List.of());
            when(calDateRepo.findByFeedTypeAndDate(feedType, "20260110")).thenReturn(List.of());
            when(calDateRepo.findByFeedTypeAndDate(feedType, "20260111")).thenReturn(List.of());

            assertTrue(service.activeServiceIds(feedType, monday).contains("ALL1"));
            assertTrue(service.activeServiceIds(feedType, tuesday).contains("ALL1"));
            assertTrue(service.activeServiceIds(feedType, wednesday).contains("ALL1"));
            assertTrue(service.activeServiceIds(feedType, thursday).contains("ALL1"));
            assertTrue(service.activeServiceIds(feedType, friday).contains("ALL1"));
            assertTrue(service.activeServiceIds(feedType, saturday).contains("ALL1"));
            assertTrue(service.activeServiceIds(feedType, sunday).contains("ALL1"));
        }

        @Test
        @DisplayName("should return empty set when no services match")
        void shouldReturnEmptySetWhenNoServicesMatch() {
            LocalDate date = LocalDate.of(2026, 1, 10); // Saturday
            String feedType = "M";

            ServiceCalendar weekdayOnly = createServiceCalendar(
                    "WD1", feedType, "20260101", "20261231",
                    1, 1, 1, 1, 1, 0, 0);

            when(calRepo.findByFeedType(feedType)).thenReturn(List.of(weekdayOnly));
            when(calDateRepo.findByFeedTypeAndDate(feedType, "20260110")).thenReturn(List.of());

            Set<String> result = service.activeServiceIds(feedType, date);

            assertTrue(result.isEmpty());
        }
    }

    private ServiceCalendar createServiceCalendar(String serviceId, String feedType,
                                                  String startDate, String endDate,
                                                  int mon, int tue, int wed, int thu, int fri, int sat, int sun) {
        ServiceCalendar cal = new ServiceCalendar();
        cal.setServiceId(serviceId);
        cal.setFeedType(feedType);
        cal.setStartDate(startDate);
        cal.setEndDate(endDate);
        cal.setMonday(mon);
        cal.setTuesday(tue);
        cal.setWednesday(wed);
        cal.setThursday(thu);
        cal.setFriday(fri);
        cal.setSaturday(sat);
        cal.setSunday(sun);
        return cal;
    }
}