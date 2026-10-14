package mod.azure.azurelib.resource;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.util.ResourceLocation;

/**
 * Beta 1.7.3 has no resource manager (only the texture pack, which is limited to vanilla texture paths), so AzureLib
 * reads its JSON and texture assets straight from the root paths of every loaded Fabric mod, falling back to the game
 * class loader. This is the Beta stand-in for 1.7.10's {@code IResourceManager}.
 */
public final class AzResourceManager {

    private static final AzResourceManager INSTANCE = new AzResourceManager();

    private volatile List<Path> roots;

    private AzResourceManager() {}

    public static AzResourceManager getInstance() {
        return INSTANCE;
    }

    /** Every mod root (jar file system or dev output directory) that may contain an {@code assets} folder. */
    public List<Path> roots() {
        List<Path> cached = this.roots;
        if (cached == null) {
            List<Path> found = new ArrayList<>();
            for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
                found.addAll(mod.getRootPaths());
            }
            cached = Collections.unmodifiableList(found);
            this.roots = cached;
        }
        return cached;
    }

    /** Drops cached mod roots; they are rebuilt on next access. */
    public void invalidate() {
        this.roots = null;
    }

    public boolean hasResource(ResourceLocation location) {
        String relative = location.toClasspathPath().substring(1);
        for (Path root : roots()) {
            if (Files.isRegularFile(root.resolve(relative)))
                return true;
        }
        return AzResourceManager.class.getResource(location.toClasspathPath()) != null;
    }

    public AzResource getResource(ResourceLocation location) throws IOException {
        if (!hasResource(location)) {
            throw new FileNotFoundException(location.toString());
        }
        return new AzResource() {

            @Override
            public ResourceLocation getResourceLocation() {
                return location;
            }

            private final ResourceLocation metaLocation = new ResourceLocation(
                location.getResourceDomain(),
                location.getResourcePath() + ".mcmeta"
            );

            private JsonObject metadata;

            private boolean metadataRead;

            @Override
            public InputStream getInputStream() throws IOException {
                return open(location);
            }

            @Override
            public boolean hasMetadata() {
                return readMetadata() != null;
            }

            @Override
            public Object getMetadata(String section) {
                JsonObject json = readMetadata();
                if (json == null || !json.has(section) || !json.get(section).isJsonObject())
                    return null;
                return AzMetadataParser.parse(section, json.getAsJsonObject(section));
            }

            private JsonObject readMetadata() {
                if (!this.metadataRead) {
                    this.metadataRead = true;
                    if (hasResource(this.metaLocation)) {
                        try (Reader reader = new InputStreamReader(open(this.metaLocation), StandardCharsets.UTF_8)) {
                            this.metadata = new JsonParser().parse(reader).getAsJsonObject();
                        } catch (Exception e) {
                            AzureLib.LOGGER.warn("Invalid metadata {}: {}", this.metaLocation, e.toString());
                        }
                    }
                }
                return this.metadata;
            }
        };
    }

    public InputStream open(ResourceLocation location) throws IOException {
        String classpath = location.toClasspathPath();
        String relative = classpath.substring(1);
        for (Path root : roots()) {
            Path candidate = root.resolve(relative);
            if (Files.isRegularFile(candidate))
                return Files.newInputStream(candidate);
        }
        InputStream stream = AzResourceManager.class.getResourceAsStream(classpath);
        if (stream == null) {
            throw new FileNotFoundException(location.toString());
        }
        return stream;
    }
}
