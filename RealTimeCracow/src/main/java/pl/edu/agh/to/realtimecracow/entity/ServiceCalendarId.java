package pl.edu.agh.to.realtimecracow.entity;

import java.io.Serializable;

public record ServiceCalendarId(String feedType, String serviceId) implements Serializable {
}