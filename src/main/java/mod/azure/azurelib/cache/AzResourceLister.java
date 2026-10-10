package mod.azure.azurelib.cache;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import net.minecraft.client.resources.AbstractResourcePack;
import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import mod.azure.azurelib.AzureLib;

/**
 * Finds resource locations by folder - the 1.7.10 stand-in for 1.18's {@code ResourceManager#listResources}.
 * <p>
 * The loaded resource packs are taken from the resource manager's per-domain {@link FallbackResourceManager}s. Every
 * pack backed by a directory or a zip/jar file (mod jars, dev-environment mod folders, user resource packs and server
 * packs) is scanned for {@code assets/<namespace>/<folder>/**<suffix>}. Packs that are neither (e.g. the vanilla
 * default pack) are skipped, which is fine because vanilla ships no AzureLib assets.
 */
public final class AzResourceLister {

    private static final String[] DOMAIN_MANAGERS = { "field_110548_a", "domainResourceManagers" };

    private static final String[] RESOURCE_PACKS = { "field_110540_a", "resourcePacks" };

    private static final String[] PACK_FILE = { "field_110597_b", "resourcePackFile" };

    private AzResourceLister() {
        throw new UnsupportedOperationException();
    }

    public static Set<ResourceLocation> listResources(IResourceManager resourceManager, String folder, String suffix) {
        Set<ResourceLocation> found = new LinkedHashSet<>();

        for (IResourcePack pack : collectPacks(resourceManager)) {
            if (!(pack instanceof AbstractResourcePack))
                continue;

            File file;
            try {
                file = ObfuscationReflectionHelper.getPrivateValue(
                    AbstractResourcePack.class,
                    (AbstractResourcePack) pack,
                    PACK_FILE
                );
            } catch (Exception e) {
                AzureLib.LOGGER.warn("Could not read the source of resource pack {}", pack.getPackName(), e);
                continue;
            }

            if (file == null)
                continue;

            try {
                if (file.isDirectory()) {
                    scanDirectory(file.toPath(), folder, suffix, found);
                } else if (file.isFile()) {
                    scanZip(file, folder, suffix, found);
                }
            } catch (IOException e) {
                AzureLib.LOGGER.warn("Failed to scan resource pack {} for {}", pack.getPackName(), folder, e);
            }
        }

        return found;
    }

    private static Set<IResourcePack> collectPacks(IResourceManager resourceManager) {
        Set<IResourcePack> packs = Collections.newSetFromMap(new IdentityHashMap<IResourcePack, Boolean>());
        if (!(resourceManager instanceof SimpleReloadableResourceManager)) {
            AzureLib.LOGGER.warn(
                "Unexpected resource manager type {}, AzureLib assets will not be found",
                resourceManager.getClass().getName()
            );
            return packs;
        }

        try {
            Map<String, FallbackResourceManager> domains = ObfuscationReflectionHelper.getPrivateValue(
                SimpleReloadableResourceManager.class,
                (SimpleReloadableResourceManager) resourceManager,
                DOMAIN_MANAGERS
            );
            for (FallbackResourceManager manager : domains.values()) {
                List<IResourcePack> domainPacks = ObfuscationReflectionHelper.getPrivateValue(
                    FallbackResourceManager.class,
                    manager,
                    RESOURCE_PACKS
                );
                packs.addAll(domainPacks);
            }
        } catch (Exception e) {
            AzureLib.LOGGER.error("Failed to enumerate resource packs, AzureLib assets will not be found", e);
        }

        return packs;
    }

    private static void scanDirectory(
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
                String namespace = namespaceDir.getFileName().toString();
                Path typeDir = namespaceDir.resolve(folder);
                if (!Files.isDirectory(typeDir))
                    return;

                try (Stream<Path> files = Files.walk(typeDir)) {
                    files.filter(Files::isRegularFile).forEach(file -> {
                        String relative = namespaceDir.relativize(file).toString().replace(File.separatorChar, '/');
                        addIfValid(namespace, relative, suffix, found);
                    });
                } catch (IOException e) {
                    AzureLib.LOGGER.warn("Failed to scan {}", typeDir, e);
                }
            });
        }
    }

    private static void scanZip(
        File file,
        String folder,
        String suffix,
        Set<ResourceLocation> found
    ) throws IOException {
        try (ZipFile zip = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory())
                    continue;

                String name = entry.getName();
                if (!name.startsWith("assets/"))
                    continue;

                int namespaceEnd = name.indexOf('/', 7);
                if (namespaceEnd < 0)
                    continue;

                String namespace = name.substring(7, namespaceEnd);
                String relative = name.substring(namespaceEnd + 1);
                if (relative.startsWith(folder + "/"))
                    addIfValid(namespace, relative, suffix, found);
            }
        }
    }

    private static void addIfValid(String namespace, String path, String suffix, Set<ResourceLocation> found) {
        if (!path.endsWith(suffix))
            return;
        // ResourceLocations are lower-case on 1.7.10; mixed-case files could never be loaded anyway.
        if (!namespace.equals(namespace.toLowerCase(Locale.ROOT)) || !path.equals(path.toLowerCase(Locale.ROOT))) {
            AzureLib.LOGGER.warn("Skipping non lower-case resource {}:{}", namespace, path);
            return;
        }
        found.add(new ResourceLocation(namespace, path));
    }
}
