package mod.azure.azurelib.render.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import mod.azure.azurelib.render.AzRendererPipeline;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.util.client.ClientUtils;

/**
 * A context class specifically for rendering entities using a custom rendering pipeline. This class extends
 * {@code AzRendererPipelineContext} and provides implementations for methods to customize entity rendering, such as
 * determining default render types and packed overlay settings.
 *
 * @param <T> the type of entity being rendered, extending {@code Entity}
 */
public class AzEntityRendererPipelineContext<T extends Entity> extends AzRendererPipelineContext<UUID, T> {

    public AzEntityRendererPipelineContext(AzRendererPipeline<UUID, T> rendererPipeline) {
        super(rendererPipeline);
    }

    @Override
    public @NotNull RenderType getDefaultRenderType(
        T animatable,
        ResourceLocation texture,
        @Nullable MultiBufferSource bufferSource,
        float partialTick,
        RenderType defaultRenderType,
        float alpha
    ) {
        var translucent = animatable.isInvisible() && !animatable.isInvisibleTo(ClientUtils.getClientPlayer());
        var visibleBody = !animatable.isInvisible(); // strictly visible flag
        var glowing = Minecraft.getInstance().shouldEntityAppearGlowing(animatable);
        var hurtOrDead = animatable instanceof LivingEntity living && (living.hurtTime > 1 || living.isDeadOrDying());

        // Handle entity damage/death state
        if (visibleBody && !glowing && hurtOrDead) {
            if (
                defaultRenderType == RenderType.entityTranslucentCull(texture) || defaultRenderType == RenderType
                    .entityTranslucent(texture)
            ) {
                return RenderType.entityCutoutNoCull(texture);
            }
            return defaultRenderType;
        }

        // Handle transparency
        if (visibleBody && alpha < 1.0F) {
            return RenderType.entityTranslucent(texture);
        }

        // --- Vanilla-style fallback ---
        if (translucent) {
            return RenderType.entityTranslucent(texture);
        } else if (visibleBody) {
            return defaultRenderType;
        } else if (glowing) {
            return RenderType.outline(texture);
        } else {
            return null;
        }
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
