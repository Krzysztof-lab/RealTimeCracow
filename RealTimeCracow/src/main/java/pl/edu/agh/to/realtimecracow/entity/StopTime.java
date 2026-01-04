package pl.edu.agh.to.realtimecracow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "stop_time")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(StopTime.StopTimeId.class)
public class StopTime {

    @Id
    @Column(name = "trip_id", nullable = false)
    private String tripId;

    @Id
    @Column(name = "stop_sequence", nullable = false)
    private Integer stopSequence;

    @Id
    @Column(name = "feed_type", nullable = false, length = 1)
    private String feedType;

    @Column(name = "stop_id")
    private String stopId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StopTimeId implements Serializable {
        private String tripId;
        private Integer stopSequence;
        private String feedType;
    }
}
