package pl.edu.agh.to.realtimecracow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.edu.agh.to.realtimecracow.entity.StopTime;

@Repository
public interface StopTimeRepository extends JpaRepository<StopTime, StopTime.StopTimeId> {

    @Modifying
    @Query("DELETE FROM StopTime st WHERE st.feedType = :feedType")
    void deleteByFeedType(String feedType);
}
