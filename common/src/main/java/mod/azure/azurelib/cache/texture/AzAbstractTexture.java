package mod.azure.azurelib.cache.texture;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

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

    protected static final RenderStateShard.WriteMaskStateShard WRITE_MASK = new RenderStateShard.WriteMaskStateShard(true, true);

    protected static final BiFunction<Identifier, Boolean, RenderType> GLOWING_RENDER_TYPE = Util.memoize(
        (texture, isGlowing) -> {
            RenderStateShard.TextureStateShard textureState = new RenderStateShard.TextureStateShard(texture, false, false);

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

    public AzAbstractTexture(Identifier location) {
        super(location);
    }

    protected static void generateTexture(Identifier texturePath, Consumer<TextureManager> textureManagerConsumer) {
        if (!RenderSystem.isOnRenderThreadOrInit())
            throw new IllegalThreadStateException("Texture loading called outside of the render thread! This should DEFINITELY not be happening.");

        TextureManager textureManager = Minecraft.getInstance().getTextureManager();

        if (!(textureManager.getTexture(texturePath, MissingTextureAtlasSprite.getTexture()) instanceof AzAbstractTexture))
            textureManagerConsumer.accept(textureManager);
    }

    /** Upload a NativeImage into this texture object using the 26.2 GPU texture path. */
    public void uploadSimple(NativeImage image) {
        GpuDevice gpuDevice = RenderSystem.getDevice();
        Identifier textureId = resourceId();

        Objects.requireNonNull(textureId);

        this.texture = gpuDevice.createTexture(textureId::toString, 5, GpuFormat.RGBA8_UNORM, image.getWidth(), image.getHeight(), 1, 1);
        this.textureView = gpuDevice.createTextureView(this.texture);
        uploadSimple(this.texture, image);
    }

    /** Upload a NativeImage into an existing GPU texture. */
    public static void uploadSimple(GpuTexture texture, NativeImage image) {
        RenderSystem.getDevice().createCommandEncoder().writeToTexture(texture, image.getPixelBytes(), 0, 0, 0, 0, image.getWidth(), image.getHeight());
    }

    public static Identifier appendToPath(Identifier location, String suffix) {
        String path = location.getPath();
        int i = path.lastIndexOf('.');

        return Identifier.fromNamespaceAndPath(location.getNamespace(), path.substring(0, i) + suffix + path.substring(i));
    }

    public static Identifier getEmissiveResource(Identifier baseResource) {
        Identifier path = appendToPath(baseResource, APPENDIX);

        generateTexture(path, textureManager -> textureManager.register(path, new AutoGlowingTexture(baseResource, path)));

        return path;
    }

    public static RenderType getRenderType(Identifier texture) {
        return GLOWING_RENDER_TYPE.apply(getEmissiveResource(texture), false);
    }

    public static RenderType getOutlineRenderType(Identifier texture) {
        return GLOWING_RENDER_TYPE.apply(getEmissiveResource(texture), true);
    }
}
