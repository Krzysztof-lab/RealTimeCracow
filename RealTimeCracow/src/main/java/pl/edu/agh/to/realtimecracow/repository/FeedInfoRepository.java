package pl.edu.agh.to.realtimecracow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.agh.to.realtimecracow.model.entity.FeedInfo;

import java.util.Optional;

@Repository
public interface FeedInfoRepository extends JpaRepository<FeedInfo, String> {

    Optional<FeedInfo> findByFeedType(String feedType);
}
