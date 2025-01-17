/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
package mod.azure.azurelib.common.internal.common.config.io;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.common.AzureLibException;
import mod.azure.azurelib.common.internal.common.config.ConfigHolder;

public final class FileWatchManager {

    public static final Marker MARKER = MarkerManager.getMarker("FileWatching");

    private final Map<String, ConfigHolder<?>> configPaths = new HashMap<>();

    private final List<WatchKey> watchKeys = new ArrayList<>();

    @Nullable
    private final WatchService service;

    private final ScheduledExecutorService executorService;

    private final Set<String> processCache = new HashSet<>();

    public FileWatchManager() {
        WatchService watchService = null;
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            AzureLib.LOGGER.error(
                MARKER,
                "Failed to initialize file watch service due to error, configs won't be automatically refreshed",
                e
            );
        } finally {
            this.service = watchService;
            this.executorService = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r);
                t.setName("Auto-Sync thread");
                t.setDaemon(true);
                return t;
            });
        }
    }

    public void stopService() {
        try {
            this.executorService.shutdown();
            this.service.close();
        } catch (IOException e) {
            throw new AzureLibException("Error while stopping FileWatch service", e);
        }
    }

    public void startService() {
        AzureLib.LOGGER.debug(MARKER, "Starting file watching service");
        if (this.service == null) {
            AzureLib.LOGGER.error(MARKER, "Unable to start file watch service");
            return;
        }
        Path configDir = Paths.get("./config");
        try {
            File configDirFile = configDir.toFile();
            if (!configDirFile.exists()) {
                configDirFile.mkdir();
            }
            Files.walkFileTree(configDir, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    WatchKey key = dir.register(FileWatchManager.this.service, StandardWatchEventKinds.ENTRY_MODIFY);
                    FileWatchManager.this.watchKeys.add(key);
                    return FileVisitResult.CONTINUE;
                }
            });
            this.executorService.scheduleAtFixedRate(() -> {
                this.processCache.clear();
                this.watchKeys.forEach(key -> {
                    List<WatchEvent<?>> eventList = key.pollEvents();
                    eventList.forEach(event -> {
                        Path path = (Path) event.context();
                        String strPath = path.toString().replaceAll("\\..+$", "");
                        if (this.processCache.contains(strPath))
                            return; // Ignore duplicate reads from subdirectories
                        ConfigHolder<?> holder = this.configPaths.get(strPath);
                        if (holder != null) {
                            ConfigIO.reloadClientValues(holder);
                            holder.dispatchFileRefreshEvent();
                            this.processCache.add(strPath);
                        }
                    });
                });
            }, 0L, 1000L, TimeUnit.MILLISECONDS);
        } catch (IOException e) {
            AzureLib.LOGGER.error(
                MARKER,
                "Unable to create watch key for config directory, disabling auto-sync function",
                e
            );
        }
    }

    public void addTrackedConfig(ConfigHolder<?> holder) {
        Path path = Paths.get(holder.getFilename());
        File file = path.toFile();
        this.configPaths.put(file.getName(), holder);
        AzureLib.LOGGER.info(MARKER, "Registered {} config for auto-sync function", holder.getConfigId());
    }
}
