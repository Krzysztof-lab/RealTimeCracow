package pl.edu.agh.to.realtimecracow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "service_calendar_date")
public class ServiceCalendarDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "feed_type", nullable = false)
    private String feedType;

    @Setter
    @Column(name = "service_id", nullable = false)
    private String serviceId;

    @Setter
    @Column(name = "date", nullable = false)
    private String date;

    @Setter
    @Column(name = "exception_type", nullable = false)
    private int exceptionType;
}