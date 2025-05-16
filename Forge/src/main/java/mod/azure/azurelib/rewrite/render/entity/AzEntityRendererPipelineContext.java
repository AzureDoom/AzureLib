package mod.azure.azurelib.rewrite.render.entity;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.util.ResourceLocation;

import mod.azure.azurelib.rewrite.render.AzRendererPipeline;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;

/**
 * A context class specifically for rendering entities using a custom rendering pipeline. This class extends
 * {@code AzRendererPipelineContext} and provides implementations for methods to customize entity rendering, such as
 * determining default render types and packed overlay settings.
 *
 * @param <T> the type of entity being rendered, extending {@code Entity}
 */
public class AzEntityRendererPipelineContext<T extends Entity> extends AzRendererPipelineContext<T> {

    public AzEntityRendererPipelineContext(AzRendererPipeline<T> rendererPipeline) {
        super(rendererPipeline);
    }

    @Override
    public RenderType getDefaultRenderType(
        T animatable,
        ResourceLocation texture,
        IRenderTypeBuffer bufferSource,
        float partialTick
    ) {
        return RenderType.entityCutoutNoCull(texture);
    }

    /**
     * Gets a packed overlay coordinate pair for rendering.<br>
     * Mostly just used for the red tint when an entity is hurt, but can be used for other things like the
     * {@link CreeperEntity} white tint when exploding.
     */
    @Override
    public int getPackedOverlay(T entity, float u, float partialTick) {
        if (!(entity instanceof LivingEntity)) {
            return OverlayTexture.NO_OVERLAY;
        }
        LivingEntity livingEntity = (LivingEntity) entity;

        return OverlayTexture.pack(
            OverlayTexture.u(u),
            OverlayTexture.v(livingEntity.hurtTime > 0 || livingEntity.deathTime > 0)
        );
    }
}
