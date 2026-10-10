package mod.azure.azurelib.render.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzModelRenderer;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.vertex.AzBufferSource;
import mod.azure.azurelib.render.vertex.MultiBufferSource;
import mod.azure.azurelib.render.vertex.PoseStack;
import mod.azure.azurelib.util.client.RenderUtils;

/**
 * Utility for rendering the local player's arms in place of an item model's {@code leftArm}/{@code rightArm} bones in
 * first person. On 1.12.2 the player's model parts render through the GL matrix stack, so pending AzureLib vertices are
 * flushed first and the bone's pose is pushed onto the GL matrix.
 */
public class AzItemArmRenderUtil {

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
        ItemCameraTransforms.TransformType transformType = context.getTransformType();
        return transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND ||
            transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND;
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

        Minecraft client = Minecraft.getMinecraft();

        if (!(client.player instanceof AbstractClientPlayer)) {
            return;
        }

        AbstractClientPlayer player = client.player;
        Render<AbstractClientPlayer> renderer = client.getRenderManager().getEntityRenderObject(player);

        if (!(renderer instanceof RenderPlayer)) {
            return;
        }

        ModelPlayer playerEntityModel = ((RenderPlayer) renderer).getMainModel();
        ResourceLocation playerSkin = player.getLocationSkin();
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
                playerSkin,
                playerEntityModel.bipedLeftArm,
                playerEntityModel.bipedLeftArmwear
            );
        } else if (RIGHT_ARM_BONE.equals(bone.getName())) {
            poseStack.scale(0.67f, 1.33f, 0.67f);
            poseStack.translate(0.25, -0.43625, 0.1625);
            renderArmParts(
                context.multiBufferSource(),
                poseStack,
                bone,
                playerSkin,
                playerEntityModel.bipedRightArm,
                playerEntityModel.bipedRightArmwear
            );
        }

        context.setVertexConsumer(context.multiBufferSource().getBuffer(context.renderType()));
        poseStack.popPose();
    }

    private static void renderArmParts(
        MultiBufferSource bufferSource,
        PoseStack poseStack,
        AzBone bone,
        ResourceLocation playerSkin,
        ModelRenderer arm,
        ModelRenderer sleeve
    ) {
        if (bufferSource instanceof AzBufferSource) {
            ((AzBufferSource) bufferSource).endBatch();
        }

        Minecraft.getMinecraft().getTextureManager().bindTexture(playerSkin);
        GlStateManager.pushMatrix();
        RenderUtils.applyPoseToGl(poseStack.last());
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        renderPart(arm, bone);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );
        renderPart(sleeve, bone);
        GlStateManager.disableBlend();

        GlStateManager.popMatrix();
    }

    private static void renderPart(ModelRenderer part, AzBone bone) {
        float oldX = part.rotationPointX, oldY = part.rotationPointY, oldZ = part.rotationPointZ;
        float oldRotX = part.rotateAngleX, oldRotY = part.rotateAngleY, oldRotZ = part.rotateAngleZ;

        part.setRotationPoint(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
        part.rotateAngleX = 0;
        part.rotateAngleY = 0;
        part.rotateAngleZ = 0;
        part.render(0.0625F);

        part.setRotationPoint(oldX, oldY, oldZ);
        part.rotateAngleX = oldRotX;
        part.rotateAngleY = oldRotY;
        part.rotateAngleZ = oldRotZ;
    }
}
