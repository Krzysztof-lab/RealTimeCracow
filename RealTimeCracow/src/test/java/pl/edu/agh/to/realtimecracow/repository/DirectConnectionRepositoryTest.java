package pl.edu.agh.to.realtimecracow.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.agh.to.realtimecracow.entity.Route;
import jakarta.persistence.EntityManager;
import pl.edu.agh.to.realtimecracow.entity.Stop;
import pl.edu.agh.to.realtimecracow.entity.StopTime;
import pl.edu.agh.to.realtimecracow.entity.Trip;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("DirectConnectionRepository Integration Tests")
class DirectConnectionRepositoryTest {

    @Autowired
    private DirectConnectionRepository directConnectionRepository;

    @Autowired
    private StopRepository stopRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private StopTimeRepository stopTimeRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        Route route = new Route();
        route.setRouteId("route_152");
        route.setFeedType("M");
        route.setRouteShortName("152");
        routeRepository.save(route);

        Stop stopA = new Stop();
        stopA.setStopId("stop_A");
        stopA.setFeedType("M");
        stopA.setStopName("Rynek Główny");
        stopRepository.save(stopA);

        Stop stopB = new Stop();
        stopB.setStopId("stop_B");
        stopB.setFeedType("M");
        stopB.setStopName("Teatr Bagatela");
        stopRepository.save(stopB);

        Stop stopC = new Stop();
        stopC.setStopId("stop_C");
        stopC.setFeedType("M");
        stopC.setStopName("Dworzec Główny");
        stopRepository.save(stopC);

        Trip trip1 = new Trip();
        trip1.setTripId("trip_1");
        trip1.setFeedType("M");
        trip1.setRouteId("route_152");
        trip1.setServiceId("service_weekday");
        tripRepository.save(trip1);

        StopTime st1_1 = new StopTime();
        st1_1.setTripId("trip_1");
        st1_1.setFeedType("M");
        st1_1.setStopSequence(1);
        st1_1.setStopId("stop_A");
        st1_1.setDepartureTime("12:00:00");
        st1_1.setArrivalTime("12:00:00");
        stopTimeRepository.save(st1_1);

        StopTime st1_2 = new StopTime();
        st1_2.setTripId("trip_1");
        st1_2.setFeedType("M");
        st1_2.setStopSequence(2);
        st1_2.setStopId("stop_B");
        st1_2.setDepartureTime("12:15:00");
        st1_2.setArrivalTime("12:14:30");
        stopTimeRepository.save(st1_2);

        StopTime st1_3 = new StopTime();
        st1_3.setTripId("trip_1");
        st1_3.setFeedType("M");
        st1_3.setStopSequence(3);
        st1_3.setStopId("stop_C");
        st1_3.setDepartureTime("12:30:00");
        st1_3.setArrivalTime("12:29:30");
        stopTimeRepository.save(st1_3);

        Trip trip2 = new Trip();
        trip2.setTripId("trip_2");
        trip2.setFeedType("M");
        trip2.setRouteId("route_152");
        trip2.setServiceId("service_weekday");
        tripRepository.save(trip2);

        StopTime st2_1 = new StopTime();
        st2_1.setTripId("trip_2");
        st2_1.setFeedType("M");
        st2_1.setStopSequence(1);
        st2_1.setStopId("stop_A");
        st2_1.setDepartureTime("12:30:00");
        st2_1.setArrivalTime("12:30:00");
        stopTimeRepository.save(st2_1);

        StopTime st2_2 = new StopTime();
        st2_2.setTripId("trip_2");
        st2_2.setFeedType("M");
        st2_2.setStopSequence(2);
        st2_2.setStopId("stop_B");
        st2_2.setDepartureTime("12:45:00");
        st2_2.setArrivalTime("12:44:30");
        stopTimeRepository.save(st2_2);

        Trip trip3 = new Trip();
        trip3.setTripId("trip_3");
        trip3.setFeedType("M");
        trip3.setRouteId("route_152");
        trip3.setServiceId("service_weekday");
        tripRepository.save(trip3);

