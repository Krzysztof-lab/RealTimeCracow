package pl.edu.agh.to.realtimecracow.controller;


import com.google.protobuf.InvalidProtocolBufferException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.to.realtimecracow.model.Departure;
import pl.edu.agh.to.realtimecracow.service.TripService;

@RestController
public class TripController {
    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @RequestMapping("/departures")
    public Departure getRandomDeparture() throws InvalidProtocolBufferException {
        return tripService.getRandomDeparture();
    }
}
