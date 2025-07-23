package mod.azure.azurelib.rewrite.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import mod.azure.azurelib.rewrite.model.AzBone;
import mod.azure.azurelib.rewrite.render.AzLayerRenderer;
import mod.azure.azurelib.rewrite.render.AzModelRenderer;
import mod.azure.azurelib.rewrite.render.AzPhasedRenderer;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;
import mod.azure.azurelib.util.RenderUtils;

/**
 * AzItemModelRenderer is a specialized implementation of {@link AzModelRenderer} for rendering {@link ItemStack}
 * objects. It provides customized rendering logic for rendering item models in a layered and recursive manner.
 */
public class AzItemModelRenderer extends AzModelRenderer<ItemStack> {

    private final AzItemRendererPipeline itemRendererPipeline;

    public AzItemModelRenderer(AzItemRendererPipeline itemRendererPipeline, AzLayerRenderer<ItemStack> layerRenderer) {
        super(itemRendererPipeline, layerRenderer);
        this.itemRendererPipeline = itemRendererPipeline;
    }

    /**
     * The actual render method that subtype renderers should override to handle their specific rendering tasks.<br>
     * {@link AzPhasedRenderer#preRender} has already been called by this stage, and {@link AzPhasedRenderer#postRender}
     * will be called directly after
     */
    @Override
    public void render(AzRendererPipelineContext<ItemStack> context, boolean isReRender) {
        if (!isReRender) {
            ItemStack animatable = context.animatable();
            mod.azure.azurelib.rewrite.animation.impl.@org.jetbrains.annotations.Nullable AzItemAnimator animator =
                itemRendererPipeline.getRenderer().getAnimator();

            if (animator != null) {
                animator.animate(animatable, context.partialTick());
            }
        }

        PoseStack poseStack = context.poseStack();

        itemRendererPipeline.modelRenderTranslations = new Matrix4f(poseStack.last().pose());

        super.render(context, isReRender);
    }

    /**
     * Renders the provided {@link AzBone} and its associated child bones
     */
    @Override
    public void renderRecursively(AzRendererPipelineContext<ItemStack> context, AzBone bone, boolean isReRender) {
        if (bone.isTrackingMatrices()) {
            ItemStack animatable = context.animatable();
            PoseStack poseStack = context.poseStack();
            Matrix4f poseState = new Matrix4f(poseStack.last().pose());
            Matrix4f localMatrix = RenderUtils.invertAndMultiplyMatrices(
                poseState,
                itemRendererPipeline.itemRenderTranslations
            );
            Matrix4f worldState = localMatrix.copy();

            bone.setModelSpaceMatrix(
                RenderUtils.invertAndMultiplyMatrices(poseState, itemRendererPipeline.modelRenderTranslations)
            );
            bone.setLocalSpaceMatrix(
                RenderUtils.invertAndMultiplyMatrices(poseState, itemRendererPipeline.modelRenderTranslations)
            );
            worldState.translate(new Vector3f(getRenderOffset(animatable, 1)));
            bone.setWorldSpaceMatrix(worldState);
        }

        super.renderRecursively(context, bone, isReRender);
    }

    public Vec3 getRenderOffset(ItemStack itemStack, float f) {
        return Vec3.ZERO;
    }
}
