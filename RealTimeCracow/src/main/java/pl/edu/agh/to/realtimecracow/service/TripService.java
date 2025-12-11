package pl.edu.agh.to.realtimecracow.service;
import org.springframework.stereotype.Service;
import com.google.transit.realtime.GtfsRealtime;
import pl.edu.agh.to.realtimecracow.model.Departure;
import pl.edu.agh.to.realtimecracow.model.StopInfo;
import java.io.IOException;
import java.util.List;

@Service
public class TripService {
    private final GtfsClient gtfsClient;
    private final GtfsParser gtfsParser;

    public TripService(GtfsClient gtfsClient, GtfsParser gtfsParser) {
        this.gtfsClient = gtfsClient;
        this.gtfsParser = gtfsParser;
    }

    // M1 method
    public Departure getRandomDeparture() throws IOException {
        GtfsRealtime.FeedMessage feed = gtfsClient.getTripUpdatesFeed();
        if (feed.getEntityCount() == 0) {
            return new Departure("No data", "-", "-");
        }
        GtfsRealtime.FeedEntity entity = feed.getEntity(0); // The first entity as a random example
        List<StopInfo> stops = gtfsParser.getStopListForEntity(entity);
        if (stops.isEmpty()) {
            return new Departure("No data", "-", "-");
        }
        return new Departure(
                stops.getFirst().stopName(),
                gtfsParser.getLineNumber(entity.getId()),
                stops.getFirst().departureTime()
        );
    }
}