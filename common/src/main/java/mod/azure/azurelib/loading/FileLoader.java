package mod.azure.azurelib.loading;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.Charset;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.AzureLibException;
import mod.azure.azurelib.animation.primitive.AzBakedAnimation;
import mod.azure.azurelib.animation.primitive.AzBakedAnimations;
import mod.azure.azurelib.loading.json.raw.Model;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.util.JsonUtil;

/**
 * Extracts raw information from given files, and other similar functions
 */
public final class FileLoader {

    private FileLoader() {
        throw new UnsupportedOperationException();
    }

    /**
     * Load up and deserialize an animation JSON file to its respective {@link AzBakedAnimation} components
     *
     * @param location The resource path of the animation file
     * @param manager  The Minecraft {@code ResourceManager} responsible for maintaining in-memory resource access
     */
    public static AzBakedAnimations loadAzAnimationsFile(ResourceLocation location, ResourceManager manager) {
        try {
            return JsonUtil.GEO_GSON.fromJson(loadFile(location, manager), AzBakedAnimations.class);
        } catch (Exception e) {
            logError(location);
            return null;
        }
    }

    /**
     * Load up and deserialize a geo model JSON file to its respective {@link AzBakedModel} format
     *
     * @param location The resource path of the model file
     * @param manager  The Minecraft {@code ResourceManager} responsible for maintaining in-memory resource access
     */
    public static Model loadModelFile(ResourceLocation location, ResourceManager manager) {
        try {
            return JsonUtil.GEO_GSON.fromJson(loadFile(location, manager), Model.class);
        } catch (Exception e) {
            logError(location);
            return null;
        }
    }

    /**
     * Loads and parses a JSON file from the specified resource location. This method retrieves the file using the
     * provided resource manager and deserializes its contents into a {@link JsonObject}. If an error occurs during file
     * loading or parsing, logs the error and returns {@code null}.
     *
     * @param location The resource location of the file to be loaded.
     * @param manager  The resource manager responsible for accessing the resource.
     * @return The parsed JSON file as a {@link JsonObject}, or {@code null} if an error occurs.
     */
    public static JsonObject loadFile(ResourceLocation location, ResourceManager manager) {
        try {
            return GsonHelper.fromJson(JsonUtil.GEO_GSON, getFileContents(location, manager), JsonObject.class);
        } catch (Exception e) {
            logError(location);
            return null;
        }
    }

    /**
     * Read a text-based file into memory in the form of a single string
     *
     * @param location The resource path of the file
     * @param manager  The Minecraft {@code ResourceManager} responsible for maintaining in-memory resource access
     */
    public static String getFileContents(ResourceLocation location, ResourceManager manager) {
        try (InputStream inputStream = manager.getResourceOrThrow(location).open()) {
            return IOUtils.toString(inputStream, Charset.defaultCharset());
        } catch (Exception e) {
            AzureLib.LOGGER.error("Couldn't load {}", location, e);

            throw new AzureLibException(location.toString());
        }
    }

    /**
     * Logs error messages with optional arguments using the AzureLib logger at the warning level. This method provides
     * a way to indicate warnings or errors during file loading or parsing.
     *
     * @param args Optional arguments to be formatted into the message placeholders.
     */
    private static void logError(Object args) {
        AzureLib.LOGGER.warn("Error parsing JSON from {}: skipping", args);
    }
}
