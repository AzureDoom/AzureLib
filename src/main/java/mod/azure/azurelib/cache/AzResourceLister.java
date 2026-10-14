package mod.azure.azurelib.cache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.resource.AzResourceManager;
import mod.azure.azurelib.util.ResourceLocation;

/**
 * Lists AzureLib assets ({@code assets/<namespace>/<folder>/**<suffix>}) across every loaded Fabric mod.
 * <p>
 * Beta 1.7.3 has no resource manager that can enumerate files, so this walks each mod's root paths directly. Fabric
 * exposes jar mods as zip file systems, so the same {@link Files#walk} code handles both jars and development
 * directories.
 */
public final class AzResourceLister {

    private AzResourceLister() {
        throw new UnsupportedOperationException();
    }

    public static Set<ResourceLocation> listResources(AzResourceManager resourceManager, String folder, String suffix) {
        Set<ResourceLocation> found = new LinkedHashSet<>();
        for (Path root : resourceManager.roots()) {
            try {
                scanRoot(root, folder, suffix, found);
            } catch (IOException e) {
                AzureLib.LOGGER.warn("Failed to scan {} for {}", root, folder, e);
            }
        }
        return found;
    }

    private static void scanRoot(
        Path root,
        String folder,
        String suffix,
        Set<ResourceLocation> found
    ) throws IOException {
        Path assets = root.resolve("assets");
        if (!Files.isDirectory(assets))
            return;
        try (Stream<Path> namespaces = Files.list(assets)) {
            namespaces.filter(Files::isDirectory).forEach(namespaceDir -> {
                String namespace = trimSlash(namespaceDir.getFileName().toString());
                Path typeDir = namespaceDir.resolve(folder);
                if (!Files.isDirectory(typeDir))
                    return;
                try (Stream<Path> files = Files.walk(typeDir)) {
                    files.filter(Files::isRegularFile).forEach(file -> {
                        // Path#toString uses the file system's separator; normalise for zip and Windows paths.
                        String relative = namespaceDir.relativize(file).toString().replace('\\', '/');
                        addIfValid(namespace, relative, suffix, found);
                    });
                } catch (IOException e) {
                    AzureLib.LOGGER.warn("Failed to scan {}", typeDir, e);
                }
            });
        }
    }

    private static String trimSlash(String name) {
        return name.endsWith("/") ? name.substring(0, name.length() - 1) : name;
    }

    private static void addIfValid(String namespace, String path, String suffix, Set<ResourceLocation> found) {
        if (!path.endsWith(suffix))
            return;
        if (!namespace.equals(namespace.toLowerCase(Locale.ROOT)) || !path.equals(path.toLowerCase(Locale.ROOT))) {
            AzureLib.LOGGER.warn("Skipping non lower-case resource {}:{}", namespace, path);
            return;
        }
        found.add(new ResourceLocation(namespace, path));
    }
}
