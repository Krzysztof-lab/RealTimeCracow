package pl.edu.agh.to.realtimecracow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "feed_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedInfo {

    @Id
    @Column(name = "feed_type", nullable = false, length = 1)
    private String feedType;

    @Column(name = "feed_publisher_name")
    private String feedPublisherName;

    @Column(name = "feed_publisher_url")
    private String feedPublisherUrl;

    @Column(name = "feed_lang")
    private String feedLang;

    @Column(name = "feed_start_date")
    private String feedStartDate;

    @Column(name = "feed_end_date")
    private String feedEndDate;

    @Column(name = "feed_version")
    private String feedVersion;
}
