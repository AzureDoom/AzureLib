package mod.azure.azurelib.render.item;

import net.minecraft.item.ItemStack;

import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.render.vertex.AzBufferSource;
import mod.azure.azurelib.render.vertex.GlStateManager;
import mod.azure.azurelib.render.vertex.MultiBufferSource;
import mod.azure.azurelib.render.vertex.PoseStack;
import mod.azure.azurelib.render.vertex.RenderType;
import mod.azure.azurelib.render.vertex.VertexConsumer;
import mod.azure.azurelib.util.client.AzRenderTick;

/**
 * Utility for rendering items in a GUI context.
 */
public class AzItemGuiRenderUtil {

    /**
     * Renders an item in the GUI. Items configured with {@code useEntityGuiLighting()} get world-style entity lighting;
     * everything else keeps Beta's standard GUI item lighting.
     */
    public static void renderInGui(
        AzItemRendererConfig config,
        AzItemRendererPipeline rendererPipeline,
        ItemStack stack,
        AzBakedModel model,
        ItemStack currentItemStack,
        PoseStack poseStack,
        MultiBufferSource source,
        int packedLight
    ) {
        boolean entityLighting = config.useEntityGuiLighting();
        if (entityLighting) {
            setLightsInEyeSpace(true);
        }

        AzItemRendererPipelineContext context = (AzItemRendererPipelineContext) rendererPipeline.context();
        float partialTick = AzRenderTick.partialTicks();
        RenderType renderType = context.getDefaultRenderType(
            stack,
            config.textureLocation(context.currentEntity(), stack),
            source,
            partialTick,
            config.getRenderType(context.currentEntity(), stack),
            config.alpha(stack)
        );
        boolean withGlint = false; // Beta has no enchantments
        VertexConsumer buffer = AzBufferSource.getFoilBuffer(source, renderType, withGlint);

        poseStack.pushPose();
        rendererPipeline.render(poseStack, model, stack, source, renderType, buffer, 0, partialTick, packedLight);

        if (source instanceof AzBufferSource) {
            ((AzBufferSource) source).endBatch();
        }

        GlStateManager.enableDepth();
        if (entityLighting) {
            // Put back the GUI lights the hotbar/container set up, in the same space they were set in.
            setLightsInEyeSpace(false);
        }
        poseStack.popPose();
    }

    /**
     * Sets the item lights with an identity model-view so their direction doesn't depend on the item's GUI transform
     * (which includes RenderItem's Y flip and the display rotation).
     */
    private static void setLightsInEyeSpace(boolean entityStyle) {
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        if (entityStyle) {
            mod.azure.azurelib.render.AzLighting.enableStandardItemLighting();
        } else {
            mod.azure.azurelib.render.AzLighting.enableGUIStandardItemLighting();
        }
        GlStateManager.popMatrix();
    }
}
