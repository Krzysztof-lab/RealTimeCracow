package pl.edu.agh.to.realtimecracow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "gtfs.files")
@Component
public class GtfsFileNamesConfig {
    private String stops;
    private String routes;
    private String trips;
    private String stopTimes;
    private String calendar;
    private String calendarDates;
}