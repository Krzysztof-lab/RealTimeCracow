package pl.edu.agh.to.realtimecracow.mcp;


import org.springframework.stereotype.Service;
import pl.edu.agh.to.realtimecracow.mcp.dto.StopDto;
import pl.edu.agh.to.realtimecracow.repository.StopRepository;

import java.util.List;

@Service
public class McpStopsService {

    private final StopRepository stopRepository;

    public McpStopsService(StopRepository stopRepository) {
        this.stopRepository = stopRepository;
    }

    public List<StopDto> listStops() {
        return stopRepository.findAll()
                .stream()
                .map(s -> new StopDto(
                        s.getStopId(),
                        s.getStopName(),
                        s.getFeedType()
                ))
                .toList();
    }
}