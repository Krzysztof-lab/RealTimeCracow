package pl.edu.agh.to.realtimecracow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.edu.agh.to.realtimecracow.entity.GtfsMetadata;
import pl.edu.agh.to.realtimecracow.repository.GtfsMetadataRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

@Service
public class GtfsStaticDownloader {
    private static final Logger log = LoggerFactory.getLogger(GtfsStaticDownloader.class);

    private final WebClient webClient;
    private final GtfsMetadataRepository metadataRepository;
    private final String baseUrl;

    public GtfsStaticDownloader(WebClient webClient,
                                 GtfsMetadataRepository metadataRepository,
                                 @Value("${gtfs.base-url}") String baseUrl) {
        this.webClient = webClient;
        this.metadataRepository = metadataRepository;
        this.baseUrl = baseUrl;
    }

    public record DownloadResult(boolean downloaded, Path filePath, String feedType) {}

    public Optional<DownloadResult> downloadIfChanged(String feedType) {
        String url = baseUrl + "/GTFS_KRK_" + feedType + ".zip";
        log.info("Checking for updates: {}", url);

        try {
            HttpHeaders headers = webClient.method(HttpMethod.HEAD)
                    .uri(url)
                    .retrieve()
                    .toBodilessEntity()
                    .block()
                    .getHeaders();

            String etag = headers.getETag();
            String lastModified = headers.getFirst(HttpHeaders.LAST_MODIFIED);
            long contentLength = headers.getContentLength();

            Optional<GtfsMetadata> existingMetadata = metadataRepository.findByFeedType(feedType);
            if (existingMetadata.isPresent()) {
                GtfsMetadata metadata = existingMetadata.get();
                boolean etagMatch = etag != null && etag.equals(metadata.getEtag());
                boolean lastModifiedMatch = lastModified != null && lastModified.equals(metadata.getLastModified());

                if (etagMatch || lastModifiedMatch) {
                    log.info("Feed {} is up to date, skipping download", feedType);
                    metadata.setLastCheckTime(Instant.now());
                    metadataRepository.save(metadata);
                    return Optional.empty();
                }
            }

            log.info("Downloading feed {}: {} bytes", feedType, contentLength);
            byte[] data = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();

            if (data == null || data.length == 0) {
                log.error("Failed to download feed {}: empty response", feedType);
                return Optional.empty();
            }

            Path tempDir = Files.createTempDirectory("gtfs_");
            Path zipFile = tempDir.resolve("GTFS_KRK_" + feedType + ".zip");
            Files.write(zipFile, data);

            GtfsMetadata metadata = existingMetadata.orElse(new GtfsMetadata());
            metadata.setFeedType(feedType);
            metadata.setEtag(etag);
            metadata.setLastModified(lastModified);
            metadata.setLastDownloadTime(Instant.now());
            metadata.setLastCheckTime(Instant.now());
            metadataRepository.save(metadata);

            log.info("Downloaded feed {} successfully: {} bytes", feedType, data.length);
            return Optional.of(new DownloadResult(true, zipFile, feedType));

        } catch (Exception e) {
            log.error("Failed to download feed {}: {}", feedType, e.getMessage());
            return Optional.empty();
        }
    }

    public void cleanupTempFiles(Path tempDir) {
        try {
            if (tempDir != null && Files.exists(tempDir)) {
                Files.walk(tempDir)
                        .sorted((a, b) -> -a.compareTo(b))
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                log.warn("Failed to delete temp file: {}", path);
                            }
                        });
            }
        } catch (IOException e) {
            log.warn("Failed to cleanup temp directory: {}", e.getMessage());
        }
    }
}
