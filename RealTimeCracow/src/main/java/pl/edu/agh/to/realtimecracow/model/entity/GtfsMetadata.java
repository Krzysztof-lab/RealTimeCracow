package pl.edu.agh.to.realtimecracow.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "gtfs_metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GtfsMetadata {

    @Id
    @Column(name = "feed_type", nullable = false, length = 1)
    private String feedType;

    @Column(name = "last_download_time")
    private Instant lastDownloadTime;

    @Column(name = "last_check_time")
    private Instant lastCheckTime;

    @Column(name = "etag")
    private String etag;

    @Column(name = "last_modified")
    private String lastModified;

    @Column(name = "file_size")
    private Long fileSize;
}
