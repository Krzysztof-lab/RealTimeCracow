package pl.edu.agh.to.realtimecracow.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "service_calendar_date")
public class ServiceCalendarDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_id", nullable = false)
    private String serviceId;

    @Column(name = "date", nullable = false)
    private String date;

    @Column(name = "exception_type", nullable = false)
    private int exceptionType;

    public ServiceCalendarDate() {}

    public Long getId() { return id; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getExceptionType() { return exceptionType; }
    public void setExceptionType(int exceptionType) { this.exceptionType = exceptionType; }
}