package pl.edu.agh.to.realtimecracow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TripService")
class TripServiceTest {

    @Mock
    private GtfsClient gtfsClient;

    @Mock
    private GtfsDataService gtfsDataService;

    private TripService tripService;

    @BeforeEach
    void setUp() {
        tripService = new TripService(gtfsClient, gtfsDataService);
        // Default: no data in database, use fallback GtfsParser
        when(gtfsDataService.hasData()).thenReturn(false);
    }
}
