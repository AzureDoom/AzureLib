package mod.azure.azurelib.render.layer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToIntFunction;

import mod.azure.azurelib.cache.texture.AzAbstractTexture;
import mod.azure.azurelib.core.object.Color;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.util.client.ClientUtils;

/**
 * A {@link AzRenderLayer} dedicated to rendering the auto-generated glow layer functionality provided by AzureLib. This
 * utilizes texture files with the <i>_glowing</i> suffix to create glowing effects for models.
 * <p>
 * The glow can be tinted by passing a color (or a function of the render context) to the constructor:
 * </p>
 *
 * <pre>{@code
 * .addRenderLayer(new AzAutoGlowingLayer<>(Color.RED))
 * .addRenderLayer(new AzAutoGlowingLayer<>(context -> isAngry(context.animatable()) ? 0xFFFF4040 : 0xFFFFFFFF))
 * }</pre>
 * <p>
 * The tint is an ARGB color multiplied with the renderer's own color, so white leaves the glow untouched and lower
 * alpha fades the glow out. Tinting works best on white or gray glow textures, since the tint multiplies the texture's
 * colors.
 * </p>
 */
public class AzAutoGlowingLayer<K, T> implements AzRenderLayer<K, T> {

    private static final int NO_TINT = 0xFFFFFFFF;

    @Nullable
    private final ToIntFunction<AzRendererPipelineContext<K, T>> glowColor;

    /**
     * Creates a glow layer that renders the glow texture with the renderer's normal color.
     */
    public AzAutoGlowingLayer() {
        this.glowColor = null;
    }

    /**
     * Creates a glow layer tinted with a fixed color.
     *
     * @param glowColor The color to tint the glow with
     */
    public AzAutoGlowingLayer(Color glowColor) {
        var argb = glowColor.argbInt();
        this.glowColor = argb == NO_TINT ? null : context -> argb;
    }

    /**
     * Creates a glow layer whose tint is computed every frame, e.g. to change or pulse the glow based on the
     * animatable's state.
     *
     * @param glowColor Returns the ARGB color to tint the glow with for the given render context
     */
    public AzAutoGlowingLayer(@Nullable ToIntFunction<AzRendererPipelineContext<K, T>> glowColor) {
        this.glowColor = glowColor;
    }

    @Override
    public void preRender(AzRendererPipelineContext<K, T> context) {}

    @Override
    public void render(AzRendererPipelineContext<K, T> context) {
        var renderPipeline = context.rendererPipeline();
        var renderType = determineRenderType(context);

        var prevRenderType = context.renderType();
        var prevVertexConsumer = context.vertexConsumer();
        var prevRenderColor = context.renderColor();
        var prevPackedLight = context.packedLight();

        if (renderType != null) {
            var tint = getGlowColor(context);

            if (tint != NO_TINT) {
                context.setRenderColor(multiplyColors(prevRenderColor, tint));
            }

            context.setRenderType(renderType);
            context.setPackedLight(getPackedLight(context));
            context.setVertexConsumer(context.multiBufferSource().getBuffer(renderType));

            renderPipeline.reRender(context);
        }

        context.setRenderType(prevRenderType);
        context.setVertexConsumer(prevVertexConsumer);
        context.setRenderColor(prevRenderColor);
        context.setPackedLight(prevPackedLight);
    }

    @Override
    public void renderForBone(AzRendererPipelineContext<K, T> context, AzBone bone) {}

    /**
     * Returns the ARGB color to tint the glow with. It is multiplied with the renderer's own color, so
     * {@code 0xFFFFFFFF} means no tint. Uses the color given to the constructor by default; override to compute it some
     * other way.
     *
     * @param context The current rendering context
     * @return The ARGB tint for the glow
     */
    protected int getGlowColor(AzRendererPipelineContext<K, T> context) {
        return glowColor == null ? NO_TINT : glowColor.applyAsInt(context);
    }

    private static int multiplyColors(int a, int b) {
        var alpha = ((a >>> 24) * (b >>> 24) + 127) / 255;
        var red = (((a >> 16) & 0xFF) * ((b >> 16) & 0xFF) + 127) / 255;
        var green = (((a >> 8) & 0xFF) * ((b >> 8) & 0xFF) + 127) / 255;
        var blue = ((a & 0xFF) * (b & 0xFF) + 127) / 255;
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    /**
     * Calculates and returns the packed light value to be used in the rendering pipeline.
     *
     * @param context The rendering context that contains information about the current rendering pipeline, the
     *                animatable entity, and other rendering configurations.
     * @return The packed light value, typically used to determine the lighting conditions in rendering.
     */
    protected int getPackedLight(AzRendererPipelineContext<K, T> context) {
        return LightCoordsUtil.FULL_SKY;
    }

    /**
     * Determines the appropriate RenderType for the animatable entity in the given rendering context. Handles special
     * cases such as invisibility, glowing appearance, and outline rendering.
     *
     * @param context The context containing the animatable and rendering configuration.
     * @return The appropriate RenderType for rendering the entity.
     */
    protected RenderType determineRenderType(AzRendererPipelineContext<K, T> context) {
        var animatable = context.animatable();
        var config = context.rendererPipeline().config();
        var textureLocation = config.textureLocation(context.currentEntity(), animatable);

        if (!(animatable instanceof Entity entity)) {
            return AzAbstractTexture.getRenderType(textureLocation);
        }

        var isInvisible = entity.isInvisible();
        var appearsGlowing = Minecraft.getInstance().shouldEntityAppearGlowing(entity);
        var isPlayerInvisible = entity.isInvisibleTo(ClientUtils.getClientPlayer());

        if (isInvisible) {
            if (!isPlayerInvisible) {
                return RenderTypes.entityTranslucent(AzAbstractTexture.getEmissiveResource(textureLocation));
            }
            if (appearsGlowing) {
                return RenderTypes.outline(AzAbstractTexture.getEmissiveResource(textureLocation));
            }
            return null;
        }

        if (appearsGlowing) {
            return AzAbstractTexture.getOutlineRenderType(textureLocation);
        }

        return AzAbstractTexture.getRenderType(textureLocation);
    }
}
