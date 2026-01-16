package pl.edu.agh.to.realtimecracow.mcp.dto;



public record ConnectionDto(
                            String routeId,
                            String plannedDeparture,
                            String realtimeDeparture,
                            Integer delaySeconds,
                            String plannedArrival) {
}