        StopTime st3_1 = new StopTime();
        st3_1.setTripId("trip_3");
        st3_1.setFeedType("M");
        st3_1.setStopSequence(1);
        st3_1.setStopId("stop_B");
        st3_1.setDepartureTime("13:00:00");
        st3_1.setArrivalTime("13:00:00");
        stopTimeRepository.save(st3_1);

        StopTime st3_2 = new StopTime();
        st3_2.setTripId("trip_3");
        st3_2.setFeedType("M");
        st3_2.setStopSequence(2);
        st3_2.setStopId("stop_A");
        st3_2.setDepartureTime("13:15:00");
        st3_2.setArrivalTime("13:14:30");
        stopTimeRepository.save(st3_2);

        Trip trip4 = new Trip();
        trip4.setTripId("trip_4");
        trip4.setFeedType("M");
        trip4.setRouteId("route_152");
        trip4.setServiceId("service_weekend");
        tripRepository.save(trip4);

        StopTime st4_1 = new StopTime();
        st4_1.setTripId("trip_4");
        st4_1.setFeedType("M");
        st4_1.setStopSequence(1);
        st4_1.setStopId("stop_A");
        st4_1.setDepartureTime("12:00:00");
        st4_1.setArrivalTime("12:00:00");
        stopTimeRepository.save(st4_1);

        StopTime st4_2 = new StopTime();
        st4_2.setTripId("trip_4");
        st4_2.setFeedType("M");
        st4_2.setStopSequence(2);
        st4_2.setStopId("stop_B");
        st4_2.setDepartureTime("12:15:00");
        st4_2.setArrivalTime("12:14:30");
        stopTimeRepository.save(st4_2);

