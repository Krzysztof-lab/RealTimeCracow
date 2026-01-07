package pl.edu.agh.to.realtimecracow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.agh.to.realtimecracow.entity.ServiceCalendarDate;

import java.util.List;

public interface ServiceCalendarDateRepository extends JpaRepository<ServiceCalendarDate, Long> {
    List<ServiceCalendarDate> findByDate(String date); // YYYYMMDD
}