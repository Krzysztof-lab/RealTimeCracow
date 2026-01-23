package pl.edu.agh.to.realtimecracow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.edu.agh.to.realtimecracow.entity.Stop;

@Repository
public interface StopRepository extends JpaRepository<Stop, Stop.StopId> {

    @Modifying
    @Query("DELETE FROM Stop s WHERE s.feedType = :feedType")
    void deleteByFeedType(String feedType);
}
