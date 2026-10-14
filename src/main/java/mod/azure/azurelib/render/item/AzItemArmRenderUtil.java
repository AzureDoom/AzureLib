package mod.azure.azurelib.render.item;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.UUID;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.platform.mc.McClient;
import mod.azure.azurelib.render.AzModelRenderer;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.vertex.AzBufferSource;
import mod.azure.azurelib.render.vertex.GlStateManager;
import mod.azure.azurelib.render.vertex.MultiBufferSource;
import mod.azure.azurelib.render.vertex.PoseStack;
import mod.azure.azurelib.util.client.RenderUtils;

/**
 * Utility for rendering the local player's arms in place of an item model's {@code leftArm}/{@code rightArm} bones in
 * first person. On Beta the player's model parts render through the GL matrix stack, so pending AzureLib vertices are
 * flushed first and the bone's pose is pushed onto the GL matrix.
 */
public class AzItemArmRenderUtil {

    private static final BipedEntityModel ARM_MODEL = new BipedEntityModel(0.0F);

    private static final String LEFT_ARM_BONE = "leftArm";

    private static final String RIGHT_ARM_BONE = "rightArm";

    /**
     * Checks if the given bone is an arm bone that should be rendered.
     *
     * @param bone The bone to check
     * @return true if this is a left or right arm bone
     */
    public static boolean isArmBone(AzBone bone) {
        String name = bone.getName();
        return LEFT_ARM_BONE.equals(name) || RIGHT_ARM_BONE.equals(name);
    }

    /**
     * Checks if arm rendering should occur based on the current display context.
     *
     * @param context The rendering context
     * @return true if we should render arms for this context
     */
    public static boolean shouldRenderArmsForContext(AzItemRendererPipelineContext context) {
        AzItemDisplayContext transformType = context.getTransformType();
        return transformType == AzItemDisplayContext.FIRST_PERSON_RIGHT_HAND ||
            transformType == AzItemDisplayContext.FIRST_PERSON_LEFT_HAND;
    }

    /**
     * Renders player arms for the specified arm bone, hiding the bone itself but keeping children visible. This method
     * should be called during the bone rendering process.
     *
     * @param context       The rendering context
     * @param bone          The arm bone to render
     * @param modelRenderer The model renderer instance (needed for buffer methods)
     */
    public static void renderArmForBone(
        AzRendererPipelineContext<UUID, ItemStack> context,
        AzBone bone,
        AzModelRenderer<UUID, ItemStack> modelRenderer
    ) {
        AzItemRendererPipelineContext itemContext = (AzItemRendererPipelineContext) context;

        if (!shouldRenderArmsForContext(itemContext)) {
            return;
        }

        bone.setHidden(true);
        bone.setChildrenHidden(false);

        PlayerEntity player = McClient.player();

        if (player == null) {
            return;
        }

        // A private biped with vanilla player proportions; Beta's renderer keeps its own model in a field AzureLib
        // would need an accessor for, and the arm geometry is identical.
        BipedEntityModel playerEntityModel = ARM_MODEL;
        PoseStack poseStack = context.poseStack();

        poseStack.pushPose();
        RenderUtils.translateMatrixToBone(poseStack, bone);
        RenderUtils.translateToPivotPoint(poseStack, bone);
        RenderUtils.rotateMatrixAroundBone(poseStack, bone);
        RenderUtils.scaleMatrixForBone(poseStack, bone);
        RenderUtils.translateAwayFromPivotPoint(poseStack, bone);

        if (LEFT_ARM_BONE.equals(bone.getName())) {
            poseStack.scale(0.67f, 1.33f, 0.67f);
            poseStack.translate(-0.25, -0.43625, 0.1625);
            renderArmParts(
                context.multiBufferSource(),
                poseStack,
                bone,
                player,
                playerEntityModel.leftArm,
                null
            );
        } else if (RIGHT_ARM_BONE.equals(bone.getName())) {
            poseStack.scale(0.67f, 1.33f, 0.67f);
            poseStack.translate(0.25, -0.43625, 0.1625);
            renderArmParts(
                context.multiBufferSource(),
                poseStack,
                bone,
                player,
                playerEntityModel.rightArm,
                null
            );
        }

        context.setVertexConsumer(context.multiBufferSource().getBuffer(context.renderType()));
        poseStack.popPose();
    }

    private static void renderArmParts(
        MultiBufferSource bufferSource,
        PoseStack poseStack,
        AzBone bone,
        PlayerEntity player,
        ModelPart arm,
        ModelPart sleeve
    ) {
        if (bufferSource instanceof AzBufferSource) {
            ((AzBufferSource) bufferSource).endBatch();
        }

        McClient.bindPlayerSkin(player);
        GlStateManager.pushMatrix();
        RenderUtils.applyPoseToGl(poseStack.last());
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        renderPart(arm, bone);

        // Beta skins have no sleeve layer; the parameter is kept for parity with later versions.
        if (sleeve != null) {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
            );
            renderPart(sleeve, bone);
            GlStateManager.disableBlend();
        }

        GlStateManager.popMatrix();
    }

    private static void renderPart(ModelPart part, AzBone bone) {
        float oldX = part.pivotX, oldY = part.pivotY, oldZ = part.pivotZ;
        float oldRotX = part.pitch, oldRotY = part.yaw, oldRotZ = part.roll;

        McClient.setPivot(part, bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
        part.pitch = 0;
        part.yaw = 0;
        part.roll = 0;
        part.render(0.0625F);

        McClient.setPivot(part, oldX, oldY, oldZ);
        part.pitch = oldRotX;
        part.yaw = oldRotY;
        part.roll = oldRotZ;
    }
}
