package pl.edu.agh.to.realtimecracow.service;

import com.google.transit.realtime.GtfsRealtime;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import pl.edu.agh.to.realtimecracow.model.Departure;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Scanner;

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

    public Departure getRandomDeparture() throws IOException {
        byte[] data = webClient.get()
                .uri("/TripUpdates.pb")
                .retrieve()
                .bodyToMono(byte[].class)
                .block();

        return parseRandomTripUpdate(data);
    }


    private Departure parseRandomTripUpdate(byte[] data) throws IOException {
        GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(data);

        GtfsRealtime.FeedEntity entity = feed.getEntity(0);

        //ZMIENNE KTÓRYCH SZUKAMY
        String lineNumber = "";
        String stopName = "";
        long time;

        //POBRANIE CZASU ODJAZDU I JEGO FORMATOWANIE
        time = entity.getTripUpdate().getStopTimeUpdate(0).getDeparture().getTime();
        Instant departureTime = Instant.ofEpochSecond(time);
        String formattedTime = departureTime.atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        System.out.println(formattedTime);

        //SZUKANIE NUMERU LINII W PLIKACH GTFS
        String id = entity.getId();

        char type = id.charAt(0);
        String line_id = id.substring(7);
        ClassPathResource resource = new ClassPathResource("gtfs/" + type + "/trips.txt");
        InputStream in = resource.getInputStream();

        Scanner scanner = new Scanner(in);
        scanner.nextLine(); // skip header line

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] parts = Arrays.stream(line.split(","))
                    .map(s -> s.replace("\"", "").trim())
                    .toArray(String[]::new);

            if (Arrays.stream(parts).anyMatch(p -> p.contains(line_id))) {
                lineNumber = parts[0];
            }
        }


        //SZUKANIE NAZWY PRZYSTANKU W PLIKACH GTFS
        String stop_sequence = String.valueOf(entity.getTripUpdate().getStopTimeUpdate(0).getStopSequence());
        String stop_id = "";

        resource = new ClassPathResource("gtfs/" + type + "/stop_times.txt");
        in = resource.getInputStream();
        scanner = new Scanner(in);
        scanner.nextLine(); // skip header line
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] parts = Arrays.stream(line.split(","))
                    .map(s -> s.replace("\"", "").trim())
                    .toArray(String[]::new);

            if (parts[0].equals(line_id) && parts[4].equals(stop_sequence)) {
                 stop_id = parts[3];
                 break;
            }
        }
        resource = new ClassPathResource("gtfs/" + type + "/stops.txt");
        in = resource.getInputStream();
        scanner = new Scanner(in);
        scanner.nextLine(); // skip header line
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] parts = Arrays.stream(line.split(","))
                    .map(s -> s.replace("\"", "").trim())
                    .toArray(String[]::new);

            if (parts[0].equals(stop_id)) {
                stopName = parts[2];
            }
        }

        return new Departure(stopName, lineNumber, formattedTime);
    }
}