package pl.edu.agh.to.realtimecracow.mcp.dto;



//
public record DepartureDto(
        String tripId,
        String routeId,
        String routeShortName,
        String stopId,
        String stopName,
        String departureTimePlanned,
        Integer delaySeconds
) {}