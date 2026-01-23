package pl.edu.agh.to.realtimecracow.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "service_calendar")
@IdClass(ServiceCalendarId.class)
@Data
public class ServiceCalendar {


    @Id
    @Column(name = "feed_type", nullable = false)
    private String feedType;

    @Id
    @Column(name = "service_id")
    private String serviceId;

    @Column(name = "monday")
    private int monday;

    @Column(name = "tuesday")
    private int tuesday;

    @Column(name = "wednesday")
    private int wednesday;

    @Column(name = "thursday")
    private int thursday;

    @Column(name = "friday")
    private int friday;

    @Column(name = "saturday")
    private int saturday;

    @Column(name = "sunday")
    private int sunday;

    @Column(name = "start_date")
    private String startDate;

    @Column(name = "end_date")
    private String endDate;
}