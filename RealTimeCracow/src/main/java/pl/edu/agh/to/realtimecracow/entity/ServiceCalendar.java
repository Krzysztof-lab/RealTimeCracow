package pl.edu.agh.to.realtimecracow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "service_calendar")
@Data
public class ServiceCalendar {

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