package pl.edu.agh.to.realtimecracow.mcp.dto;

public record ConnectionDto(
        String departureTimePlanned,
        String arrivalTimePlanned,
        Integer delaySeconds) {
}
