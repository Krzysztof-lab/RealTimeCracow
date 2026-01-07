package pl.edu.agh.to.realtimecracow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.agh.to.realtimecracow.entity.ServiceCalendar;

public interface ServiceCalendarRepository extends JpaRepository<ServiceCalendar, String> {
}