package pl.edu.agh.to.realtimecracow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.edu.agh.to.realtimecracow.entity.Route;

@Repository
public interface RouteRepository extends JpaRepository<Route, Route.RouteId> {

    @Modifying
    @Query("DELETE FROM Route r WHERE r.feedType = :feedType")
    void deleteByFeedType(String feedType);
}
