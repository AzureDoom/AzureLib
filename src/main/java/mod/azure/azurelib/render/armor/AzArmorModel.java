package mod.azure.azurelib.render.armor;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.platform.mc.McClient;
import mod.azure.azurelib.render.vertex.AzBufferSource;
import mod.azure.azurelib.render.vertex.OverlayTexture;
import mod.azure.azurelib.render.vertex.PoseStack;
import mod.azure.azurelib.render.vertex.RenderType;
import mod.azure.azurelib.render.vertex.VertexConsumer;
import mod.azure.azurelib.util.AzEquipmentSlot;
import mod.azure.azurelib.util.client.AzRenderTick;
import mod.azure.azurelib.util.client.RenderUtils;

/**
 * A {@link BipedEntityModel} whose parts are only used as the pose source for an AzureLib armor model.
 * <p>
 * Beta's player renderer draws armor in "render passes" with a vanilla biped model. AzureLib's armor hook
 * ({@code PlayerEntityRendererMixin_AzArmor}) cancels the vanilla pass for AzureLib armor and calls
 * {@link #renderArmor(LivingEntity, ItemStack, AzEquipmentSlot, float)} at the same point instead, so the GL matrix is
 * already in the player's model space, exactly where vanilla would draw its armor.
 */
public class AzArmorModel extends BipedEntityModel {

    /** Beta renders entity models at 1/16 block per model unit. */
    private static final float MODEL_SCALE = 0.0625F;

    private final AzArmorRendererPipeline rendererPipeline;

    @Nullable
    private AzArmorRenderer pendingRenderer;

    @Nullable
    private ItemStack pendingStack;

    @Nullable
    private AzEquipmentSlot pendingSlot;

    public AzArmorModel(AzArmorRendererPipeline rendererPipeline) {
        super(1.0F);
        this.rendererPipeline = rendererPipeline;
    }

    public void prepare(AzArmorRenderer renderer, ItemStack stack, AzEquipmentSlot slot) {
        this.pendingRenderer = renderer;
        this.pendingStack = stack;
        this.pendingSlot = slot;
    }

    private static void resetPart(ModelPart part, float x, float y, float z) {
        McClient.setPivot(part, x, y, z);
        part.pitch = part.yaw = part.roll = 0;
    }

    private void resetPose() {
        resetPart(head, 0, 0, 0);
        resetPart(hat, 0, 0, 0);
        resetPart(body, 0, 0, 0);
        resetPart(rightArm, -5, 2, 0);
        resetPart(leftArm, 5, 2, 0);
        resetPart(rightLeg, -1.9F, 12, 0);
        resetPart(leftLeg, 1.9F, 12, 0);
    }

    /**
     * Poses this biped exactly like Beta's {@code LivingEntityRenderer} poses the player model, then renders the
     * AzureLib armor piece for the given slot following that pose.
     */
    public void renderArmor(LivingEntity entity, ItemStack stack, AzEquipmentSlot slot, float partialTick) {
        AzArmorRenderer renderer = this.rendererPipeline.renderer();
        prepare(renderer, stack, slot);

        float limbSwingAmount = Math.min(
            1.0F,
            Mc.prevLimbSwingAmount(entity) + (Mc.limbSwingAmount(entity) - Mc.prevLimbSwingAmount(entity))
                * partialTick
        );
        float limbSwing = Mc.limbSwing(entity) - Mc.limbSwingAmount(entity) * (1.0F - partialTick);
        float bodyYaw = Mc.prevBodyYaw(entity) + (Mc.bodyYaw(entity) - Mc.prevBodyYaw(entity)) * partialTick;
        float headYaw = Mc.prevYaw(entity) + (Mc.yaw(entity) - Mc.prevYaw(entity)) * partialTick;
        float pitch = Mc.prevPitch(entity) + (Mc.pitch(entity) - Mc.prevPitch(entity)) * partialTick;
        float age = Mc.age(entity) + partialTick;

        render(entity, limbSwing, limbSwingAmount, age, headYaw - bodyYaw, pitch, MODEL_SCALE);
    }

    public void render(
        @Nonnull Entity entity,
        float limbSwing,
        float limbSwingAmount,
        float ageInTicks,
        float netHeadYaw,
        float headPitch,
        float scale
    ) {
        if (pendingRenderer == null || pendingStack == null || pendingSlot == null) {
            return;
        }

        // Beta's biped poses the arms from these flags, which its renderer copies from the player's state.
        McClient.setBipedPose(
            this,
            Mc.isSneaking(entity),
            entity instanceof LivingEntity && Mc.heldItem((LivingEntity) entity) != null,
            entity instanceof LivingEntity ? Mc.swingProgress((LivingEntity) entity, AzRenderTick.partialTicks()) : 0
        );

        resetPose();
        McClient.setAngles(this, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        pendingRenderer.prepForRender(entity, pendingStack, pendingSlot, this);

        AzBufferSource bufferSource = AzBufferSource.getInstance();
        bufferSource.endBatch();
        renderToBuffer(
            new PoseStack(),
            null,
            RenderUtils.currentPackedLight(),
            OverlayTexture.NO_OVERLAY,
            1,
            1,
            1,
            1
        );
        bufferSource.endBatch();
    }

    public void renderToBuffer(
        @Nonnull PoseStack poseStack,
        @Nullable VertexConsumer buffer,
        int packedLight,
        int packedOverlay,
        float red,
        float green,
        float blue,
        float alpha
    ) {
        AzArmorRendererPipelineContext context = rendererPipeline.context();
        Entity currentEntity = context.currentEntity();

        if (currentEntity == null) {
            return;
        }

        AzBufferSource bufferSource = AzBufferSource.getInstance();
        AzArmorRendererConfig config = rendererPipeline.config();
        ItemStack animatable = context.animatable();
        float partialTick = AzRenderTick.partialTicks();
        RenderType renderType = context.getDefaultRenderType(
            animatable,
            config.textureLocation(currentEntity, animatable),
            bufferSource,
            partialTick,
            config.getRenderType(currentEntity, animatable),
            config.alpha(animatable)
        );

        // Beta has no enchantments, so armor never has a glint.
        buffer = bufferSource.getBuffer(renderType);
        AzBakedModel model = rendererPipeline.renderer().provider().provideBakedModel(currentEntity, animatable);
        rendererPipeline.render(poseStack, model, animatable, bufferSource, null, buffer, 0, partialTick, packedLight);
    }

    /** Copies the pose flags of another biped (for mods that render AzureLib armor on their own biped mobs). */
    public void applyBaseModel(BipedEntityModel baseModel) {
        McClient.copyBipedPose(baseModel, this);
    }
}
