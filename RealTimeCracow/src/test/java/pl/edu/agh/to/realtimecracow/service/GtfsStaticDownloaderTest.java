package pl.edu.agh.to.realtimecracow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import pl.edu.agh.to.realtimecracow.repository.GtfsMetadataRepository;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("GtfsStaticDownloader")
class GtfsStaticDownloaderTest {

    @Mock
    private GtfsMetadataRepository metadataRepository;

    @Mock
    private WebClient webClient;

    private GtfsStaticDownloader downloader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        downloader = new GtfsStaticDownloader(webClient, metadataRepository, "http://example.com");
    }

    @Test
    @DisplayName("Should cleanup temp files recursively")
    void shouldCleanupTempFilesRecursively() throws Exception {
        // Given
        Path testTempDir = tempDir.resolve("test_gtfs_cleanup");
        Files.createDirectories(testTempDir);

        Path file1 = testTempDir.resolve("file1.txt");
        Path subDir = testTempDir.resolve("subdir");
        Files.createFile(file1);
        Files.createDirectories(subDir);
        Path file2 = subDir.resolve("file2.txt");
        Files.createFile(file2);

        assertThat(Files.exists(testTempDir)).isTrue();
        assertThat(Files.exists(file1)).isTrue();
        assertThat(Files.exists(file2)).isTrue();

        // When
        downloader.cleanupTempFiles(testTempDir);

        // Then
        assertThat(Files.exists(testTempDir)).isFalse();
        assertThat(Files.exists(file1)).isFalse();
        assertThat(Files.exists(file2)).isFalse();
    }

}
