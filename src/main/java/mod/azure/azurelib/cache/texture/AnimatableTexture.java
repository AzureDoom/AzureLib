package mod.azure.azurelib.cache.texture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.util.AzureLibUtil;
import mod.azure.azurelib.util.client.RenderUtils;

/**
 * Wrapper for {@link SimpleTexture SimpleTexture} implementation allowing for animated textures for AzureLib models.
 * <p>
 * On modern versions AzureLib swaps plain textures for this class through a {@code TextureManager} mixin. On 1.7.10 the
 * swap happens lazily in {@link #ensureLoaded(ResourceLocation)}, which every AzureLib render pipeline calls (via
 * {@link #setAndUpdate(ResourceLocation)}) before drawing: textures with an {@code animation} section in their
 * {@code .mcmeta} are loaded as an {@link AnimatableTexture}, everything else is left to vanilla.
 */
public class AnimatableTexture extends SimpleTexture {

    /** Per-location result of the "is this an animated texture?" check. Cleared on resource reload. */
    private static final Map<ResourceLocation, Boolean> ANIMATION_CACHE = new HashMap<>();

    private static final Map<Class<?>, Method> SET_ANIMATION_FRAME = new HashMap<>();

    protected AnimationContents animationContents = null;

    protected boolean isAnimated = false;

    public AnimatableTexture(final ResourceLocation location) {
        super(location);
    }

    /**
     * Forget which textures were found to be animated. Called after every resource reload.
     */
    public static void onResourceReload() {
        ANIMATION_CACHE.clear();
    }

    /**
     * Make sure the texture at the given location is loaded, and loaded as an {@link AnimatableTexture} if its
     * {@code .mcmeta} declares an animation.
     */
    public static void ensureLoaded(ResourceLocation location) {
        Boolean cached = ANIMATION_CACHE.get(location);

        if (cached != null)
            return;

        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        ITextureObject current = textureManager.getTexture(location);

        if (current instanceof AnimatableTexture) {
            ANIMATION_CACHE.put(location, ((AnimatableTexture) current).isAnimated());
            return;
        }

        if (current != null && current.getClass() != SimpleTexture.class) {
            // Someone else manages this texture (dynamic, atlas, another mod's custom type); leave it alone.
            ANIMATION_CACHE.put(location, false);
            return;
        }

        if (!hasAnimationMetadata(location)) {
            ANIMATION_CACHE.put(location, false);
            return;
        }

        AnimatableTexture animatableTexture = new AnimatableTexture(location);

        if (current != null)
            textureManager.deleteTexture(location);

        if (!textureManager.loadTexture(location, animatableTexture)) {
            AzureLib.LOGGER.error("Failed to load texture {}", location);
            ANIMATION_CACHE.put(location, false);
            return;
        }

        ANIMATION_CACHE.put(location, animatableTexture.isAnimated());
    }

