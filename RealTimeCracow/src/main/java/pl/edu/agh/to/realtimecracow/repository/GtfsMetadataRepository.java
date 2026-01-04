package pl.edu.agh.to.realtimecracow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.agh.to.realtimecracow.entity.GtfsMetadata;

import java.util.Optional;

@Repository
public interface GtfsMetadataRepository extends JpaRepository<GtfsMetadata, String> {

    Optional<GtfsMetadata> findByFeedType(String feedType);
}
