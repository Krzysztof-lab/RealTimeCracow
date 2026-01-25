package pl.edu.agh.to.realtimecracow.mcp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.to.realtimecracow.entity.Stop;
import pl.edu.agh.to.realtimecracow.mcp.dto.StopDto;
import pl.edu.agh.to.realtimecracow.repository.StopRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StopsService {

    private final StopRepository stopRepository;

    public List<StopDto> listStops() {
        return stopRepository.findAll()
                .stream()
                .map(Stop::getStopName)
                .distinct()
                .sorted()
                .map(StopDto::new)
                .toList();
    }
}