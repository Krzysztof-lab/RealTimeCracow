package pl.edu.agh.to.realtimecracow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "route")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(Route.RouteId.class)
public class Route {

    @Id
    @Column(name = "route_id", nullable = false)
    private String routeId;

    @Id
    @Column(name = "feed_type", nullable = false, length = 1)
    private String feedType;

    @Column(name = "route_short_name")
    private String routeShortName;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @SuppressWarnings("java:S1700") // JPA @IdClass requires field names to match entity
    public static class RouteId implements Serializable {
        private String routeId;
        private String feedType;
    }
}
