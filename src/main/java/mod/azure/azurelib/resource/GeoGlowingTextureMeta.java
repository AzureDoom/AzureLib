package mod.azure.azurelib.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import mod.azure.azurelib.util.GsonHelper;

/**
 * Metadata class that stores the data for AzureLib's {@link mod.azure.azurelib.render.layer.AzAutoGlowingLayer emissive
 * texture feature} for a given texture.
 * <p>
 * On 1.7.10 custom {@code .mcmeta} sections can't be registered with the vanilla metadata serializer, so the
 * {@code glowsections} section is read straight from the texture's {@code .mcmeta} file by
 * {@link #fromMcmeta(IResourceManager, ResourceLocation)}. Images are handled as ARGB {@link BufferedImage}s.
 */
public class GeoGlowingTextureMeta {

    public static final String SECTION_NAME = "glowsections";

    private final List<Pixel> pixels;

    public GeoGlowingTextureMeta(List<Pixel> pixels) {
        this.pixels = pixels;
    }

    /**
     * Reads the {@code glowsections} section from {@code <texture>.mcmeta}, if the file and section exist.
     */
    @Nullable
    public static GeoGlowingTextureMeta fromMcmeta(IResourceManager resourceManager, ResourceLocation texture) {
        ResourceLocation mcmeta = new ResourceLocation(
            texture.getResourceDomain(),
            texture.getResourcePath() + ".mcmeta"
        );

        try (InputStream stream = resourceManager.getResource(mcmeta).getInputStream()) {
            JsonElement root = new JsonParser().parse(IOUtils.toString(stream, StandardCharsets.UTF_8));

            if (!root.isJsonObject() || !root.getAsJsonObject().has(SECTION_NAME))
                return null;

            return fromJson(GsonHelper.convertToJsonObject(root.getAsJsonObject().get(SECTION_NAME), SECTION_NAME));
        } catch (JsonParseException e) {
            throw e;
        } catch (Exception e) {
            return null;
        }
    }

    public static GeoGlowingTextureMeta fromJson(JsonObject json) {
        List<Pixel> pixels = fromSections(GsonHelper.getAsJsonArray(json, "sections", null));

        if (pixels.isEmpty())
            throw new JsonParseException("Empty glowlayer sections file. Must have at least one glow section!");

        return new GeoGlowingTextureMeta(pixels);
    }

    private static List<Pixel> fromSections(@Nullable JsonArray sectionsArray) {
        if (sectionsArray == null)
            return Collections.emptyList();

        List<Pixel> pixels = new ArrayList<>();

        for (JsonElement element : sectionsArray) {
            if (!(element instanceof JsonObject))
                throw new JsonParseException(
                    "Invalid glowsections json format, expected a JsonObject, found: " + element.getClass()
                );

            JsonObject obj = (JsonObject) element;
            int x1 = GsonHelper.getAsInt(obj, "x1", GsonHelper.getAsInt(obj, "x", 0));
            int y1 = GsonHelper.getAsInt(obj, "y1", GsonHelper.getAsInt(obj, "y", 0));
            int x2 = GsonHelper.getAsInt(obj, "x2", GsonHelper.getAsInt(obj, "w", 0) + x1);
            int y2 = GsonHelper.getAsInt(obj, "y2", GsonHelper.getAsInt(obj, "h", 0) + y1);
            int alpha = GsonHelper.getAsInt(obj, "alpha", GsonHelper.getAsInt(obj, "a", 0));

            if (x1 + y1 + x2 + y2 == 0)
                throw new IllegalArgumentException(
                    "Invalid glowsections section object, section must be at least one pixel in size"
                );

            for (int x = x1; x <= x2; x++) {
                for (int y = y1; y <= y2; y++) {
                    pixels.add(new Pixel(x, y, alpha));
                }
            }
        }

        return pixels;
    }

    /**
     * Generate a {@link GeoGlowingTextureMeta} from pre-made glowmask image.
     */
    public static GeoGlowingTextureMeta fromExistingImage(BufferedImage glowLayer) {
        List<Pixel> pixels = new ArrayList<>();

        for (int x = 0; x < glowLayer.getWidth(); x++) {
            for (int y = 0; y < glowLayer.getHeight(); y++) {
                int color = glowLayer.getRGB(x, y);

                if (color != 0)
                    pixels.add(new Pixel(x, y, color >>> 24));
            }
        }

        if (pixels.isEmpty())
            throw new IllegalStateException("Invalid glow layer texture provided, must have at least one pixel!");

        return new GeoGlowingTextureMeta(pixels);
    }

    /**
     * Create a new mask image based on the pre-determined pixel data, moving every masked pixel out of the original
     * image and into the new one.
     */
    public void createImageMask(BufferedImage originalImage, BufferedImage newImage) {
        for (Pixel pixel : this.pixels) {
            int color = originalImage.getRGB(pixel.x, pixel.y);

            if (pixel.alpha > 0)
                color = (pixel.alpha & 0xFF) << 24 | color & 0xFFFFFF;

            newImage.setRGB(pixel.x, pixel.y, color);
            originalImage.setRGB(pixel.x, pixel.y, 0);
        }
    }

    private static final class Pixel {

        private final int x;

        private final int y;

        private final int alpha;

        private Pixel(int x, int y, int alpha) {
            this.x = x;
            this.y = y;
            this.alpha = alpha;
        }
    }
}
