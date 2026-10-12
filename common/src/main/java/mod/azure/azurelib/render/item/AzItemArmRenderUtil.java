package mod.azure.azurelib.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
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

        if (!shouldRenderArmsForContext(itemContext)) {
            return;
        }

        bone.setHidden(true);
        bone.setChildrenHidden(false);

        var client = Minecraft.getInstance();
        var poseStack = context.poseStack();
        var packedLight = context.packedLight();

        EntityRenderer<?, ?> renderer = client.getEntityRenderDispatcher().getRenderer(client.player);

        if (!(renderer instanceof AvatarRenderer<?> playerEntityRenderer)) {
            return;
        }

        PlayerModel playerEntityModel = playerEntityRenderer.getModel();

        var playerSkin = ((LocalPlayer) ClientUtils.getClientPlayer())
            .getSkin()
            .body()
            .texturePath();

        poseStack.pushPose();

        RenderUtils.translateMatrixToBone(poseStack, bone);
        RenderUtils.translateToPivotPoint(poseStack, bone);
        RenderUtils.rotateMatrixAroundBone(poseStack, bone);
        RenderUtils.scaleMatrixForBone(poseStack, bone);
        RenderUtils.translateAwayFromPivotPoint(poseStack, bone);

        if (LEFT_ARM_BONE.equals(bone.getName())) {
            poseStack.scale(0.67f, 1.33f, 0.67f);
            poseStack.translate(-0.25, -0.43625, 0.1625);
            renderArm(
                poseStack,
                bone,
                playerEntityModel.leftArm,
                playerEntityModel.leftSleeve,
                playerSkin,
                packedLight,
                itemContext,
                modelRenderer
            );
        } else if (RIGHT_ARM_BONE.equals(bone.getName())) {
            poseStack.scale(0.67f, 1.33f, 0.67f);
            poseStack.translate(0.25, -0.43625, 0.1625);
            renderArm(
                poseStack,
                bone,
                playerEntityModel.rightArm,
                playerEntityModel.rightSleeve,
                playerSkin,
                packedLight,
                itemContext,
                modelRenderer
            );
        }

        poseStack.popPose();
    }

    private static void renderArm(
        PoseStack poseStack,
        AzBone bone,
        ModelPart arm,
        ModelPart sleeve,
        Identifier playerSkin,
        int packedLight,
        AzItemRendererPipelineContext itemContext,
        AzModelRenderer<UUID, ItemStack> modelRenderer
    ) {
        arm.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
        arm.setRotation(0, 0, 0);

        var sleeveVisible = sleeve.visible;
        var armSkipDraw = arm.skipDraw;

        try {
            sleeve.visible = false;
            arm.render(
                poseStack,
                modelRenderer.getOrRefreshBufferRenderType(itemContext, bone, RenderTypes.entitySolid(playerSkin)),
                packedLight,
                OverlayTexture.NO_OVERLAY
            );

            if (sleeveVisible) {
                sleeve.visible = true;
                arm.skipDraw = true;
                arm.render(
                    poseStack,
                    modelRenderer.getOrRefreshBufferRenderType(
                        itemContext,
                        bone,
                        RenderTypes.entityTranslucent(playerSkin)
                    ),
                    packedLight,
                    OverlayTexture.NO_OVERLAY
                );
            }
        } finally {
            sleeve.visible = sleeveVisible;
            arm.skipDraw = armSkipDraw;
        }
    }
}