    private static boolean hasAnimationMetadata(ResourceLocation texture) {
        ResourceLocation mcmeta = new ResourceLocation(
            texture.getResourceDomain(),
            texture.getResourcePath() + ".mcmeta"
        );

        try {
            Minecraft.getMinecraft().getResourceManager().getResource(mcmeta).getInputStream().close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void loadTexture(IResourceManager manager) throws IOException {
        this.deleteGlTexture();

        BufferedImage image;
        AnimationMetadataSection animMeta = null;
        boolean blur = false;
        boolean clamp = false;

        IResource resource = manager.getResource(this.textureLocation);

        try (InputStream stream = resource.getInputStream()) {
            image = ImageIO.read(stream);

            if (resource.hasMetadata()) {
                animMeta = (AnimationMetadataSection) resource.getMetadata("animation");
                TextureMetadataSection textureMeta = (TextureMetadataSection) resource.getMetadata("texture");

                if (textureMeta != null) {
                    blur = textureMeta.getTextureBlur();
                    clamp = textureMeta.getTextureClamp();
                }
            }
        }

        AnimationContents previous = this.animationContents;
        this.animationContents = null;
        this.isAnimated = false;

        if (animMeta != null) {
            AnimationContents contents = new AnimationContents(image, animMeta);

            if (contents.isValid()) {
                this.animationContents = contents;
                this.isAnimated = true;
            }
        }

        if (!this.isAnimated) {
            // Not (validly) animated: behave exactly like a SimpleTexture.
            TextureUtil.uploadTextureImageAllocate(getGlTextureId(), image, blur, clamp);
            return;
        }

        this.animationContents.animatedTexture.uploadFrame(this.animationContents.animatedTexture.currentFrame);

        ResourceLocation glowPath = GeoAbstractTexture.appendToPath(this.textureLocation, "_glowmask");
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

        if (previous != null && textureManager.getTexture(glowPath) instanceof AutoGlowingTexture) {
            // The glowmask was built against the previous animation contents, rebuild it against these.
            textureManager.deleteTexture(glowPath);
            textureManager.loadTexture(glowPath, new AutoGlowingTexture(this.textureLocation, glowPath));
        }
    }

    public boolean isAnimated() {
        return this.isAnimated;
    }

    int frameWidth() {
        return this.animationContents == null ? 0 : this.animationContents.frameWidth;
    }

    int frameHeight() {
        return this.animationContents == null ? 0 : this.animationContents.frameHeight;
    }

    void setGlowMaskTexture(AutoGlowingTexture texture, BufferedImage baseImage, BufferedImage glowMask) {
        if (this.animationContents != null)
            this.animationContents.animatedTexture.setGlowMaskTexture(texture, baseImage, glowMask);
    }

    void forceFrameUpload() {
        if (this.animationContents != null)
            this.animationContents.animatedTexture.uploadFrame(this.animationContents.animatedTexture.currentFrame);
    }

    public static void setAndUpdate(ResourceLocation texturePath) {
        setAndUpdate(texturePath, (int) RenderUtils.getCurrentTick());
    }

    /**
     * Ensures the texture is loaded (as an {@link AnimatableTexture} when animated) and advances its animation to the
     * given tick.
     */
    public static void setAndUpdate(ResourceLocation texturePath, int frameTick) {
        if (texturePath == null)
            return;

        ensureLoaded(texturePath);
        ITextureObject texture = Minecraft.getMinecraft().getTextureManager().getTexture(texturePath);

        if (texture instanceof AnimatableTexture) {
            ((AnimatableTexture) texture).setAnimationFrame(frameTick);
        } else if (texture != null) {
            Method method = SET_ANIMATION_FRAME.computeIfAbsent(texture.getClass(), type -> {
                try {
                    return type.getMethod("setAnimationFrame", int.class);
                } catch (NoSuchMethodException e) {
                    return null;
                }
            });

            if (method != null) {
                try {
                    method.invoke(texture, frameTick);
                } catch (ReflectiveOperationException ignored) {}
            }
        }
    }

    public void setAnimationFrame(int tick) {
        if (this.animationContents != null)
            this.animationContents.animatedTexture.setCurrentFrame(tick);
    }

    private static final class Frame {

        private final int index;

        private final int time;

        private Frame(int index, int time) {
            this.index = index;
            this.time = time;
        }

        int index() {
            return this.index;
        }

        int time() {
            return this.time;
        }
    }

    protected class AnimationContents {

        protected final int frameWidth;

        protected final int frameHeight;

        protected final Texture animatedTexture;

        private AnimationContents(BufferedImage image, AnimationMetadataSection animMeta) {
            int width = image.getWidth();
            int height = image.getHeight();
            int metaWidth = animMeta.getFrameWidth();
            int metaHeight = animMeta.getFrameHeight();

            // Same rules as 1.18's AnimationMetadataSection#getFrameSize
            if (metaWidth != -1) {
                this.frameWidth = metaWidth;
                this.frameHeight = metaHeight != -1 ? metaHeight : height;
            } else if (metaHeight != -1) {
                this.frameWidth = width;
                this.frameHeight = metaHeight;
            } else {
                int size = Math.min(width, height);
                this.frameWidth = size;
                this.frameHeight = size;
            }

            this.animatedTexture = generateAnimatedTexture(image, animMeta);
        }

        private boolean isValid() {
            return this.animatedTexture != null;
        }

        private Texture generateAnimatedTexture(BufferedImage image, AnimationMetadataSection animMeta) {
            if (
                this.frameWidth <= 0 || this.frameHeight <= 0 || !AzureLibUtil.isMultipleOf(
                    image.getWidth(),
                    this.frameWidth
                ) || !AzureLibUtil.isMultipleOf(image.getHeight(), this.frameHeight)
            ) {
                AzureLib.LOGGER.error(
                    "Image {} size {},{} is not multiple of frame size {},{}",
                    AnimatableTexture.this.textureLocation,
                    image.getWidth(),
                    image.getHeight(),
                    this.frameWidth,
                    this.frameHeight
                );
                return null;
            }

            int columns = image.getWidth() / this.frameWidth;
            int rows = image.getHeight() / this.frameHeight;
            int frameCount = columns * rows;
            List<Frame> frames = new ArrayList<>();

            for (int i = 0; i < animMeta.getFrameCount(); i++) {
                frames.add(new Frame(animMeta.getFrameIndex(i), animMeta.getFrameTimeSingle(i)));
            }

            if (frames.isEmpty()) {
                for (int frame = 0; frame < frameCount; ++frame) {
                    frames.add(new Frame(frame, animMeta.getFrameTime()));
                }
            } else {
                int index = 0;
                Set<Integer> unusedFrames = new HashSet<>();

                for (Frame frame : frames) {
                    if (frame.time <= 0) {
                        AzureLib.LOGGER.warn(
                            "Invalid frame duration on sprite {} frame {}: {}",
                            AnimatableTexture.this.textureLocation,
                            index,
                            frame.time
                        );
                        unusedFrames.add(frame.index);
                    } else if (frame.index < 0 || frame.index >= frameCount) {
                        AzureLib.LOGGER.warn(
                            "Invalid frame index on sprite {} frame {}: {}",
                            AnimatableTexture.this.textureLocation,
                            index,
                            frame.index
                        );
                        unusedFrames.add(frame.index);
                    }

                    index++;
                }

                if (!unusedFrames.isEmpty())
                    AzureLib.LOGGER.warn(
                        "Unused frames in sprite {}: {}",
                        AnimatableTexture.this.textureLocation,
                        Arrays.toString(unusedFrames.toArray())
                    );
            }

            return frames.size() <= 1
                ? null
                : new Texture(image, frames.toArray(new Frame[0]), columns, false); // 1.7.10 animation metadata has no
                                                                                    // "interpolate" flag
        }

        public class Texture {

            private BufferedImage baseImage;

            private final Frame[] frames;

            private final int framePanelSize;

            private final boolean interpolating;

            private final BufferedImage interpolatedFrame;

            private final int totalFrameTime;

            protected int glowMaskTextureId = -1;

            protected BufferedImage glowmaskImage = null;

            protected BufferedImage glowmaskInterpolatedFrame = null;

            private int currentFrame;

            private int currentSubframe;

            private Texture(BufferedImage baseImage, Frame[] frames, int framePanelSize, boolean interpolating) {
                this.baseImage = baseImage;
                this.frames = frames;
                this.framePanelSize = framePanelSize;
                this.interpolating = interpolating;
                this.interpolatedFrame = interpolating ? newFrameImage() : null;

                int time = 0;

                for (Frame frame : this.frames) {
                    time += frame.time;
                }

                this.totalFrameTime = time;
                this.currentFrame = this.frames[0].index;
            }

            private BufferedImage newFrameImage() {
                return new BufferedImage(
                    AnimationContents.this.frameWidth,
                    AnimationContents.this.frameHeight,
                    BufferedImage.TYPE_INT_ARGB
                );
            }

            private int getFrameX(int frameIndex) {
                return frameIndex % this.framePanelSize;
            }

            private int getFrameY(int frameIndex) {
                return frameIndex / this.framePanelSize;
            }

            public void setGlowMaskTexture(
                AutoGlowingTexture texture,
                BufferedImage baseImage,
                BufferedImage glowMask
            ) {
                this.glowMaskTextureId = texture.getGlTextureId();
                this.glowmaskImage = glowMask;
                this.glowmaskInterpolatedFrame = this.interpolating ? newFrameImage() : null;
                this.baseImage = baseImage;
            }

            private BufferedImage frameOf(BufferedImage image, int frameIndex) {
                return image.getSubimage(
                    getFrameX(frameIndex) * AnimationContents.this.frameWidth,
                    getFrameY(frameIndex) * AnimationContents.this.frameHeight,
                    AnimationContents.this.frameWidth,
                    AnimationContents.this.frameHeight
                );
            }

            void uploadFrame(int frameIndex) {
                TextureUtil.uploadTextureImageAllocate(
                    AnimatableTexture.this.getGlTextureId(),
                    frameOf(this.baseImage, frameIndex),
                    false,
                    false
                );

                if (this.glowmaskImage != null && this.glowMaskTextureId != -1) {
                    TextureUtil.uploadTextureImageAllocate(
                        this.glowMaskTextureId,
                        frameOf(this.glowmaskImage, frameIndex),
                        false,
                        false
                    );
                }
            }

            public void setCurrentFrame(int ticks) {
                ticks %= this.totalFrameTime;

                if (ticks == this.currentSubframe)
                    return;

                int lastSubframe = this.currentSubframe;
                int lastFrame = this.currentFrame;
                int time = 0;

                for (Frame frame : this.frames) {
                    time += frame.time;

                    if (ticks < time) {
                        this.currentFrame = frame.index;
                        this.currentSubframe = ticks % frame.time;
                        break;
                    }
                }

                if (this.currentFrame != lastFrame && this.currentSubframe == 0) {
                    uploadFrame(this.currentFrame);
                } else if (this.currentSubframe != lastSubframe && this.interpolating) {
                    generateInterpolatedFrame(
                        AnimatableTexture.this.getGlTextureId(),
                        this.baseImage,
                        this.interpolatedFrame
                    );

                    if (this.glowmaskImage != null && this.glowMaskTextureId != -1) {
                        generateInterpolatedFrame(
                            this.glowMaskTextureId,
                            this.glowmaskImage,
                            this.glowmaskInterpolatedFrame
                        );
                    }
                }
            }

            private Frame frameForIndex(int frameIndex) {
                for (Frame frame : this.frames) {
                    if (frame.index == frameIndex)
                        return frame;
                }

                return this.frames[0];
            }

            private int framePosition(Frame target) {
                for (int i = 0; i < this.frames.length; i++) {
                    if (this.frames[i] == target)
                        return i;
                }

                return 0;
            }

            private void generateInterpolatedFrame(
                int textureId,
                BufferedImage image,
                BufferedImage interpolatedFrame
            ) {
                Frame frame = frameForIndex(this.currentFrame);
                double frameProgress = 1 - (double) this.currentSubframe / (double) frame.time();
                int nextFrameIndex = this.frames[(framePosition(frame) + 1) % this.frames.length].index();

                if (frame.index() != nextFrameIndex) {
                    for (int y = 0; y < interpolatedFrame.getHeight(); ++y) {
                        for (int x = 0; x < interpolatedFrame.getWidth(); ++x) {
                            int prevFramePixel = getPixel(image, frame.index(), x, y);
                            int nextFramePixel = getPixel(image, nextFrameIndex, x, y);
                            int blendedRed = interpolate(
                                frameProgress,
                                prevFramePixel >> 16 & 255,
                                nextFramePixel >> 16 & 255
                            );
                            int blendedGreen = interpolate(
                                frameProgress,
                                prevFramePixel >> 8 & 255,
                                nextFramePixel >> 8 & 255
                            );
                            int blendedBlue = interpolate(frameProgress, prevFramePixel & 255, nextFramePixel & 255);

                            interpolatedFrame.setRGB(
                                x,
                                y,
                                prevFramePixel & 0xFF000000 | blendedRed << 16 | blendedGreen << 8 | blendedBlue
                            );
                        }
                    }

                    TextureUtil.uploadTextureImageAllocate(textureId, interpolatedFrame, false, false);
                }
            }

            private int getPixel(BufferedImage image, int frameIndex, int x, int y) {
                return image.getRGB(
                    x + getFrameX(frameIndex) * AnimationContents.this.frameWidth,
                    y + getFrameY(frameIndex) * AnimationContents.this.frameHeight
                );
            }

            private int interpolate(double frameProgress, double prevColour, double nextColour) {
                return (int) (frameProgress * prevColour + (1 - frameProgress) * nextColour);
            }
        }
    }
}
