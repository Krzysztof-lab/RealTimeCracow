package pl.edu.agh.to.realtimecracow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.to.realtimecracow.model.Departure;
import pl.edu.agh.to.realtimecracow.service.TripService;

import java.io.IOException;

@RestController
@RequestMapping("/departures")
@Tag(name = "Departures", description = "Endpointy do pobierania informacji o odjazdach")
public class TripController {
    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping
    @Operation(
            summary = "Pobiera przykladowy odjazd",
            description = "Pobiera przykladowy odjazd"
    )
    public Departure getRandomDeparture() throws IOException {
        return tripService.getRandomDeparture();
    }
}
