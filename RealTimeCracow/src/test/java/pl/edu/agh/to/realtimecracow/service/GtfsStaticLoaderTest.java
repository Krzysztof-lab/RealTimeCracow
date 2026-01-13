package pl.edu.agh.to.realtimecracow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.to.realtimecracow.config.GtfsFileNamesConfig;
import pl.edu.agh.to.realtimecracow.entity.*;
import pl.edu.agh.to.realtimecracow.repository.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GtfsStaticLoader")
class GtfsStaticLoaderTest {

    @Mock
    private StopRepository stopRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private StopTimeRepository stopTimeRepository;

    @Mock
    private ServiceCalendarRepository serviceCalendarRepository;

    @Mock
    private ServiceCalendarDateRepository serviceCalendarDateRepository;

    private GtfsFileNamesConfig fileNamesConfig;

    @InjectMocks
    private GtfsStaticLoader loader;

    @Captor
    private ArgumentCaptor<List<Stop>> stopCaptor;

    @Captor
    private ArgumentCaptor<List<Route>> routeCaptor;

    @Captor
    private ArgumentCaptor<List<Trip>> tripCaptor;

    @Captor
    private ArgumentCaptor<List<StopTime>> stopTimeCaptor;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileNamesConfig = new GtfsFileNamesConfig();
        fileNamesConfig.setStops("stops.txt");
        fileNamesConfig.setRoutes("routes.txt");
        fileNamesConfig.setTrips("trips.txt");
        fileNamesConfig.setStopTimes("stop_times.txt");
        fileNamesConfig.setCalendar("calendar.txt");
        fileNamesConfig.setCalendarDates("calendar_dates.txt");

