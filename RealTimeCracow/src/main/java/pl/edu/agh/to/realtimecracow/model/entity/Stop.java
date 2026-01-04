package pl.edu.agh.to.realtimecracow.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "stop")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(Stop.StopId.class)
public class Stop {

    @Id
    @Column(name = "stop_id", nullable = false)
    private String stopId;

    @Id
    @Column(name = "feed_type", nullable = false, length = 1)
    private String feedType;

    @Column(name = "stop_name")
    private String stopName;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StopId implements Serializable {
        private String stopId;
        private String feedType;
    }
}
