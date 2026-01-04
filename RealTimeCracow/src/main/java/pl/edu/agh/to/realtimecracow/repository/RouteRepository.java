package pl.edu.agh.to.realtimecracow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.edu.agh.to.realtimecracow.entity.Route;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Route.RouteId> {

    Optional<Route> findByRouteId(String routeId);

    List<Route> findByFeedType(String feedType);

    Optional<Route> findByRouteShortName(String shortName);

    @Modifying
    @Query("DELETE FROM Route r WHERE r.feedType = :feedType")
    void deleteByFeedType(String feedType);

    long countByFeedType(String feedType);
}
