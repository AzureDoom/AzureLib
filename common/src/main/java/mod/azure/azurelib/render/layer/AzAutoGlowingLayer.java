package mod.azure.azurelib.render.layer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;

import mod.azure.azurelib.cache.texture.AutoGlowingTexture;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.util.client.ClientUtils;

/**
 * A {@link AzRenderLayer} dedicated to rendering the auto-generated glow layer functionality provided by AzureLib. This
 * utilizes texture files with the <i>_glowing</i> suffix to create glowing effects for models.
 */
public class AzAutoGlowingLayer<K, T> implements AzRenderLayer<K, T> {

    @Override
    public void preRender(AzRendererPipelineContext<K, T> context) {}

    @Override
    public void render(AzRendererPipelineContext<K, T> context) {
        var renderPipeline = context.rendererPipeline();
        var renderType = determineRenderType(context);

        var prevRenderType = context.renderType();
        var prevVertexConsumer = context.vertexConsumer();

        if (renderType != null) {
            context.setRenderType(renderType);
            context.setPackedLight(getPackedLight(context));
            context.setVertexConsumer(context.multiBufferSource().getBuffer(renderType));

            renderPipeline.reRender(context);
        }

        context.setRenderType(prevRenderType);
        context.setVertexConsumer(prevVertexConsumer);
    }

    @Override
    public void renderForBone(AzRendererPipelineContext<K, T> context, AzBone bone) {}

    /**
     * Calculates and returns the packed light value to be used in the rendering pipeline.
     *
     * @param context The rendering context that contains information about the current rendering pipeline, the
     *                animatable entity, and other rendering configurations.
     * @return The packed light value, typically used to determine the lighting conditions in rendering.
     */
    protected int getPackedLight(AzRendererPipelineContext<K, T> context) {
        return LightTexture.FULL_SKY;
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
            return AutoGlowingTexture.getRenderType(textureLocation);
        }

        var isInvisible = entity.isInvisible();
        var appearsGlowing = Minecraft.getInstance().shouldEntityAppearGlowing(entity);
        var isPlayerInvisible = entity.isInvisibleTo(ClientUtils.getClientPlayer());

        if (isInvisible) {
            if (!isPlayerInvisible) {
                return RenderType.itemEntityTranslucentCull(AutoGlowingTexture.getEmissiveResource(textureLocation));
            }
            if (appearsGlowing) {
                return RenderType.outline(AutoGlowingTexture.getEmissiveResource(textureLocation));
            }
            return null;
        }

        if (appearsGlowing) {
            return AutoGlowingTexture.getOutlineRenderType(textureLocation);
        }

        return AutoGlowingTexture.getRenderType(textureLocation);
    }
}
