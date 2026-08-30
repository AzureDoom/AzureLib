package mod.azure.azurelib.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.render.AzBufferSource;

public class AzItemGuiRenderUtil {

    /**
     * Wrapper method to handle rendering the item in a GUI context (defined by
     * {@link net.minecraft.world.item.ItemDisplayContext#GUI} normally).<br>
     * Just includes some additional required transformations and settings.
     * <p>
     * NOTE: the lighting setup ({@code Lighting.setupForEntityInInventory}/{@code setupForFlatItems}/
     * {@code setupFor3DItems}) and batch-flush/depth-test calls this used to make around the render call were dropped
     * here, since {@link AzBufferSource} is a deferred-submit recorder, not a real, immediately-flushable
     * {@code MultiBufferSource.BufferSource} — there's nothing to {@code endBatch()} at this point, the actual
     * submission happens later. Whatever now calls into item-in-GUI rendering under 26.2's render-state model needs to
     * own that lighting/depth-state setup itself; this needs verifying against the real call site.
     */
    public static void renderInGui(
        AzItemRendererConfig config,
        AzItemRendererPipeline rendererPipeline,
        ItemStack stack,
        AzBakedModel model,
        ItemStack currentItemStack,
        PoseStack poseStack,
        AzBufferSource source,
        int packedLight
    ) {
        var context = rendererPipeline.context();
        var partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();
        var textureLocation = config.textureLocation(context.currentEntity(), stack);
        var renderType = context.getDefaultRenderType(
            stack,
            textureLocation,
            source,
            partialTick,
            config.getRenderType(context.currentEntity(), stack),
            config.alpha(stack)
        );

        poseStack.pushPose();

        rendererPipeline.render(poseStack, model, stack, source, renderType, null, 0, partialTick, packedLight);

        poseStack.popPose();
    }
}
