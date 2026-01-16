package pl.edu.agh.to.realtimecracow.mcp;

import org.springframework.stereotype.Component;
import pl.edu.agh.to.realtimecracow.mcp.dto.StopDto;
import pl.edu.agh.to.realtimecracow.repository.StopRepository;

import java.util.List;

@Component
public class McpStopsTool {

    private final StopRepository stopRepository;

    public McpStopsTool(StopRepository stopRepository) {
        this.stopRepository = stopRepository;
    }

    public List<StopDto> listStops() {
        return stopRepository.findAll()
                .stream()
                .map(s -> new StopDto(
                        s.getStopId(),
                        s.getFeedType(),
                        s.getStopName()
                ))
                .toList();
    }
}