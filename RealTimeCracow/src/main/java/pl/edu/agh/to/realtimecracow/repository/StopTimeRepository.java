package pl.edu.agh.to.realtimecracow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.edu.agh.to.realtimecracow.model.entity.StopTime;

import java.util.List;
import java.util.Optional;

@Repository
public interface StopTimeRepository extends JpaRepository<StopTime, StopTime.StopTimeId> {

    List<StopTime> findByTripId(String tripId);

    List<StopTime> findByStopId(String stopId);

    Optional<StopTime> findByTripIdAndStopSequence(String tripId, Integer stopSequence);

    List<StopTime> findByFeedType(String feedType);

    @Modifying
    @Query("DELETE FROM StopTime st WHERE st.feedType = :feedType")
    void deleteByFeedType(String feedType);

    long countByFeedType(String feedType);
}
