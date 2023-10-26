package mod.azure.azurelib.loading;

import com.google.gson.JsonObject;

import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.loading.json.raw.Model;
import mod.azure.azurelib.loading.object.BakedAnimations;
import mod.azure.azurelib.util.JsonUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Extracts raw information from given files, and other similar functions
 */
public final class FileLoader {
	/**
	 * Load up and deserialize an animation json file to its respective {@link mod.azure.azurelib.core.animation.Animation} components
	 * @param location The resource path of the animations file
	 * @param manager The Minecraft {@code ResourceManager} responsible for maintaining in-memory resource access
	 */
	public static BakedAnimations loadAnimationsFile(ResourceLocation location, ResourceManager manager) {
		return JsonUtil.GEO_GSON.fromJson(loadFile(location, manager), BakedAnimations.class);
	}

	/**
	 * Load up and deserialize a geo model json file to its respective {@link BakedGeoModel} format
	 * @param location The resource path of the model file
	 * @param manager The Minecraft {@code ResourceManager} responsible for maintaining in-memory resource access
	 */
	public static Model loadModelFile(ResourceLocation location, ResourceManager manager) {
		return JsonUtil.GEO_GSON.fromJson(loadFile(location, manager), Model.class);
	}

	/**
	 * Load a given json file into memory
	 * @param location The resource path of the json file
	 * @param manager The Minecraft {@code ResourceManager} responsible for maintaining in-memory resource access
	 */
	public static JsonObject loadFile(ResourceLocation location, ResourceManager manager) {
		return GsonHelper.fromJson(JsonUtil.GEO_GSON, getFileContents(location, manager), JsonObject.class);
	}

	/**
	 * Read a text-based file into memory in the form of a single string
	 * @param location The resource path of the file
	 * @param manager The Minecraft {@code ResourceManager} responsible for maintaining in-memory resource access
	 */
	public static String getFileContents(ResourceLocation location, ResourceManager manager) {
		try (InputStream inputStream = manager.getResourceOrThrow(location).open()) {
			return IOUtils.toString(inputStream, Charset.defaultCharset());
		}
		catch (Exception e) {
			AzureLib.LOGGER.error("Couldn't load " + location, e);

			throw new RuntimeException(new FileNotFoundException(location.toString()));
		}
	}
}
