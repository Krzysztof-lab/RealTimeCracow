package pl.edu.agh.to.realtimecracow.mcp.dto;

//
public record ConnectionDto(
        String tripId,
        String routeId,
        String routeShortName,
        String departureTimePlanned,
        String arrivalTimePlanned,
        Integer delaySeconds) {
}
