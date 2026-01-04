package pl.edu.agh.to.realtimecracow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.edu.agh.to.realtimecracow.model.entity.Stop;

import java.util.List;
import java.util.Optional;

@Repository
public interface StopRepository extends JpaRepository<Stop, Stop.StopId> {

    Optional<Stop> findByStopId(String stopId);

    List<Stop> findByFeedType(String feedType);

    List<Stop> findByStopNameContainingIgnoreCase(String name);

    @Modifying
    @Query("DELETE FROM Stop s WHERE s.feedType = :feedType")
    void deleteByFeedType(String feedType);

    long countByFeedType(String feedType);
}
