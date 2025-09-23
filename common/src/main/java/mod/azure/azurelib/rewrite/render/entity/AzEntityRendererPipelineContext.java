package mod.azure.azurelib.rewrite.render.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.rewrite.render.AzRendererPipeline;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;
import mod.azure.azurelib.util.ClientUtils;

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
    public @NotNull RenderType getDefaultRenderType(
        T animatable,
        ResourceLocation texture,
        @Nullable MultiBufferSource bufferSource,
        float partialTick
    ) {
        var isInvisible = animatable.isInvisible();
        var isPlayerInvisible = animatable.isInvisibleTo(ClientUtils.getClientPlayer());

        if (isInvisible && !isPlayerInvisible) {
            return RenderType.itemEntityTranslucentCull(texture);
        }

        if (Minecraft.getInstance().shouldEntityAppearGlowing(animatable)) {
            return RenderType.entityTranslucentEmissive(texture, true);
        }

        return RenderType.entityCutoutNoCull(texture);
    }

    /**
     * Gets a packed overlay coordinate pair for rendering.<br>
     * Mostly just used for the red tint when an entity is hurt, but can be used for other things like the
     * {@link net.minecraft.world.entity.monster.Creeper} white tint when exploding.
     */
    @Override
    public int getPackedOverlay(T entity, float u, float partialTick) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            return OverlayTexture.NO_OVERLAY;
        }

        return OverlayTexture.pack(
            OverlayTexture.u(u),
            OverlayTexture.v(livingEntity.hurtTime > 0 || livingEntity.deathTime > 0)
        );
    }
}
