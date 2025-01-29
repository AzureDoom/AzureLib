package mod.azure.azurelib.common.internal.common.cache.texture.util;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.Arrays;
import java.util.List;

import mod.azure.azurelib.common.internal.common.AzureLib;

public class AnimationContents {

    public final FrameSize frameSize;

    public final Texture animatedTexture;

    private int getID;

    private ResourceLocation location;

    public AnimationContents(
        NativeImage image,
        AnimationMetadataSection animMeta,
        int getID,
        ResourceLocation location
    ) {
        this.frameSize = animMeta.calculateFrameSize(image.getWidth(), image.getHeight());
        this.animatedTexture = generateAnimatedTexture(image, animMeta);
        this.getID = getID;
        this.location = location;
    }

    public boolean isValid() {
        return this.animatedTexture != null;
    }

    private Texture generateAnimatedTexture(NativeImage image, AnimationMetadataSection animMeta) {
        if (
            !Mth.isMultipleOf(image.getWidth(), this.frameSize.width()) || !Mth.isMultipleOf(
                image.getHeight(),
                this.frameSize.height()
            )
        ) {
            AzureLib.LOGGER.error(
                "Image {} size {},{} is not multiple of frame size {},{}",
                this.location,
                image.getWidth(),
                image.getHeight(),
                this.frameSize.width(),
                this.frameSize.height()
            );

            return null;
        }

        int columns = image.getWidth() / this.frameSize.width();
        int rows = image.getHeight() / this.frameSize.height();
        int frameCount = columns * rows;
        List<Frame> frames = new ObjectArrayList<>();

        animMeta.forEachFrame((frame, frameTime) -> frames.add(new Frame(frame, frameTime)));

        if (frames.isEmpty()) {
            for (int frame = 0; frame < frameCount; ++frame) {
                frames.add(new Frame(frame, animMeta.getDefaultFrameTime()));
            }
        } else {
            int index = 0;
            IntSet unusedFrames = new IntOpenHashSet();

            for (Frame frame : frames) {
                if (frame.time() <= 0) {
                    AzureLib.LOGGER.warn(
                        "Invalid frame duration on sprite {} frame {}: {}",
                        this.location,
                        index,
                        frame.time()
                    );
                    unusedFrames.add(frame.index());
                } else if (frame.index() < 0 || frame.index() >= frameCount) {
                    AzureLib.LOGGER.warn(
                        "Invalid frame index on sprite {} frame {}: {}",
                        this.location,
                        index,
                        frame.index()
                    );
                    unusedFrames.add(frame.index());
                }

                index++;
            }

            if (!unusedFrames.isEmpty())
                AzureLib.LOGGER.warn(
                    "Unused frames in sprite {}: {}",
                    this.location,
                    Arrays.toString(unusedFrames.toArray())
                );
        }

        return frames.size() <= 1
            ? null
            : new Texture(
                image,
                frames.toArray(new Frame[0]),
                columns,
                animMeta.isInterpolatedFrames(),
                frameSize,
                getID
            );
    }
}
