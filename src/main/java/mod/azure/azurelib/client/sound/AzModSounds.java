package mod.azure.azurelib.client.sound;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.sound.SoundEngine;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.platform.Services;

public final class AzModSounds {

    private enum Kind {

        SOUND("sounds"),
        MUSIC("music"),
        RECORD("records");

        final String folder;

        Kind(String folder) {
            this.folder = folder;
        }
    }

    private AzModSounds() {
        throw new UnsupportedOperationException();
    }

    public static void registerAll(SoundEngine engine) {
        Path cache = Services.PLATFORM.getGameDir().resolve("azurelib").resolve("sounds");
        int count = 0;
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            for (Path root : mod.getRootPaths()) {
                Path assets = root.resolve("assets");
                if (!Files.isDirectory(assets)) {
                    continue;
                }
                try (DirectoryStream<Path> namespaces = Files.newDirectoryStream(assets)) {
                    for (Path namespace : namespaces) {
                        for (Kind kind : Kind.values()) {
                            count += registerFolder(engine, cache, namespace, kind, mod);
                        }
                    }
                } catch (IOException e) {
                    AzureLib.LOGGER.error("Couldn't list sounds of mod {}", mod.getMetadata().getId(), e);
                }
            }
        }
        if (count > 0) {
            AzureLib.LOGGER.info("Registered {} mod sound file(s)", count);
        }
    }

    private static int registerFolder(SoundEngine engine, Path cache, Path namespaceDir, Kind kind, ModContainer mod) {
        Path folder = namespaceDir.resolve(kind.folder);
        if (!Files.isDirectory(folder)) {
            return 0;
        }
        String namespace = fileName(namespaceDir);
        List<Path> files = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(folder)) {
            walk.filter(Files::isRegularFile).filter(AzModSounds::isSoundFile).forEach(files::add);
        } catch (IOException e) {
            AzureLib.LOGGER.error("Couldn't list {} of mod {}", folder, mod.getMetadata().getId(), e);
            return 0;
        }

        int count = 0;
        for (Path file : files) {
            String relative = folder.relativize(file).toString().replace('\\', '/');
            // Beta derives the key from this name: cut at the first '.', '/' -> '.'.
            String name = namespace + "/" + relative;
            if (relative.indexOf('.') != relative.lastIndexOf('.') || namespace.indexOf('.') >= 0) {
                AzureLib.LOGGER.warn(
                    "Skipping sound {} of mod {}: Beta sound names can't contain '.' before the extension",
                    name,
                    mod.getMetadata().getId()
                );
                continue;
            }
            try {
                Path target = cache.resolve(kind.folder).resolve(namespace).resolve(relative);
                Files.createDirectories(target.getParent());
                try (InputStream in = Files.newInputStream(file)) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                }
                register(engine, kind, name, target);
                count++;
            } catch (IOException | RuntimeException e) {
                AzureLib.LOGGER.error("Couldn't register sound {} of mod {}", name, mod.getMetadata().getId(), e);
            }
        }
        return count;
    }

    private static void register(SoundEngine engine, Kind kind, String name, Path file) {
        switch (kind) {
            case SOUND:
                engine.loadSound(name, file.toFile());
                break;
            case MUSIC:
                engine.loadMusic(name, file.toFile());
                break;
            case RECORD:
                engine.loadRecord(name, file.toFile());
                break;
            default:
                break;
        }
    }

    private static boolean isSoundFile(Path file) {
        String name = fileName(file).toLowerCase(Locale.ROOT);
        return name.endsWith(".ogg") || name.endsWith(".wav") || name.endsWith(".mus");
    }

    private static String fileName(Path path) {
        Path name = path.getFileName();
        String text = name == null ? path.toString() : name.toString();
        // Zip file system directory names can end with '/'.
        return text.endsWith("/") ? text.substring(0, text.length() - 1) : text;
    }
}