        loader = new GtfsStaticLoader(
                stopRepository,
                routeRepository,
                tripRepository,
                stopTimeRepository,
                serviceCalendarRepository,
                serviceCalendarDateRepository,
                fileNamesConfig
        );
    }


    @Nested
    @DisplayName("loadFromZip()")
    class LoadFromZipTests {

        @Test
        @DisplayName("should load stops from ZIP")
        void shouldLoadStopsFromZip() throws IOException {
            String stopsCsv = """
                    stop_id,stop_name
                    S1,Rynek Główny
                    S2,Teatr Bagatela
                    """;

            Path zipPath = createZipWithFile("stops.txt", stopsCsv);

            loader.loadFromZip(zipPath, "M");

            verify(stopRepository, atLeastOnce()).saveAll(stopCaptor.capture());

            List<Stop> allStops = stopCaptor.getAllValues().stream()
                    .flatMap(List::stream)
                    .toList();

            assertEquals(2, allStops.size());
            assertTrue(allStops.stream().anyMatch(s -> s.getStopId().equals("S1")));
            assertTrue(allStops.stream().anyMatch(s -> s.getStopId().equals("S2")));
        }

        @Test
        @DisplayName("should load routes from ZIP")
        void shouldLoadRoutesFromZip() throws IOException {
            String routesCsv = """
                    route_id,route_short_name
                    R1,152
                    R2,M1
                    """;

            Path zipPath = createZipWithFile("routes.txt", routesCsv);

            loader.loadFromZip(zipPath, "M");

            verify(routeRepository, atLeastOnce()).saveAll(routeCaptor.capture());

            List<Route> allRoutes = routeCaptor.getAllValues().stream()
                    .flatMap(List::stream)
                    .toList();

            assertEquals(2, allRoutes.size());
            assertTrue(allRoutes.stream().anyMatch(r -> r.getRouteId().equals("R1")));
            assertTrue(allRoutes.stream().anyMatch(r -> r.getRouteShortName().equals("152")));
        }

        @Test
        @DisplayName("should load trips from ZIP")
        void shouldLoadTripsFromZip() throws IOException {
            String tripsCsv = """
                    trip_id,route_id,service_id
                    T1,R1,WD1
                    T2,R2,WD2
                    """;

            Path zipPath = createZipWithFile("trips.txt", tripsCsv);

            loader.loadFromZip(zipPath, "M");

            verify(tripRepository, atLeastOnce()).saveAll(tripCaptor.capture());

            List<Trip> allTrips = tripCaptor.getAllValues().stream()
                    .flatMap(List::stream)
                    .toList();

            assertEquals(2, allTrips.size());
            assertTrue(allTrips.stream().anyMatch(t -> t.getTripId().equals("T1")));
            assertTrue(allTrips.stream().anyMatch(t -> t.getRouteId().equals("R1")));
        }

        @Test
        @DisplayName("should load stop times from ZIP")
        void shouldLoadStopTimesFromZip() throws IOException {
            String stopTimesCsv = """
                    trip_id,stop_id,stop_sequence,arrival_time,departure_time
                    T1,S1,1,08:00:00,08:00:00
                    T1,S2,2,08:15:00,08:15:00
                    """;

            Path zipPath = createZipWithFile("stop_times.txt", stopTimesCsv);

            loader.loadFromZip(zipPath, "M");

            verify(stopTimeRepository, atLeastOnce()).saveAll(stopTimeCaptor.capture());

            List<StopTime> allStopTimes = stopTimeCaptor.getAllValues().stream()
                    .flatMap(List::stream)
                    .toList();

            assertEquals(2, allStopTimes.size());
            assertTrue(allStopTimes.stream().anyMatch(st -> st.getStopSequence() == 1));
            assertTrue(allStopTimes.stream().anyMatch(st -> st.getArrivalTime().equals("08:00:00")));
        }

        @Test
        @DisplayName("should handle Polish characters in stop names")
        void shouldHandlePolishCharacters() throws IOException {
            String stopsCsv = """
                    stop_id,stop_name
                    S1,Plac Wszystkich Świętych
                    S2,Dworzec Główny Wschód
                    """;

            Path zipPath = createZipWithFile("stops.txt", stopsCsv);

            loader.loadFromZip(zipPath, "M");

            verify(stopRepository, atLeastOnce()).saveAll(stopCaptor.capture());

            List<Stop> allStops = stopCaptor.getAllValues().stream()
                    .flatMap(List::stream)
                    .toList();

            assertTrue(allStops.stream()
                    .anyMatch(s -> s.getStopName().equals("Plac Wszystkich Świętych")));
            assertTrue(allStops.stream()
                    .anyMatch(s -> s.getStopName().equals("Dworzec Główny Wschód")));
        }

        @Test
        @DisplayName("should handle missing optional fields")
        void shouldHandleMissingOptionalFields() throws IOException {
            String stopsCsv = """
                    stop_id
                    S1
                    S2
                    """;

            Path zipPath = createZipWithFile("stops.txt", stopsCsv);

            loader.loadFromZip(zipPath, "M");

            verify(stopRepository, atLeastOnce()).saveAll(stopCaptor.capture());

            List<Stop> allStops = stopCaptor.getAllValues().stream()
                    .flatMap(List::stream)
                    .toList();

            assertEquals(2, allStops.size());
            assertTrue(allStops.stream().allMatch(s -> s.getStopName() == null));
        }

        @Test
        @DisplayName("should handle BOM in CSV files")
        void shouldHandleBOM() throws IOException {
            // BOM (Byte Order Mark) for UTF-8
            byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
            String stopsCsv = "stop_id,stop_name\nS1,Test Stop\n";
            byte[] csvBytes = new byte[bom.length + stopsCsv.getBytes(StandardCharsets.UTF_8).length];
            System.arraycopy(bom, 0, csvBytes, 0, bom.length);
            System.arraycopy(stopsCsv.getBytes(StandardCharsets.UTF_8), 0, csvBytes, bom.length,
                    stopsCsv.getBytes(StandardCharsets.UTF_8).length);

            Path zipPath = createZipWithBytes("stops.txt", csvBytes);

            loader.loadFromZip(zipPath, "M");

            verify(stopRepository, atLeastOnce()).saveAll(stopCaptor.capture());

            List<Stop> allStops = stopCaptor.getAllValues().stream()
                    .flatMap(List::stream)
                    .toList();

            assertFalse(allStops.isEmpty());
            assertEquals("S1", allStops.getFirst().getStopId());
        }

        @Test
        @DisplayName("should load service calendar from ZIP")
        void shouldLoadServiceCalendarFromZip() throws IOException {
            String calendarCsv = """
                    service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date
                    WD1,1,1,1,1,1,0,0,20260101,20261231
                    WE1,0,0,0,0,0,1,1,20260101,20261231
                    """;

            Path zipPath = createZipWithFile("calendar.txt", calendarCsv);

            loader.loadFromZip(zipPath, "M");

            verify(serviceCalendarRepository, atLeastOnce()).saveAll(anyList());
        }

        @Test
        @DisplayName("should load service calendar dates from ZIP")
        void shouldLoadServiceCalendarDatesFromZip() throws IOException {
            String calendarDatesCsv = """
                    service_id,date,exception_type
                    WD1,20260101,2
                    EXTRA1,20260102,1
                    """;

            Path zipPath = createZipWithFile("calendar_dates.txt", calendarDatesCsv);

            loader.loadFromZip(zipPath, "M");

            verify(serviceCalendarDateRepository, atLeastOnce()).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("clearFeedData()")
    class ClearFeedDataTests {

        @Test
        @DisplayName("should clear all feed data")
        void shouldClearAllFeedData() {
            String feedType = "M";

            loader.clearFeedData(feedType);

            verify(stopTimeRepository).deleteByFeedType(feedType);
            verify(tripRepository).deleteByFeedType(feedType);
            verify(routeRepository).deleteByFeedType(feedType);
            verify(stopRepository).deleteByFeedType(feedType);
            verify(serviceCalendarDateRepository).deleteByFeedType(feedType);
            verify(serviceCalendarRepository).deleteByFeedType(feedType);
        }

        @Test
        @DisplayName("should clear data in correct order")
        void shouldClearDataInCorrectOrder() {
            String feedType = "M";

            loader.clearFeedData(feedType);

            // Verify deletion order: stop_times -> trips -> routes -> stops
            var inOrder = inOrder(stopTimeRepository, tripRepository, routeRepository,
                    stopRepository, serviceCalendarDateRepository, serviceCalendarRepository);
            inOrder.verify(stopTimeRepository).deleteByFeedType(feedType);
            inOrder.verify(tripRepository).deleteByFeedType(feedType);
            inOrder.verify(routeRepository).deleteByFeedType(feedType);
            inOrder.verify(stopRepository).deleteByFeedType(feedType);
            inOrder.verify(serviceCalendarDateRepository).deleteByFeedType(feedType);
            inOrder.verify(serviceCalendarRepository).deleteByFeedType(feedType);
        }
    }

    @Nested
    @DisplayName("Batch processing")
    class BatchProcessingTests {

        @Test
        @DisplayName("should process large files in batches")
        void shouldProcessLargeFilesInBatches() throws IOException {
            StringBuilder csvBuilder = new StringBuilder("stop_id,stop_name\n");
            // Create 2500 stops (batch size is 1000)
            for (int i = 1; i <= 2500; i++) {
                csvBuilder.append("S").append(i).append(",Stop ").append(i).append("\n");
            }

            Path zipPath = createZipWithFile("stops.txt", csvBuilder.toString());

            loader.loadFromZip(zipPath, "M");

            // Should be called at least 3 times (2500 / 1000 = 2.5, rounded up = 3)
            verify(stopRepository, atLeast(3)).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("Error handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("should handle invalid stop sequence gracefully")
        void shouldHandleInvalidStopSequence() throws IOException {
            String stopTimesCsv = """
                    trip_id,stop_id,stop_sequence,arrival_time,departure_time
                    T1,S1,invalid,08:00:00,08:00:00
                    T1,S2,2,08:15:00,08:15:00
                    """;

            Path zipPath = createZipWithFile("stop_times.txt", stopTimesCsv);

            loader.loadFromZip(zipPath, "M");

            verify(stopTimeRepository, atLeastOnce()).saveAll(stopTimeCaptor.capture());

            List<StopTime> allStopTimes = stopTimeCaptor.getAllValues().stream()
                    .flatMap(List::stream)
                    .toList();

            // Should still load the valid record
            assertTrue(allStopTimes.stream().anyMatch(st -> st.getStopSequence() != null && st.getStopSequence() == 2));
            // Invalid sequence should be null
            assertTrue(allStopTimes.stream().anyMatch(st -> st.getStopSequence() == null));
        }

        @Test
        @DisplayName("should handle empty CSV files")
        void shouldHandleEmptyCsvFiles() throws IOException {
            String stopsCsv = "stop_id,stop_name\n"; // Only header, no data

            Path zipPath = createZipWithFile("stops.txt", stopsCsv);

            loader.loadFromZip(zipPath, "M");

            // Should not throw exception
            verify(stopRepository, never()).saveAll(anyList());
        }
    }

    // Helper methods
    private Path createZipWithFile(String filename, String content) throws IOException {
        return createZipWithBytes(filename, content.getBytes(StandardCharsets.UTF_8));
    }

    private Path createZipWithBytes(String filename, byte[] content) throws IOException {
        Path zipPath = tempDir.resolve("test.zip");

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            ZipEntry entry = new ZipEntry(filename);
            zos.putNextEntry(entry);
            zos.write(content);
            zos.closeEntry();
        }

        return zipPath;
    }
}