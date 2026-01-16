package pl.edu.agh.to.realtimecracow.mcp.dto;

public record DepartureDto(
        String tripId,
        String plannedDeparture,
        String realtimeDeparture,
        Integer delaySeconds
) {}