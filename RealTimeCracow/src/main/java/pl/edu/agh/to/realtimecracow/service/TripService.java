package pl.edu.agh.to.realtimecracow.service;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.transit.realtime.GtfsRealtime;
import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import pl.edu.agh.to.realtimecracow.model.Departure;

@Service
public class TripService {
    private final WebClient webClient;

    public TripService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://gtfs.ztp.krakow.pl")
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs()
                                .maxInMemorySize(10 * 1024 * 1024))
                        .build())
                .build();
    }

    public Departure getRandomDeparture() throws InvalidProtocolBufferException {
        byte[] data = webClient.get()
                .uri("/TripUpdates.pb")
                .retrieve()
                .bodyToMono(byte[].class)
                .block();

        Departure randomDeparutre =  parseTripUpdates(data);

        return randomDeparutre;
    }


    private Departure parseTripUpdates(byte[] data) throws InvalidProtocolBufferException {
        GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(data);

        System.out.println(feed);


//        for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
//            if (entity.hasTripUpdate()) {
//                var update = entity.getTripUpdate();
//                var stopTimeUpdate = update.getStopTimeUpdate(0);
//                return new Departure(
//                        stopTimeUpdate.getStopId(),
//                        update.getTrip().getRouteId(),
//                        stopTimeUpdate.getArrival().getTime() + ""
//                );
//            }
//        }

        return new Departure("No data", "-", "-");
    }


}