package pl.edu.agh.to.realtimecracow.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
public class GtfsRefreshScheduler {
    private static final Logger log = LoggerFactory.getLogger(GtfsRefreshScheduler.class);
    private static final List<String> FEED_TYPES = List.of("A", "M", "T");

    private final GtfsStaticDownloader downloader;
    private final GtfsStaticLoader loader;
    private final GtfsDataService dataService;

    @Value("${gtfs.refresh.on-startup:true}")
    private boolean refreshOnStartup;

    public GtfsRefreshScheduler(GtfsStaticDownloader downloader,
                                 GtfsStaticLoader loader,
                                 GtfsDataService dataService
                                 ) {
        this.downloader = downloader;
        this.loader = loader;
        this.dataService = dataService;
    }

    @PostConstruct
    public void initOnStartup() {
        if (refreshOnStartup) {
            log.info("Checking GTFS data on startup...");

            if (!dataService.hasData()) {
                log.info("Database is empty, attempting to download GTFS data...");
                refreshAllFeeds();
            } else {
                log.info("Database has existing data: {} stops, {} trips, {} routes",
                        dataService.getStopCount(),
                        dataService.getTripCount(),
                        dataService.getRouteCount());

                checkForUpdates();
            }
        }
    }

    @Scheduled(cron = "${gtfs.refresh.cron:0 0 */6 * * *}")
    public void scheduledRefresh() {
        log.info("Running scheduled GTFS refresh check...");
        checkForUpdates();
    }

    private void checkForUpdates() {
        boolean anyUpdates = false;

        for (String feedType : FEED_TYPES) {
            try {
                Optional<GtfsStaticDownloader.DownloadResult> result = downloader.downloadIfChanged(feedType);
                if (result.isPresent()) {
                    anyUpdates = true;
                    loadFeedData(result.get());
                }
            } catch (RuntimeException e) {
                log.error("Failed to process feed {}: {}", feedType, e.getMessage(), e);
            }
        }

        if (anyUpdates) {
            dataService.refreshCache();
            log.info("GTFS data updated and cache refreshed");
        }
    }

    public void refreshAllFeeds() {
        log.info("Refreshing all GTFS feeds...");
        boolean anySuccess = false;

        for (String feedType : FEED_TYPES) {
            try {
                Optional<GtfsStaticDownloader.DownloadResult> result = downloader.downloadIfChanged(feedType);
                if (result.isPresent()) {
                    loadFeedData(result.get());
                    anySuccess = true;
                }
            } catch (Exception e) {
                log.error("Failed to download/load feed {}: {}", feedType, e.getMessage());
            }
        }

        if (anySuccess) {
            dataService.refreshCache();
            log.info("GTFS feeds refreshed successfully");
        } else if (!dataService.hasData()) {
            log.warn("Failed to download GTFS data and database is empty. Using fallback data from resources.");
        }
    }

    private void loadFeedData(GtfsStaticDownloader.DownloadResult result) {
        Path tempDir = result.filePath().getParent();
        try {
            loader.loadFromZip(result.filePath(), result.feedType());
            log.info("Successfully loaded feed {} into database", result.feedType());
        } catch (Exception e) {
            log.error("Failed to load feed {} into database: {}", result.feedType(), e.getMessage());
        } finally {
            downloader.cleanupTempFiles(tempDir);
        }
    }
}