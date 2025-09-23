package mod.azure.azurelib.rewrite.render.item;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import mod.azure.azurelib.common.internal.client.util.RenderUtils;
import mod.azure.azurelib.rewrite.model.AzBone;
import mod.azure.azurelib.rewrite.render.AzLayerRenderer;
import mod.azure.azurelib.rewrite.render.AzModelRenderer;
import mod.azure.azurelib.rewrite.render.AzPhasedRenderer;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;

/**
 * AzItemModelRenderer is a specialized implementation of {@link AzModelRenderer} for rendering {@link ItemStack}
 * objects. It provides customized rendering logic for rendering item models in a layered and recursive manner.
 */
public class AzItemModelRenderer extends AzModelRenderer<ItemStack> {

    protected final AzItemRendererPipeline itemRendererPipeline;

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
    public void renderRecursively(AzRendererPipelineContext<ItemStack> context, AzBone bone, boolean isReRender) {
        var buffer = context.vertexConsumer();
        var bufferSource = context.multiBufferSource();
        var poseStack = context.poseStack();
        var renderType = context.renderType();

        poseStack.pushPose();
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

        var config = itemRendererPipeline.config();

        context.setTextureOverride(getTextureOverrideForBone(bone, context.animatable(), context.partialTick()));

        ResourceLocation texture = context.getTextureOverride() == null
            ? config.textureLocation(context.animatable())
            : context.getTextureOverride();

        RenderType renderTypeOverride = getRenderTypeOverrideForBone(
            bone,
            context.animatable(),
            texture,
            bufferSource,
            context.partialTick()
        );

        if (texture != null && renderTypeOverride == null) {
            renderTypeOverride = context.getDefaultRenderType(
                context.animatable(),
                texture,
                context.multiBufferSource(),
                context.partialTick()
            );
            renderType = renderTypeOverride;
        }

        if (renderTypeOverride != null) {
            context.setVertexConsumer(bufferSource.getBuffer(renderTypeOverride));
            renderType = renderTypeOverride;
        }

        if (!isReRender && buffer instanceof BufferBuilder builder && !builder.building) {
            context.setVertexConsumer(bufferSource.getBuffer(renderType));
        }

        if (
            !boneRenderOverride(
                poseStack,
                bone,
                bufferSource,
                buffer,
                context.partialTick(),
                context.packedLight(),
                context.packedOverlay(),
                context.renderColor()
            )
        ) {
            super.renderCubesOfBone(context, bone);
        }

        renderCubesOfBone(context, bone);

        if (!isReRender) {
            layerRenderer.applyRenderLayersForBone(context, bone);
        }

        renderChildBones(context, bone, isReRender);

        poseStack.popPose();
    }

    public Vec3 getRenderOffset(ItemStack itemStack, float f) {
        return Vec3.ZERO;
    }
}
