/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.cache.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.ARGB;

import java.util.List;
import java.util.Optional;

import mod.azure.azurelib.render.layer.AzAutoGlowingLayer;

/**
 * Metadata class that stores the data for AzureLib's {@link AzAutoGlowingLayer emissive texture feature} for a given
 * texture
 */
public class AzGlowingTextureMeta {

    public static final Codec<AzGlowingTextureMeta> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Section.CODEC.listOf().fieldOf("sections").forGetter(AzGlowingTextureMeta::sections)
        ).apply(instance, AzGlowingTextureMeta::new)
    );

    public static final MetadataSectionType<AzGlowingTextureMeta> TYPE = new MetadataSectionType<>(
        "glowsections",
        CODEC
    );

    private final List<Section> sections;

    private final List<Pixel> pixels;

    private AzGlowingTextureMeta(List<Section> sections) {
        this.sections = sections;
        this.pixels = expand(sections);
    }

    private AzGlowingTextureMeta(List<Section> sections, List<Pixel> pixels) {
        this.sections = sections;
        this.pixels = pixels;
    }

    private List<Section> sections() {
        return this.sections;
    }

    private static List<Pixel> expand(List<Section> sections) {
        List<Pixel> pixels = new ObjectArrayList<>();

        for (Section section : sections) {
            int x1 = section.minX();
            int y1 = section.minY();
            int x2 = section.maxX();
            int y2 = section.maxY();

            if (x1 + y1 + x2 + y2 == 0)
                throw new IllegalArgumentException(
                    "Invalid glowsections section object, section must be at least one pixel in size"
                );

            for (int x = x1; x <= x2; x++) {
                for (int y = y1; y <= y2; y++) {
                    pixels.add(new Pixel(x, y, section.alphaValue()));
                }
            }
        }

        if (pixels.isEmpty())
            throw new IllegalStateException("Empty glowlayer sections file. Must have at least one glow section!");

        return pixels;
    }

    /** Generate the GlowLayer pixels list from an existing image resource, instead of using the .png.mcmeta file. */
    public static AzGlowingTextureMeta fromExistingImage(NativeImage glowLayer) {
        List<Pixel> pixels = new ObjectArrayList<>();

        for (int x = 0; x < glowLayer.getWidth(); x++) {
            for (int y = 0; y < glowLayer.getHeight(); y++) {
                int color = glowLayer.getPixel(x, y);

                if (color != 0)
                    pixels.add(new Pixel(x, y, ARGB.alpha(color)));
            }
        }

        if (pixels.isEmpty())
            throw new IllegalStateException("Invalid glow layer texture provided, must have at least one pixel!");

        return new AzGlowingTextureMeta(List.of(), pixels);
    }

    /**
     * Create a new mask image based on the pre-determined pixel data
     */
    public void createImageMask(NativeImage originalImage, NativeImage newImage) {
        for (Pixel pixel : this.pixels) {
            int color = originalImage.getPixel(pixel.x, pixel.y);

            if (pixel.alpha > 0)
                color = ARGB.color(pixel.alpha, ARGB.red(color), ARGB.green(color), ARGB.blue(color));

            newImage.setPixel(pixel.x, pixel.y, color);
            originalImage.setPixel(pixel.x, pixel.y, 0);
        }
    }

    private record Section(
        Optional<Integer> x1,
        Optional<Integer> y1,
        Optional<Integer> x2,
        Optional<Integer> y2,
        Optional<Integer> x,
        Optional<Integer> y,
        Optional<Integer> w,
        Optional<Integer> h,
        Optional<Integer> alpha,
        Optional<Integer> a
    ) {

        static final Codec<Section> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                Codec.INT.optionalFieldOf("x1").forGetter(Section::x1),
                Codec.INT.optionalFieldOf("y1").forGetter(Section::y1),
                Codec.INT.optionalFieldOf("x2").forGetter(Section::x2),
                Codec.INT.optionalFieldOf("y2").forGetter(Section::y2),
                Codec.INT.optionalFieldOf("x").forGetter(Section::x),
                Codec.INT.optionalFieldOf("y").forGetter(Section::y),
                Codec.INT.optionalFieldOf("w").forGetter(Section::w),
                Codec.INT.optionalFieldOf("h").forGetter(Section::h),
                Codec.INT.optionalFieldOf("alpha").forGetter(Section::alpha),
                Codec.INT.optionalFieldOf("a").forGetter(Section::a)
            ).apply(instance, Section::new)
        );

        int minX() {
            return this.x1.orElseGet(() -> this.x.orElse(0));
        }

        int minY() {
            return this.y1.orElseGet(() -> this.y.orElse(0));
        }

        int maxX() {
            return this.x2.orElseGet(() -> this.w.orElse(0) + minX());
        }

        int maxY() {
            return this.y2.orElseGet(() -> this.h.orElse(0) + minY());
        }

        int alphaValue() {
            return this.alpha.orElseGet(() -> this.a.orElse(0));
        }
    }

    private record Pixel(
        int x,
        int y,
        int alpha
    ) {}
}
