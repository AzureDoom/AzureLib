/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.cache.texture;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.GsonHelper;
import org.jspecify.annotations.Nullable;

import java.util.List;

import mod.azure.azurelib.render.layer.AzAutoGlowingLayer;

/**
 * Metadata class that stores the data for AzureLib's {@link AzAutoGlowingLayer emissive texture feature} for a given
 * texture.
 */
public class GeoGlowingTextureMeta {

    public static final Codec<GeoGlowingTextureMeta> CODEC = Codec.PASSTHROUGH.comapFlatMap(dynamic -> {
        JsonElement element = dynamic.convert(JsonOps.INSTANCE).getValue();

        if (!element.isJsonObject())
            return DataResult.error(() -> "Expected glowsections to be a JSON object");

        try {
            return DataResult.success(fromJson(element.getAsJsonObject()));
        } catch (RuntimeException ex) {
            return DataResult.error(ex::getMessage);
        }
    }, meta -> new Dynamic<>(JsonOps.INSTANCE, new JsonObject()));

    /**
     * 26.x resource metadata uses MetadataSectionType instead of MetadataSectionSerializer. Prefer a namespaced key for
     * new files, but keep the old key if your existing .mcmeta files use it.
     */
    public static final MetadataSectionType<GeoGlowingTextureMeta> TYPE = new MetadataSectionType<>(
        "glowsections",
        CODEC
    );

    private final List<Pixel> pixels;

    public GeoGlowingTextureMeta(List<Pixel> pixels) {
        this.pixels = pixels;
    }

    private static GeoGlowingTextureMeta fromJson(JsonObject json) {
        List<Pixel> pixels = fromSections(GsonHelper.getAsJsonArray(json, "sections", null));

        if (pixels.isEmpty())
            throw new JsonParseException("Empty glowlayer sections file. Must have at least one glow section!");

        return new GeoGlowingTextureMeta(pixels);
    }

    private static List<Pixel> fromSections(@Nullable JsonArray sectionsArray) {
        if (sectionsArray == null)
            return List.of();

        List<Pixel> pixels = new ObjectArrayList<>();

        for (JsonElement element : sectionsArray) {
            if (!(element instanceof JsonObject obj))
                throw new JsonParseException(
                    "Invalid glowsections json format, expected a JsonObject, found: " + element.getClass()
                );

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

    /** Generate the GlowLayer pixels list from an existing image resource, instead of using the .png.mcmeta file. */
    public static GeoGlowingTextureMeta fromExistingImage(NativeImage glowLayer) {
        List<Pixel> pixels = new ObjectArrayList<>();

        for (int x = 0; x < glowLayer.getWidth(); x++) {
            for (int y = 0; y < glowLayer.getHeight(); y++) {
                int color = glowLayer.getPixel(x, y);

                if (color != 0)
                    pixels.add(new Pixel(x, y, color >>> 24));
            }
        }

        if (pixels.isEmpty())
            throw new IllegalStateException("Invalid glow layer texture provided, must have at least one pixel!");

        return new GeoGlowingTextureMeta(pixels);
    }

    /** Create a new mask image based on the pre-determined pixel data. */
    public void createImageMask(NativeImage originalImage, NativeImage newImage) {
        for (Pixel pixel : this.pixels) {
            int color = originalImage.getPixel(pixel.x, pixel.y);

            if (pixel.alpha > 0)
                color = (pixel.alpha << 24) | (color & 0x00FFFFFF);

            newImage.setPixel(pixel.x, pixel.y, color);
            originalImage.setPixel(pixel.x, pixel.y, 0);
        }
    }

    private record Pixel(
        int x,
        int y,
        int alpha
    ) {}
}
