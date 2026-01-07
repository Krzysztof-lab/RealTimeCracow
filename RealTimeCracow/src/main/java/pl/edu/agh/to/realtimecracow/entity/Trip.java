package pl.edu.agh.to.realtimecracow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "trip")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(Trip.TripId.class)
public class Trip {

    @Id
    @Column(name = "trip_id", nullable = false)
    private String tripId;

    @Id
    @Column(name = "feed_type", nullable = false, length = 1)
    private String feedType;

    @Column(name = "route_id")
    private String routeId;

    @Column(name = "service_id")
    private String serviceId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TripId implements Serializable {
        private String tripId;
        private String feedType;
    }
}
