package mod.azure.azurelib.cache.texture;

import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import mod.azure.azurelib.platform.Services;

public abstract class AzAbstractTexture extends SimpleTexture {

    protected static final RenderStateShard.ShaderStateShard SHADER_STATE = new RenderStateShard.ShaderStateShard(
        GameRenderer::getRendertypeEntityTranslucentEmissiveShader
    );

    protected static final RenderStateShard.TransparencyStateShard TRANSPARENCY_STATE =
        new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );
        }, () -> {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        });

    protected static final RenderStateShard.WriteMaskStateShard WRITE_MASK = new RenderStateShard.WriteMaskStateShard(
        true,
        true
    );

    protected static final BiFunction<ResourceLocation, Boolean, RenderType> GLOWING_RENDER_TYPE = Util.memoize(
        (texture, isGlowing) -> {
            RenderStateShard.TextureStateShard textureState = new RenderStateShard.TextureStateShard(
                texture,
                false,
                false
            );

            return RenderType.create(
                "az_glowing_layer",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                false,
                true,
                RenderType.CompositeState.builder()
                    .setShaderState(SHADER_STATE)
                    .setTextureState(textureState)
                    .setTransparencyState(TRANSPARENCY_STATE)
                    .setOverlayState(new RenderStateShard.OverlayStateShard(true))
                    .setWriteMaskState(WRITE_MASK)
                    .createCompositeState(isGlowing)
            );
        }
    );

    protected static final String APPENDIX = "_glowmask";

    public AzAbstractTexture(ResourceLocation location) {
        super(location);
    }

    public static void onRenderThread(RenderCall renderCall) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(renderCall);
        } else {
            renderCall.execute();
        }
    }

    /**
     * Generates the texture instance for the given path with the given appendix if it hasn't already been generated
     */
    protected static void generateTexture(
        ResourceLocation texturePath,
        Consumer<TextureManager> textureManagerConsumer
    ) {
        if (!RenderSystem.isOnRenderThreadOrInit())
            throw new IllegalThreadStateException(
                "Texture loading called outside of the render thread! This should DEFINITELY not be happening."
            );

        TextureManager textureManager = Minecraft.getInstance().getTextureManager();

        if (
            !(textureManager.getTexture(
                texturePath,
                MissingTextureAtlasSprite.getTexture()
            ) instanceof AzAbstractTexture)
        )
            textureManagerConsumer.accept(textureManager);
    }

    /**
     * No-frills helper method for uploading {@link NativeImage images} into memory for use
     */
    public static void uploadSimple(int texture, NativeImage image, boolean blur, boolean clamp) {
        TextureUtil.prepareImage(texture, 0, image.getWidth(), image.getHeight());
        image.upload(0, 0, 0, 0, 0, image.getWidth(), image.getHeight(), blur, clamp, false, true);
    }

    public static ResourceLocation appendToPath(ResourceLocation location, String suffix) {
        String path = location.getPath();
        int i = path.lastIndexOf('.');

        return ResourceLocation.fromNamespaceAndPath(
            location.getNamespace(),
            path.substring(0, i) + suffix + path.substring(i)
        );
    }

    @Override
    public void load(ResourceManager resourceManager) throws IOException {
        RenderCall renderCall = loadTexture(resourceManager, Minecraft.getInstance());

        if (renderCall == null)
            return;

        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(renderCall);
        } else {
            renderCall.execute();
        }
    }

    /**
     * Debugging function to write out the generated glowmap image to disk
     */
    protected void printDebugImageToDisk(ResourceLocation id, NativeImage newImage) {
        try {
            File file = new File(Services.PLATFORM.getGameDir().toFile(), "GeoTexture Debug Printouts");

            if (!file.exists()) {
                file.mkdirs();
            } else if (!file.isDirectory()) {
                file.delete();
                file.mkdirs();
            }

            file = new File(file, id.getPath().replace('/', '.'));

            if (!file.exists())
                file.createNewFile();

            newImage.writeToFile(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Called at {@link AbstractTexture#load} time to load this texture for the first time into the render cache.
     * Generate and apply the necessary functions here, then return the RenderCall to submit to the render pipeline.
     *
     * @return The RenderCall to submit to the render pipeline, or null if no further action required
     */
    @Nullable
    protected abstract RenderCall loadTexture(ResourceManager resourceManager, Minecraft mc) throws IOException;

    /**
     * Get the emissive resource equivalent of the input resource path.<br>
     * Additionally prepares the texture manager for the missing texture if the resource is not present
     *
     * @return The glowlayer resourcepath for the provided input path
     */
    public static ResourceLocation getEmissiveResource(ResourceLocation baseResource) {
        ResourceLocation path = appendToPath(baseResource, APPENDIX);

        generateTexture(
            path,
            textureManager -> textureManager.register(path, new AutoGlowingTexture(baseResource, path))
        );

        return path;
    }

    /**
     * Return a cached instance of the RenderType for the given texture for GeoGlowingLayer rendering.
     *
     * @param texture The texture of the resource to apply a glow layer to
     */
    public static RenderType getRenderType(ResourceLocation texture) {
        return GLOWING_RENDER_TYPE.apply(getEmissiveResource(texture), false);
    }

    /**
     * Return a cached instance of the RenderType for the given texture for AutoGlowingGeoLayer rendering, while the
     * entity has an outline
     *
     * @param texture The texture of the resource to apply a glow layer to
     */
    public static RenderType getOutlineRenderType(ResourceLocation texture) {
        return GLOWING_RENDER_TYPE.apply(getEmissiveResource(texture), true);
    }
}
