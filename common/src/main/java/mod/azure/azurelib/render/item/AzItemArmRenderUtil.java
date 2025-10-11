package mod.azure.azurelib.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzModelRenderer;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.util.client.ClientUtils;
import mod.azure.azurelib.util.client.RenderUtils;

/**
 * Utility class for rendering item-specific player arms in Minecraft. This class provides methods for determining
 * visibility and rendering logic for arm bones, typically used in first-person item rendering scenarios. It integrates
 * with custom pipeline contexts and model renderers.
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
        var name = bone.getName();
        return LEFT_ARM_BONE.equals(name) || RIGHT_ARM_BONE.equals(name);
    }

    /**
     * Checks if arm rendering should occur based on the current display context.
     *
     * @param context The rendering context
     * @return true if we should render arms for this context
     */
    public static boolean shouldRenderArmsForContext(AzItemRendererPipelineContext context) {
        var transformType = context.getTransformType();
        return transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ||
            transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
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
        var itemContext = (AzItemRendererPipelineContext) context;

        // Only render if this is a first-person context
        if (!shouldRenderArmsForContext(itemContext)) {
            return;
        }

        // Hide the arm bone but keep children visible
        bone.setHidden(true);
        bone.setChildrenHidden(false);

        var client = Minecraft.getInstance();
        var poseStack = context.poseStack();
        var packedLight = context.packedLight();

        // Get player model and skin
        var playerEntityRenderer = (PlayerRenderer) client.getEntityRenderDispatcher().getRenderer(client.player);
        var playerEntityModel = playerEntityRenderer.getModel();
        var playerSkin = ((LocalPlayer) ClientUtils.getClientPlayer()).getSkinTextureLocation();

        poseStack.pushPose();

        // Apply bone transformations
        RenderUtils.translateMatrixToBone(poseStack, bone);
        RenderUtils.translateToPivotPoint(poseStack, bone);
        RenderUtils.rotateMatrixAroundBone(poseStack, bone);
        RenderUtils.scaleMatrixForBone(poseStack, bone);
        RenderUtils.translateAwayFromPivotPoint(poseStack, bone);

        if (LEFT_ARM_BONE.equals(bone.getName())) {
            renderLeftArm(poseStack, bone, playerEntityModel, playerSkin, packedLight, itemContext, modelRenderer);
        } else if (RIGHT_ARM_BONE.equals(bone.getName())) {
            renderRightArm(poseStack, bone, playerEntityModel, playerSkin, packedLight, itemContext, modelRenderer);
        }

        // Only needed for 1.20.1 and lower
        context.setVertexConsumer(context.multiBufferSource().getBuffer(context.renderType()));

        poseStack.popPose();
    }

    /**
     * Renders the left arm and sleeve.
     */
    private static void renderLeftArm(
        PoseStack poseStack,
        AzBone bone,
        net.minecraft.client.model.PlayerModel<?> playerEntityModel,
        net.minecraft.resources.ResourceLocation playerSkin,
        int packedLight,
        AzItemRendererPipelineContext itemContext,
        AzModelRenderer<UUID, ItemStack> modelRenderer
    ) {
        poseStack.scale(0.67f, 1.33f, 0.67f);
        poseStack.translate(-0.25, -0.43625, 0.1625);

        // Set up and render the left arm
        playerEntityModel.leftArm.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
        playerEntityModel.leftArm.setRotation(0, 0, 0);
        playerEntityModel.leftArm.render(
            poseStack,
            modelRenderer.getOrRefreshBufferRenderType(itemContext, bone, RenderType.entitySolid(playerSkin)),
            packedLight,
            OverlayTexture.NO_OVERLAY
        );

        // Set up and render a left sleeve
        playerEntityModel.leftSleeve.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
        playerEntityModel.leftSleeve.setRotation(0, 0, 0);
        playerEntityModel.leftSleeve.render(
            poseStack,
            modelRenderer.getOrRefreshBufferRenderType(itemContext, bone, RenderType.entityTranslucent(playerSkin)),
            packedLight,
            OverlayTexture.NO_OVERLAY
        );
    }

    /**
     * Renders the right arm and sleeve.
     */
    private static void renderRightArm(
        PoseStack poseStack,
        AzBone bone,
        net.minecraft.client.model.PlayerModel<?> playerEntityModel,
        net.minecraft.resources.ResourceLocation playerSkin,
        int packedLight,
        AzItemRendererPipelineContext itemContext,
        AzModelRenderer<UUID, ItemStack> modelRenderer
    ) {
        poseStack.scale(0.67f, 1.33f, 0.67f);
        poseStack.translate(0.25, -0.43625, 0.1625);

        // Set up and render right arm
        playerEntityModel.rightArm.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
        playerEntityModel.rightArm.setRotation(0, 0, 0);
        playerEntityModel.rightArm.render(
            poseStack,
            modelRenderer.getOrRefreshBufferRenderType(itemContext, bone, RenderType.entitySolid(playerSkin)),
            packedLight,
            OverlayTexture.NO_OVERLAY
        );

        // Set up and render a right sleeve
        playerEntityModel.rightSleeve.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
        playerEntityModel.rightSleeve.setRotation(0, 0, 0);
        playerEntityModel.rightSleeve.render(
            poseStack,
            modelRenderer.getOrRefreshBufferRenderType(itemContext, bone, RenderType.entityTranslucent(playerSkin)),
            packedLight,
            OverlayTexture.NO_OVERLAY
        );
    }
}
