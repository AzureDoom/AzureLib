package mod.azure.azurelib.render.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.UUID;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.platform.Services;
import mod.azure.azurelib.render.AzLayerRenderer;
import mod.azure.azurelib.render.AzModelRenderer;
import mod.azure.azurelib.render.AzPhasedRenderer;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.util.client.RenderUtils;

/**
 * AzItemModelRenderer is a specialized implementation of {@link AzModelRenderer} for rendering {@link ItemStack}
 * objects. It provides customized rendering logic for rendering item models in a layered and recursive manner.
 */
public class AzItemModelRenderer extends AzModelRenderer<UUID, ItemStack> {

    protected final AzItemRendererPipeline itemRendererPipeline;

    public AzItemModelRenderer(
        AzItemRendererPipeline itemRendererPipeline,
        AzLayerRenderer<UUID, ItemStack> layerRenderer
    ) {
        super(itemRendererPipeline, layerRenderer);
        this.itemRendererPipeline = itemRendererPipeline;
    }

    /**
     * The actual render method that subtype renderers should override to handle their specific rendering tasks.<br>
     * {@link AzPhasedRenderer#preRender} has already been called by this stage, and {@link AzPhasedRenderer#postRender}
     * will be called directly after
     */
    @Override
    public void render(AzRendererPipelineContext<UUID, ItemStack> context, boolean isReRender) {
        if (!isReRender || context.applyAnimationOnReRender()) {
            var animatable = context.animatable();
            var animator = itemRendererPipeline.getRenderer().getAnimator();

            if (animator != null) {
                handleAnimation(animator, animatable, context.partialTick());
            }
        }

        var poseStack = context.poseStack();

        itemRendererPipeline.modelRenderTranslations = new Matrix4f(poseStack.last().pose());

        super.render(context, isReRender);
    }

    /**
     * Renders the provided {@link AzBone} and its associated child bones
     */
    @Override
    public void renderRecursively(AzRendererPipelineContext<UUID, ItemStack> context, AzBone bone, boolean isReRender) {
        var poseStack = context.poseStack();

        var itemRendererConfig = (AzItemRendererConfig) itemRendererPipeline.config();
        var itemContext = (AzItemRendererPipelineContext) itemRendererPipeline.context();
        boolean shouldFreezeTransforms = !itemRendererConfig.shouldAnimateInContext(itemContext.getTransformType());

        float origPosX = 0, origPosY = 0, origPosZ = 0;
        float origRotX = 0, origRotY = 0, origRotZ = 0;
        float origScaleX = 0, origScaleY = 0, origScaleZ = 0;

        if (shouldFreezeTransforms) {
            origPosX = bone.getPosX();
            origPosY = bone.getPosY();
            origPosZ = bone.getPosZ();
            origRotX = bone.getRotX();
            origRotY = bone.getRotY();
            origRotZ = bone.getRotZ();
            origScaleX = bone.getScaleX();
            origScaleY = bone.getScaleY();
            origScaleZ = bone.getScaleZ();

            var initialSnapshot = bone.getInitialAzSnapshot();
            bone.setPosX(initialSnapshot.getOffsetX());
            bone.setPosY(initialSnapshot.getOffsetY());
            bone.setPosZ(initialSnapshot.getOffsetZ());
            bone.setRotX(initialSnapshot.getRotX());
            bone.setRotY(initialSnapshot.getRotY());
            bone.setRotZ(initialSnapshot.getRotZ());
            bone.setScaleX(initialSnapshot.getScaleX());
            bone.setScaleY(initialSnapshot.getScaleY());
            bone.setScaleZ(initialSnapshot.getScaleZ());
        }

        poseStack.pushPose();

        var animator = itemRendererPipeline.getRenderer().getAnimator();
        var isAnimationPlaying = false;
        // Check if the first-person mod is loaded as it has its own arm system for items
        var firstPerson = Services.PLATFORM.isModLoaded("firstperson");
        // Check if the bone is an arm bone and the first person mod is loaded
        var isArmBone = AzItemArmRenderUtil.isArmBone(bone) && !firstPerson;

        if (animator != null) {
            // Check all animation controllers to see if any are playing
            for (var controller : animator.getAnimationControllerContainer().getAll()) {
                if (controller.stateMachine().isPlaying()) {
                    isAnimationPlaying = true;
                    break;
                }
            }
        }

        // Check if the bone is an arm bone and an animation is playing
        if (isArmBone && isAnimationPlaying) {
            AzItemArmRenderUtil.renderArmForBone(context, bone, this);
        }

        if (bone.isTrackingMatrices()) {
            var animatable = context.animatable();
            var poseState = new Matrix4f(poseStack.last().pose());
            var localMatrix = RenderUtils.invertAndMultiplyMatrices(
                poseState,
                itemRendererPipeline.itemRenderTranslations
            );

            bone.setModelSpaceMatrix(
                RenderUtils.invertAndMultiplyMatrices(poseState, itemRendererPipeline.modelRenderTranslations)
            );
            bone.setLocalSpaceMatrix(
                RenderUtils.translateMatrix(localMatrix, getRenderOffset(animatable, 1).toVector3f())
            );
        }

        context.setVertexConsumer(getOrRefreshRenderBuffer(isReRender, context, bone));

        super.renderRecursively(context, bone, isReRender);

        if (shouldFreezeTransforms) {
            bone.setPosX(origPosX);
            bone.setPosY(origPosY);
            bone.setPosZ(origPosZ);
            bone.setRotX(origRotX);
            bone.setRotY(origRotY);
            bone.setRotZ(origRotZ);
            bone.setScaleX(origScaleX);
            bone.setScaleY(origScaleY);
            bone.setScaleZ(origScaleZ);
        }

        poseStack.popPose();
    }

    public Vec3 getRenderOffset(ItemStack itemStack, float f) {
        return Vec3.ZERO;
    }
}
