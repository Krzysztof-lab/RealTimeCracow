package pl.edu.agh.to.realtimecracow.service;

import org.springframework.stereotype.Service;
import pl.edu.agh.to.realtimecracow.entity.ServiceCalendar;
import pl.edu.agh.to.realtimecracow.entity.ServiceCalendarDate;
import pl.edu.agh.to.realtimecracow.repository.ServiceCalendarDateRepository;
import pl.edu.agh.to.realtimecracow.repository.ServiceCalendarRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ServiceCalendarService {

    private final ServiceCalendarRepository calRepo;
    private final ServiceCalendarDateRepository calDateRepo;

    public ServiceCalendarService(ServiceCalendarRepository calRepo,
                                  ServiceCalendarDateRepository calDateRepo) {
        this.calRepo = calRepo;
        this.calDateRepo = calDateRepo;
    }

    public Set<String> activeServiceIds(LocalDate date) {
        String yyyymmdd = date.format(DateTimeFormatter.BASIC_ISO_DATE);
        DayOfWeek dow = date.getDayOfWeek();

        Set<String> active = calRepo.findAll().stream()
                .filter(c -> c.getStartDate().compareTo(yyyymmdd) <= 0)
                .filter(c -> c.getEndDate().compareTo(yyyymmdd) >= 0)
                .filter(c -> runsOnDay(c, dow))
                .map(ServiceCalendar::getServiceId)
                .collect(Collectors.toCollection(HashSet::new));

        for (ServiceCalendarDate ex : calDateRepo.findByDate(yyyymmdd)) {
            if (ex.getExceptionType() == 1) active.add(ex.getServiceId());
            if (ex.getExceptionType() == 2) active.remove(ex.getServiceId());
        }

        return active;
    }

    private boolean runsOnDay(ServiceCalendar c, DayOfWeek dow) {
        return switch (dow) {
            case MONDAY -> c.getMonday() == 1;
            case TUESDAY -> c.getTuesday() == 1;
            case WEDNESDAY -> c.getWednesday() == 1;
            case THURSDAY -> c.getThursday() == 1;
            case FRIDAY -> c.getFriday() == 1;
            case SATURDAY -> c.getSaturday() == 1;
            case SUNDAY -> c.getSunday() == 1;
        };
    }
}