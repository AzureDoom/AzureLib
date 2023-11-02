package mod.azure.azurelib.common.internal.common.config.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import mod.azure.azurelib.common.internal.common.config.ConfigHolder;
import mod.azure.azurelib.common.internal.common.AzureLib;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.Nullable;

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
            AzureLib.LOGGER.error(MARKER, "Failed to initialize file watch service due to error, configs won't be automatically refreshed", e);
        } finally {
            this.service = watchService;
            this.executorService = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r);
                t.setName("Auto-Sync thread");
                return t;
            });
        }
    }
    
    public void stopService() {
    	this.executorService.shutdown();
    }

    public void startService() {
        AzureLib.LOGGER.debug(MARKER, "Starting file watching service");
        if (this.service == null) {
            AzureLib.LOGGER.error(MARKER, "Unable to start file watch service");
            return;
        }
        Path configDir = Paths.get("./config");
        try {
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
            AzureLib.LOGGER.error(MARKER, "Unable to create watch key for config directory, disabling auto-sync function", e);
        }
    }

    public void addTrackedConfig(ConfigHolder<?> holder) {
        Path path = Paths.get(holder.getFilename());
        File file = path.toFile();
        this.configPaths.put(file.getName(), holder);
        AzureLib.LOGGER.info(MARKER, "Registered {} config for auto-sync function", holder.getConfigId());
    }
}
