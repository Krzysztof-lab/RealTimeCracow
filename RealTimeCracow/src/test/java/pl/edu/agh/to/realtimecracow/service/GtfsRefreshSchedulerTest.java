package pl.edu.agh.to.realtimecracow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GtfsRefreshScheduler")
class GtfsRefreshSchedulerTest {

    @Mock
    private GtfsStaticDownloader downloader;

    @Mock
    private GtfsStaticLoader loader;

    @Mock
    private GtfsDataService dataService;

    private GtfsRefreshScheduler scheduler;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        scheduler = new GtfsRefreshScheduler(downloader, loader, dataService);
        ReflectionTestUtils.setField(scheduler, "refreshOnStartup", true);
    }

    @Nested
    @DisplayName("initOnStartup()")
    class InitOnStartupTests {

        @Test
        @DisplayName("Should download all feeds when database is empty")
        void shouldDownloadAllFeedsWhenDatabaseEmpty() throws Exception {
            // Given
            when(dataService.hasData()).thenReturn(false);

            Path zipFile = tempDir.resolve("feed.zip");
            Files.createFile(zipFile);

            GtfsStaticDownloader.DownloadResult result = new GtfsStaticDownloader.DownloadResult(true, zipFile, "M");

            when(downloader.downloadIfChanged("A")).thenReturn(Optional.of(result));
            when(downloader.downloadIfChanged("M")).thenReturn(Optional.of(result));
            when(downloader.downloadIfChanged("T")).thenReturn(Optional.of(result));

            // When
            scheduler.initOnStartup();

            // Then
            verify(downloader).downloadIfChanged("A");
            verify(downloader).downloadIfChanged("M");
            verify(downloader).downloadIfChanged("T");
            verify(loader, times(3)).loadFromZip(any(), anyString());
            verify(dataService).refreshCache();
        }

        @Test
        @DisplayName("Should only check for updates when database has data")
        void shouldCheckForUpdatesWhenDatabaseHasData() throws Exception {
            // Given
            when(dataService.hasData()).thenReturn(true);
            when(dataService.getStopCount()).thenReturn(5000L);
            when(dataService.getTripCount()).thenReturn(15000L);
            when(dataService.getRouteCount()).thenReturn(200L);

            when(downloader.downloadIfChanged(anyString())).thenReturn(Optional.empty()); // No updates

            // When
            scheduler.initOnStartup();

            // Then
            verify(dataService).hasData();
            verify(downloader).downloadIfChanged("A");
            verify(downloader).downloadIfChanged("M");
            verify(downloader).downloadIfChanged("T");
            verify(loader, never()).loadFromZip(any(), anyString());
            verify(dataService, never()).refreshCache(); // No updates, no refresh
        }

        @Test
        @DisplayName("Should do nothing when refreshOnStartup is false")
        void shouldDoNothingWhenRefreshOnStartupIsFalse() {
            // Given
            ReflectionTestUtils.setField(scheduler, "refreshOnStartup", false);

            // When
            scheduler.initOnStartup();

            // Then
            verify(dataService, never()).hasData();
            verify(downloader, never()).downloadIfChanged(anyString());
        }
    }

    @Nested
    @DisplayName("scheduledRefresh()")
    class ScheduledRefreshTests {

        @Test
        @DisplayName("Should call checkForUpdates")
        void shouldCallCheckForUpdates() {
            // Given
            when(downloader.downloadIfChanged(anyString())).thenReturn(Optional.empty());

            // When
            scheduler.scheduledRefresh();

            // Then
            verify(downloader).downloadIfChanged("A");
            verify(downloader).downloadIfChanged("M");
            verify(downloader).downloadIfChanged("T");
        }
    }

    @Nested
    @DisplayName("checkForUpdates()")
    class CheckForUpdatesTests {

        @Test
        @DisplayName("Should refresh cache when one feed changed")
        void shouldRefreshCacheWhenOneFeedChanged() throws Exception {
            // Given
            Path zipFile = tempDir.resolve("feed_M.zip");
            Files.createFile(zipFile);

            GtfsStaticDownloader.DownloadResult result = new GtfsStaticDownloader.DownloadResult(true, zipFile, "M");

            when(downloader.downloadIfChanged("A")).thenReturn(Optional.empty());
            when(downloader.downloadIfChanged("M")).thenReturn(Optional.of(result));
            when(downloader.downloadIfChanged("T")).thenReturn(Optional.empty());

            // When
            scheduler.scheduledRefresh();

            // Then
            verify(loader).loadFromZip(zipFile, "M");
            verify(downloader).cleanupTempFiles(tempDir);
            verify(dataService).refreshCache();
        }

        @Test
        @DisplayName("Should not refresh cache when no feeds changed")
        void shouldNotRefreshCacheWhenNoFeedsChanged() {
            // Given
            when(downloader.downloadIfChanged(anyString())).thenReturn(Optional.empty());

            // When
            scheduler.scheduledRefresh();

            // Then
            verify(dataService, never()).refreshCache();
        }

        @Test
        @DisplayName("Should continue with other feeds when one feed fails")
        void shouldContinueWithOtherFeedsWhenOneFails() throws Exception {
            // Given
            Path zipFileM = tempDir.resolve("feed_M.zip");
            Path zipFileT = tempDir.resolve("feed_T.zip");
            Files.createFile(zipFileM);
            Files.createFile(zipFileT);

            GtfsStaticDownloader.DownloadResult resultM = new GtfsStaticDownloader.DownloadResult(true, zipFileM, "M");
            GtfsStaticDownloader.DownloadResult resultT = new GtfsStaticDownloader.DownloadResult(true, zipFileT, "T");

            when(downloader.downloadIfChanged("A")).thenThrow(new RuntimeException("Network error"));
            when(downloader.downloadIfChanged("M")).thenReturn(Optional.of(resultM));
            when(downloader.downloadIfChanged("T")).thenReturn(Optional.of(resultT));

            // When
            scheduler.scheduledRefresh();

            // Then
            verify(loader).loadFromZip(zipFileM, "M");
            verify(loader).loadFromZip(zipFileT, "T");
            verify(dataService).refreshCache(); // Still refresh with successful feeds
        }
    }

    @Nested
    @DisplayName("refreshAllFeeds()")
    class RefreshAllFeedsTests {

        @Test
        @DisplayName("Should refresh cache when all feeds succeed")
        void shouldRefreshCacheWhenAllFeedsSucceed() throws Exception {
            // Given
            Path zipFileA = tempDir.resolve("feed_A.zip");
            Path zipFileM = tempDir.resolve("feed_M.zip");
            Path zipFileT = tempDir.resolve("feed_T.zip");
            Files.createFile(zipFileA);
            Files.createFile(zipFileM);
            Files.createFile(zipFileT);

            GtfsStaticDownloader.DownloadResult resultA = new GtfsStaticDownloader.DownloadResult(true, zipFileA, "A");
            GtfsStaticDownloader.DownloadResult resultM = new GtfsStaticDownloader.DownloadResult(true, zipFileM, "M");
            GtfsStaticDownloader.DownloadResult resultT = new GtfsStaticDownloader.DownloadResult(true, zipFileT, "T");

            when(downloader.downloadIfChanged("A")).thenReturn(Optional.of(resultA));
            when(downloader.downloadIfChanged("M")).thenReturn(Optional.of(resultM));
            when(downloader.downloadIfChanged("T")).thenReturn(Optional.of(resultT));

            // When
            scheduler.refreshAllFeeds();

            // Then
            verify(loader).loadFromZip(zipFileA, "A");
            verify(loader).loadFromZip(zipFileM, "M");
            verify(loader).loadFromZip(zipFileT, "T");
            verify(dataService).refreshCache();
        }

        @Test
        @DisplayName("Should warn when all feeds fail and database is empty")
        void shouldWarnWhenAllFeedsFailAndDatabaseEmpty() {
            // Given
            when(dataService.hasData()).thenReturn(false);
            when(downloader.downloadIfChanged(anyString())).thenReturn(Optional.empty());

            // When
            scheduler.refreshAllFeeds();

            // Then
            verify(dataService).hasData();
            verify(dataService, never()).refreshCache();
        }

        @Test
        @DisplayName("Should not warn when all feeds fail but database has data")
        void shouldNotWarnWhenAllFeedsFailButDatabaseHasData() {
            // Given
            when(dataService.hasData()).thenReturn(true);
            when(downloader.downloadIfChanged(anyString())).thenReturn(Optional.empty());

            // When
            scheduler.refreshAllFeeds();

            // Then
            verify(dataService).hasData();
            verify(dataService, never()).refreshCache();
        }
    }

    @Nested
    @DisplayName("loadFeedData()")
    class LoadFeedDataTests {

        @Test
        @DisplayName("Should cleanup temp files after loading")
        void shouldCleanupTempFilesAfterLoading() throws Exception {
            // Given
            Path zipFile = tempDir.resolve("feed.zip");
            Files.createFile(zipFile);

            GtfsStaticDownloader.DownloadResult result = new GtfsStaticDownloader.DownloadResult(true, zipFile, "M");

            when(dataService.hasData()).thenReturn(false);
            when(downloader.downloadIfChanged("A")).thenReturn(Optional.of(result));
            when(downloader.downloadIfChanged("M")).thenReturn(Optional.empty());
            when(downloader.downloadIfChanged("T")).thenReturn(Optional.empty());

            // When
            scheduler.initOnStartup();

            // Then
            InOrder inOrder = inOrder(loader, downloader);
            inOrder.verify(loader).loadFromZip(zipFile, "M");
            inOrder.verify(downloader).cleanupTempFiles(tempDir);
        }

        @Test
        @DisplayName("Should cleanup temp files even when loading fails")
        void shouldCleanupTempFilesEvenWhenLoadingFails() throws Exception {
            // Given
            Path zipFile = tempDir.resolve("feed.zip");
            Files.createFile(zipFile);

            GtfsStaticDownloader.DownloadResult result = new GtfsStaticDownloader.DownloadResult(true, zipFile, "M");

            when(dataService.hasData()).thenReturn(false);
            when(downloader.downloadIfChanged("A")).thenReturn(Optional.of(result));
            when(downloader.downloadIfChanged("M")).thenReturn(Optional.empty());
            when(downloader.downloadIfChanged("T")).thenReturn(Optional.empty());

            doThrow(new RuntimeException("Failed to parse ZIP")).when(loader).loadFromZip(zipFile, "M");

            // When
            scheduler.initOnStartup();

            // Then
            verify(downloader).cleanupTempFiles(tempDir); // Still cleanup despite exception
        }
    }
}
