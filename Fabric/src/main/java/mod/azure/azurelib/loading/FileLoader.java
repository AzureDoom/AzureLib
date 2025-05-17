/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.loading;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.Charset;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.AzureLibException;
import mod.azure.azurelib.animation.primitive.AzBakedAnimation;
import mod.azure.azurelib.animation.primitive.AzBakedAnimations;
import mod.azure.azurelib.loading.json.raw.Model;
import mod.azure.azurelib.util.JsonUtil;

/**
 * Extracts raw information from given files, and other similar functions
 */
public final class FileLoader {

    /**
     * Load up and deserialize an animation JSON file to its respective {@link AzBakedAnimation} components
     *
     * @param location The resource path of the animation file
     * @param manager  The Minecraft {@code ResourceManager} responsible for maintaining in-memory resource access
     */
    public static AzBakedAnimations loadAzAnimationsFile(ResourceLocation location, ResourceManager manager) {
        return JsonUtil.GEO_GSON.fromJson(loadFile(location, manager), AzBakedAnimations.class);
    }

    public static Model loadModelFile(ResourceLocation location, ResourceManager manager) {
        return JsonUtil.GEO_GSON.fromJson(loadFile(location, manager), Model.class);
    }

    /**
     * Load a given json file into memory
     *
     * @param location The resource path of the JSON file
     * @param manager  The Minecraft {@code ResourceManager} responsible for maintaining in-memory resource access
     */
    public static JsonObject loadFile(ResourceLocation location, ResourceManager manager) {
        return GsonHelper.fromJson(JsonUtil.GEO_GSON, getFileContents(location, manager), JsonObject.class);
    }

    /**
     * Read a text-based file into memory in the form of a single string
     *
     * @param location The resource path of the file
     * @param manager  The Minecraft {@code ResourceManager} responsible for maintaining in-memory resource access
     */
    public static String getFileContents(ResourceLocation location, ResourceManager manager) {
        try (InputStream inputStream = manager.getResource(location).getInputStream()) {
            return IOUtils.toString(inputStream, Charset.defaultCharset());
        } catch (Exception e) {
            AzureLib.LOGGER.error("Couldn't load {}", location, e);

            throw new AzureLibException(new FileNotFoundException(location.toString()));
        }
    }
}
