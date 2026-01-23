package pl.edu.agh.to.realtimecracow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.agh.to.realtimecracow.entity.ServiceCalendar;
import pl.edu.agh.to.realtimecracow.entity.ServiceCalendarId;

import java.util.List;

public interface ServiceCalendarRepository extends JpaRepository<ServiceCalendar, ServiceCalendarId> {
    List<ServiceCalendar> findByFeedType(String feedType);
    void deleteByFeedType(String feedType);
}