package mod.azure.azurelib.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.impl.AzItemAnimator;
import mod.azure.azurelib.render.AzBufferSource;
import mod.azure.azurelib.render.AzProvider;
import mod.azure.azurelib.render.AzRendererConfig;

public class AzArmorRenderer {

    private @Nullable Entity entity;

    private final AzProvider<UUID, ItemStack> provider;

    private final AzArmorRendererPipeline rendererPipeline;

    private @Nullable AzItemAnimator reusedAzItemAnimator;

    public AzArmorRenderer(AzArmorRendererConfig config) {
        this.provider = new AzProvider<>(
            config::createAnimator,
            config::modelLocation,
            animator -> {
                if (animator.get(AzureLib.AZ_ID.get()) != null) {
                    return UUID.randomUUID();
                }

                return animator.get(AzureLib.AZ_ID.get());
            }
        );
        this.rendererPipeline = createPipeline(config);
    }

    protected AzArmorRendererPipeline createPipeline(AzRendererConfig<UUID, ItemStack> config) {
        return new AzArmorRendererPipeline(config, this);
    }

    /**
     * Renders an AzureLib armor piece through Minecraft 26.2's deferred submit pipeline.
     * <p>
     * The AzureLib geometry pipeline runs against an identity pose stack. Its recorded local-space geometry is then
     * submitted through the vanilla armor layer's {@link SubmitNodeCollector} using the original entity pose.
     * </p>
     *
     * @return true when AzureLib successfully handled the armor piece; false to allow vanilla to render it instead.
     */
    public boolean render(
        PoseStack entityPoseStack,
        SubmitNodeCollector submitNodeCollector,
        Entity entity,
        HumanoidRenderState renderState,
        ItemStack stack,
        EquipmentSlot slot,
        HumanoidModel<?> baseModel,
        int packedLight
    ) {
        if (
            !prepForRender(
                entity,
                renderState,
                stack,
                slot,
                baseModel,
                true,
                null
            )
        ) {
            return false;
        }

        return renderPrepared(
            entityPoseStack,
            submitNodeCollector,
            entity,
            stack,
            packedLight
        );
    }

    private boolean renderPrepared(
        PoseStack entityPoseStack,
        SubmitNodeCollector submitNodeCollector,
        Entity entity,
        ItemStack stack,
        int packedLight
    ) {
        var geometry = new AzBufferSource();
        var localPoseStack = new PoseStack();
        var bakedModel = provider.provideBakedModel(entity, stack);
        var partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();

        rendererPipeline.render(
            localPoseStack,
            bakedModel,
            stack,
            geometry,
            null,
            null,
            0,
            partialTick,
            packedLight
        );

        geometry.submitAll(submitNodeCollector, entityPoseStack);

        return true;
    }

    /**
     * Prepares the renderer for the current armor render pass.
     */
    public boolean prepForRender(
        @Nullable Entity entity,
        @Nullable HumanoidRenderState renderState,
        ItemStack stack,
        @Nullable EquipmentSlot slot,
        @Nullable HumanoidModel<?> baseModel,
        boolean setupBaseModel,
        @Nullable ModelPart modelPartOverride
    ) {
        if (entity == null || renderState == null || slot == null || baseModel == null) {
            return false;
        }

        this.entity = entity;

        rendererPipeline.context()
            .prepare(
                entity,
                renderState,
                stack,
                slot,
                baseModel,
                setupBaseModel,
                modelPartOverride
            );

        prepareAnimator(stack);

        return true;
    }

    private void prepareAnimator(ItemStack stack) {
        if (entity == null) {
            reusedAzItemAnimator = null;
            return;
        }

        reusedAzItemAnimator = (AzItemAnimator) provider.provideAnimator(entity, stack);
    }

    public @Nullable AzItemAnimator animator() {
        return reusedAzItemAnimator;
    }

    public AzProvider<UUID, ItemStack> provider() {
        return provider;
    }

    public AzArmorRendererPipeline rendererPipeline() {
        return rendererPipeline;
    }

    public boolean renderForBone(
        PoseStack poseStack,
        AzBufferSource bufferSource,
        Entity entity,
        ItemStack stack,
        EquipmentSlot slot,
        HumanoidModel<?> baseModel,
        ModelPart modelPart,
        int packedLight
    ) {
        var renderState = new HumanoidRenderState();

        if (
            !prepForRender(
                entity,
                renderState,
                stack,
                slot,
                baseModel,
                false,
                modelPart
            )
        ) {
            return false;
        }

        var bakedModel = provider.provideBakedModel(entity, stack);
        var partialTick = Minecraft.getInstance()
            .getDeltaTracker()
            .getGameTimeDeltaTicks();

        rendererPipeline.render(
            poseStack,
            bakedModel,
            stack,
            bufferSource,
            null,
            null,
            0,
            partialTick,
            packedLight
        );

        return true;
    }
}
