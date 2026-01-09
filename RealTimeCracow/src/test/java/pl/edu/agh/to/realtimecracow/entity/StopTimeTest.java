package pl.edu.agh.to.realtimecracow.entity;

import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pl.edu.agh.to.realtimecracow.repository.StopTimeRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("StopTimeTest")
public class StopTimeTest
{

    @Autowired
    private StopTimeRepository stopTimeRepository;

    @Test
    public void shouldSaveAndLoadByCompositeId() {
        StopTime st = new StopTime(
                "20",
                1,
                "A",
                "23",
                "08:00:00",
                "08:01:00"
        );
        stopTimeRepository.save(st);

        StopTime.StopTimeId id = new StopTime.StopTimeId("20", 1, "A");
        StopTime loaded = stopTimeRepository.findById(id).orElseThrow();

        assertEquals("23", loaded.getStopId());
        assertEquals("08:00:00", loaded.getArrivalTime());
        assertEquals("08:01:00", loaded.getDepartureTime());
    }

    @Test
    @Transactional
    void shouldUpdateExistingRow() {
        StopTime st = new StopTime(
                "10",
                2,
                "M",
                "20",
                "09:00:00",
                "09:02:00"
        );
        stopTimeRepository.save(st);

        StopTime.StopTimeId id = new StopTime.StopTimeId("10", 2, "M");
        StopTime loaded = stopTimeRepository.findById(id).orElseThrow();

        loaded.setDepartureTime("09:03:00");
        stopTimeRepository.save(loaded);

        StopTime reloaded = stopTimeRepository.findById(id).orElseThrow();
        assertEquals("09:03:00", reloaded.getDepartureTime());
    }

    @Test
    @Transactional
    void shouldNotCreateDuplicateForSameCompositeId() {
        stopTimeRepository.save(new StopTime("1", 1, "A", "s2", "10:00:00", "10:01:00"));
        stopTimeRepository.save(new StopTime("1", 1, "A", "s3", "19:00:00", "19:02:00"));

        assertEquals(1, stopTimeRepository.count());
        StopTime loaded = stopTimeRepository.findById(new StopTime.StopTimeId("1", 1, "A")).orElseThrow();
        assertEquals("19:02:00", loaded.getDepartureTime());
        assertEquals("s3", loaded.getStopId());


    }

}
