package pl.edu.agh.to.realtimecracow.entity;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.agh.to.realtimecracow.repository.TripRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("TripTest")
public class TripTest {


    @Autowired
    private TripRepository tripRepository;

    @Test
    public void shouldSaveAndLoadByCompositeId() {
        tripRepository.save( new Trip("1","A","2","3"));

        Trip.TripId id = new Trip.TripId("1", "A");
        Trip loaded = tripRepository.findById(id).orElseThrow();

        assertEquals("1", loaded.getTripId());
        assertEquals("2", loaded.getRouteId());
        assertEquals("A", loaded.getFeedType());
        assertEquals("3", loaded.getServiceId());

    }

    @Test
    @Transactional
    void shouldUpdateExistingRow() {
        tripRepository.save( new Trip("1","A","2","3"));

        Trip.TripId id = new Trip.TripId("1", "A");
        Trip loaded = tripRepository.findById(id).orElseThrow();

        loaded.setRouteId("2");

        Trip reloaded = tripRepository.findById(id).orElseThrow();
        assertEquals("2", reloaded.getRouteId());
    }

    @Test
    @Transactional
    void shouldNotCreateDuplicateForSameCompositeId() {
        tripRepository.deleteAll();

        tripRepository.save(new Trip("3", "T", "1", "2"));
        tripRepository.save(new Trip("3", "T", "11", "22"));

        assertEquals(1, tripRepository.count());


        Trip loaded = tripRepository.findById(new Trip.TripId("3", "T")).orElseThrow();
        assertEquals("11", loaded.getRouteId());
        assertEquals("22", loaded.getServiceId());

    }

}
