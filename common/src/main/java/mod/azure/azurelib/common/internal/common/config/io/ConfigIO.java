package mod.azure.azurelib.common.internal.common.config.io;

import mod.azure.azurelib.common.internal.common.config.ConfigHolder;
import mod.azure.azurelib.common.internal.common.config.format.IConfigFormat;
import mod.azure.azurelib.common.internal.common.config.format.IConfigFormatHandler;
import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.common.AzureLibException;
import mod.azure.azurelib.common.internal.common.config.exception.ConfigReadException;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.File;
import java.io.IOException;

public final class ConfigIO {

    public static final Marker MARKER = MarkerManager.getMarker("IO");
    public static final FileWatchManager FILE_WATCH_MANAGER = new FileWatchManager();

    private ConfigIO() {}
    public static void processConfig(ConfigHolder<?> holder) {
        AzureLib.LOGGER.debug(MARKER, "Starting processing of config {}", holder.getConfigId());
        processSafely(holder, () -> {
            File file = getConfigFile(holder);
            if (file.exists()) {
                try {
                    readConfig(holder);
                } catch (IOException e) {
                    AzureLib.LOGGER.error(MARKER, "Config read failed for config ID {}, will create default config file", holder.getConfigId());
                }
            }
            try {
                writeConfig(holder);
            } catch (IOException e) {
                AzureLib.LOGGER.fatal(MARKER, "Couldn't write config {}, aborting mod startup", holder.getConfigId());
                throw new AzureLibException("Config write failed", e);
            }
        });
        AzureLib.LOGGER.debug(MARKER, "Processing of config {} has finished", holder.getConfigId());
    }

    public static void reloadClientValues(ConfigHolder<?> configHolder) {
        processSafely(configHolder, () -> {
            try {
                readConfig(configHolder);
            } catch (IOException e) {
                AzureLib.LOGGER.error(MARKER, "Failed to read config file {}", configHolder.getConfigId());
            }
        });
    }

    public static void saveClientValues(ConfigHolder<?> configHolder) {
        processSafely(configHolder, () -> {
            try {
                writeConfig(configHolder);
            } catch (IOException e) {
                AzureLib.LOGGER.error(MARKER, "Failed to write config file {}", configHolder.getConfigId());
            }
        });
    }

    private static void processSafely(ConfigHolder<?> holder, Runnable action) {
        try {
            synchronized (holder.getLock()) {
                action.run();
            }
        } catch (Exception e) {
            AzureLib.LOGGER.fatal(MARKER, "Error loading config {} due to critical error '{}'. Report this issue to this config's owner!", holder.getConfigId(), e.getMessage());
            throw new ReportedException(CrashReport.forThrowable(e, "Config " + holder.getConfigId() + " failed. Report issue to config owner"));
        }
    }

    private static void readConfig(ConfigHolder<?> holder) throws IOException {
        AzureLib.LOGGER.debug(MARKER, "Reading config {}", holder.getConfigId());
        IConfigFormat format = holder.getFormat().createFormat();
        File file = getConfigFile(holder);
        if (!file.exists())
            return;
        try {
            format.readFile(file);
            holder.values().forEach(value -> value.deserializeValue(format));
        } catch (ConfigReadException e) {
            AzureLib.LOGGER.error(MARKER, "Config read failed, using default values", e);
        }
    }

    public static void writeConfig(ConfigHolder<?> holder) throws IOException {
        AzureLib.LOGGER.debug(MARKER, "Writing config {}", holder.getConfigId());
        File file = getConfigFile(holder);
        File dir = file.getParentFile();
        if (dir.mkdirs()) {
            AzureLib.LOGGER.debug(MARKER, "Created file directories at {}", dir.getAbsolutePath());
        }
        if (!file.exists() && !file.createNewFile()) {
            throw new AzureLibException("Config file create failed");
        }
        IConfigFormatHandler handler = holder.getFormat();
        IConfigFormat format = handler.createFormat();
        holder.values().forEach(value -> value.serializeValue(format));
        format.writeFile(file);
    }

    public static File getConfigFile(ConfigHolder<?> holder) {
        IConfigFormatHandler handler = holder.getFormat();
        String filename = holder.getFilename();
        return new File("./config/" + filename + "." + handler.fileExt());
    }
}
