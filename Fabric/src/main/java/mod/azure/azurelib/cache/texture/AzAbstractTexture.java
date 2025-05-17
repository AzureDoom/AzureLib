package mod.azure.azurelib.cache.texture;

import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public abstract class AzAbstractTexture extends SimpleTexture {

    static class GlowRenderType extends RenderType {

        public GlowRenderType(
            String p_i225992_1_,
            VertexFormat p_i225992_2_,
            int p_i225992_3_,
            int p_i225992_4_,
            boolean p_i225992_5_,
            boolean p_i225992_6_,
            Runnable p_i225992_7_,
            Runnable p_i225992_8_
        ) {
            super(
                p_i225992_1_,
                p_i225992_2_,
                p_i225992_3_,
                p_i225992_4_,
                p_i225992_5_,
                p_i225992_6_,
                p_i225992_7_,
                p_i225992_8_
            );
            // TODO Auto-generated constructor stub
        }

        public static RenderType emissive(ResourceLocation texture, boolean isGlowing) {
            return RenderType.create(
                "az_glowing_layer",
                DefaultVertexFormat.NEW_ENTITY,
                GL11.GL_QUADS,
                256,
                RenderType.CompositeState.builder()
                    .setAlphaState(RenderType.DEFAULT_ALPHA)
                    .setCullState(RenderType.NO_CULL)
                    .setTextureState(new TextureStateShard(texture, false, false))
                    .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                    .setOverlayState(RenderType.OVERLAY)
                    .createCompositeState(isGlowing)
            );
        }
    }

    private static final String APPENDIX = "_glowmask";

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

        if (!(textureManager.getTexture(texturePath) instanceof AzAbstractTexture))
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

        return new ResourceLocation(location.getNamespace(), path.substring(0, i) + suffix + path.substring(i));
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
            File file = new File(FabricLoader.getInstance().getGameDir().toFile(), "GeoTexture Debug Printouts");

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
    protected static ResourceLocation getEmissiveResource(ResourceLocation baseResource) {
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
        return GlowRenderType.emissive(getEmissiveResource(texture), false);
    }

    /**
     * Return a cached instance of the RenderType for the given texture for AutoGlowingGeoLayer rendering, while the
     * entity has an outline
     *
     * @param texture The texture of the resource to apply a glow layer to
     */
    public static RenderType getOutlineRenderType(ResourceLocation texture) {
        return GlowRenderType.emissive(getEmissiveResource(texture), true);
    }
}
