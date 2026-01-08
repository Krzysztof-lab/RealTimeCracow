package pl.edu.agh.to.realtimecracow.entity;

import java.io.Serializable;

public class ServiceCalendarId implements Serializable {
    private String feedType;
    private String serviceId;

    public ServiceCalendarId() {}

    public ServiceCalendarId(String feedType, String serviceId) {
        this.feedType = feedType;
        this.serviceId = serviceId;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceCalendarId that)) return false;
        return java.util.Objects.equals(feedType, that.feedType)
                && java.util.Objects.equals(serviceId, that.serviceId);
    }
    @Override public int hashCode() {
        return java.util.Objects.hash(feedType, serviceId);
    }
}