        entityManager.flush();
    }

    @Test
    @DisplayName("Should find connection when stop A is before stop B in trip")
    void shouldFindConnectionWhenStopABeforeStopB() {
        // When
        DirectConnectionRepository.DirectTripRow result = directConnectionRepository.findBestDirectTrip(
                "M",
                List.of("stop_A"),
                List.of("stop_B"),
                "12:00:00",
                Set.of("service_weekday")
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.tripId()).isEqualTo("trip_1");
        assertThat(result.routeId()).isEqualTo("route_152");
        assertThat(result.departureTime()).isEqualTo("12:00:00");
        assertThat(result.arrivalTime()).isEqualTo("12:14:30");
    }

    @Test
    @DisplayName("Should not find connection when stop A is after stop B")
    void shouldNotFindConnectionWhenWrongSequence() {
        DirectConnectionRepository.DirectTripRow result = directConnectionRepository.findBestDirectTrip(
                "M",
                List.of("stop_C"),
                List.of("stop_A"),
                "12:00:00",
                Set.of("service_weekday")
        );

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should not find connection when departure time is before requested time")
    void shouldNotFindConnectionWhenDepartureTimeBeforeRequested() {
        // When
        DirectConnectionRepository.DirectTripRow result = directConnectionRepository.findBestDirectTrip(
                "M",
                List.of("stop_A"),
                List.of("stop_B"),
                "12:05:00",
                Set.of("service_weekday")
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.tripId()).isEqualTo("trip_2");
        assertThat(result.departureTime()).isEqualTo("12:30:00");
    }

    @Test
    @DisplayName("Should not find connection when service_id is not active")
    void shouldNotFindConnectionWhenServiceIdNotActive() {
        // When
        DirectConnectionRepository.DirectTripRow result = directConnectionRepository.findBestDirectTrip(
                "M",
                List.of("stop_A"),
                List.of("stop_B"),
                "12:00:00",
                Set.of("service_special")
        );

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return earliest trip when multiple trips match")
    void shouldReturnEarliestTripWhenMultipleMatch() {
        // When
        DirectConnectionRepository.DirectTripRow result = directConnectionRepository.findBestDirectTrip(
                "M",
                List.of("stop_A"),
                List.of("stop_B"),
                "11:00:00",
                Set.of("service_weekday")
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.tripId()).isEqualTo("trip_1");
        assertThat(result.departureTime()).isEqualTo("12:00:00");
    }

    @Test
    @DisplayName("Should handle multiple stop IDs for same stop name")
    void shouldHandleMultipleStopIds() {
        // Given
        Stop stopA2 = new Stop();
        stopA2.setStopId("stop_A2");
        stopA2.setFeedType("M");
        stopA2.setStopName("Rynek Główny Platform 2");
        stopRepository.save(stopA2);

        Trip trip5 = new Trip();
        trip5.setTripId("trip_5");
        trip5.setFeedType("M");
        trip5.setRouteId("route_152");
        trip5.setServiceId("service_weekday");
        tripRepository.save(trip5);

        StopTime st5_1 = new StopTime();
        st5_1.setTripId("trip_5");
        st5_1.setFeedType("M");
        st5_1.setStopSequence(1);
        st5_1.setStopId("stop_A2"); // Alternative stop ID
        st5_1.setDepartureTime("11:00:00");
        st5_1.setArrivalTime("11:00:00");
        stopTimeRepository.save(st5_1);

        StopTime st5_2 = new StopTime();
        st5_2.setTripId("trip_5");
        st5_2.setFeedType("M");
        st5_2.setStopSequence(2);
        st5_2.setStopId("stop_B");
        st5_2.setDepartureTime("11:15:00");
        st5_2.setArrivalTime("11:14:30");
        stopTimeRepository.save(st5_2);

        entityManager.flush();

        // When
        DirectConnectionRepository.DirectTripRow result = directConnectionRepository.findBestDirectTrip(
                "M",
                List.of("stop_A", "stop_A2"), // Multiple stop IDs
                List.of("stop_B"),
                "11:00:00",
                Set.of("service_weekday")
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.tripId()).isEqualTo("trip_5");
        assertThat(result.departureTime()).isEqualTo("11:00:00");
    }

    @Test
    @DisplayName("Should handle different feed types separately")
    void shouldHandleDifferentFeedTypes() {
        // Given
        Route routeTram = new Route();
        routeTram.setRouteId("route_1");
        routeTram.setFeedType("A");
        routeTram.setRouteShortName("1");
        routeRepository.save(routeTram);

        Stop stopATram = new Stop();
        stopATram.setStopId("stop_A");
        stopATram.setFeedType("A");
        stopATram.setStopName("Rynek Główny Tram");
        stopRepository.save(stopATram);

        Stop stopBTram = new Stop();
        stopBTram.setStopId("stop_B");
        stopBTram.setFeedType("A");
        stopBTram.setStopName("Teatr Bagatela Tram");
        stopRepository.save(stopBTram);

        Trip tripTram = new Trip();
        tripTram.setTripId("trip_tram");
        tripTram.setFeedType("A");
        tripTram.setRouteId("route_1");
        tripTram.setServiceId("service_weekday");
        tripRepository.save(tripTram);

        StopTime stTram1 = new StopTime();
        stTram1.setTripId("trip_tram");
        stTram1.setFeedType("A");
        stTram1.setStopSequence(1);
        stTram1.setStopId("stop_A");
        stTram1.setDepartureTime("12:00:00");
        stTram1.setArrivalTime("12:00:00");
        stopTimeRepository.save(stTram1);

        StopTime stTram2 = new StopTime();
        stTram2.setTripId("trip_tram");
        stTram2.setFeedType("A");
        stTram2.setStopSequence(2);
        stTram2.setStopId("stop_B");
        stTram2.setDepartureTime("12:10:00");
        stTram2.setArrivalTime("12:09:30");
        stopTimeRepository.save(stTram2);

        entityManager.flush();

        // When
        DirectConnectionRepository.DirectTripRow resultBus = directConnectionRepository.findBestDirectTrip(
                "M",
                List.of("stop_A"),
                List.of("stop_B"),
                "12:00:00",
                Set.of("service_weekday")
        );

        // Then
        assertThat(resultBus).isNotNull();
        assertThat(resultBus.tripId()).isEqualTo("trip_1");

        // When
        DirectConnectionRepository.DirectTripRow resultTram = directConnectionRepository.findBestDirectTrip(
                "A",
                List.of("stop_A"),
                List.of("stop_B"),
                "12:00:00",
                Set.of("service_weekday")
        );

        // Then
        assertThat(resultTram).isNotNull();
        assertThat(resultTram.tripId()).isEqualTo("trip_tram");
    }
}
