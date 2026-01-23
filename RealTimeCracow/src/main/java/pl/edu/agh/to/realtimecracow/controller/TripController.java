package pl.edu.agh.to.realtimecracow.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.to.realtimecracow.model.Departure;
import pl.edu.agh.to.realtimecracow.service.TripService;
import java.time.LocalDateTime;


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
            summary = "Najbliższe bezpośrednie połączenie",
            description = "Najszybsze bezpośrednie połączenie między przystankami z uwzględnieniem kalendarza"
    )
    public ResponseEntity<Departure> getNextDirect(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(required = false) String at,
            @RequestParam String feedType
    ) {
        LocalDateTime dateTime = (at == null) ? LocalDateTime.now() : LocalDateTime.parse(at);

        return tripService.getNextDirectDeparture(feedType, from, to, dateTime)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
