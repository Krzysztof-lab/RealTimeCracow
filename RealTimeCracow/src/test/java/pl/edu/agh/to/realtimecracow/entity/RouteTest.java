package pl.edu.agh.to.realtimecracow.entity;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.agh.to.realtimecracow.repository.RouteRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("RouteTest")
class RouteTest {


    @Autowired
    private RouteRepository routeRepository;

    @Test
    void shouldSaveAndLoadByCompositeId() {
        Route r  = new Route("1","M","102" );
        routeRepository.save(r);

        Route.RouteId id = new Route.RouteId("1", "M");
        Route loaded = routeRepository.findById(id).orElseThrow();

        assertEquals("102", loaded.getRouteShortName());
        assertEquals("1", loaded.getRouteId());

    }

    @Test
    @Transactional
    void shouldUpdateExistingRow() {
        routeRepository.save(new Route("2", "A", "112"));

        Route.RouteId id = new Route.RouteId("2", "A");
        Route loaded = routeRepository.findById(id).orElseThrow();

        loaded.setRouteShortName("113");
        routeRepository.save(loaded);

        Route reloaded = routeRepository.findById(id).orElseThrow();
        assertEquals("113", reloaded.getRouteShortName());
    }

    @Test
    @Transactional
    void shouldNotCreateDuplicateForSameCompositeId() {
        routeRepository.deleteAll();
        routeRepository.save(new Route("3", "A", "112"));
        routeRepository.save(new Route("3", "A", "113"));

        assertEquals(1, routeRepository.count());

        Route loaded = routeRepository.findById(new Route.RouteId("3", "A")).orElseThrow();
        assertEquals("113", loaded.getRouteShortName());
        assertEquals("3", loaded.getRouteId());
        assertEquals("A", loaded.getFeedType());

    }

}

