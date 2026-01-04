package pl.edu.agh.to.realtimecracow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.edu.agh.to.realtimecracow.entity.Trip;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Trip.TripId> {

    Optional<Trip> findByTripId(String tripId);

    List<Trip> findByFeedType(String feedType);

    List<Trip> findByRouteId(String routeId);

    @Modifying
    @Query("DELETE FROM Trip t WHERE t.feedType = :feedType")
    void deleteByFeedType(String feedType);

    long countByFeedType(String feedType);
}
