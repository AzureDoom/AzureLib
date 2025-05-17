package mod.azure.azurelib.render.item;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import mod.azure.azurelib.model.AzBakedModel;

public class AzItemGuiRenderUtil {

    /**
     * Wrapper method to handle rendering the item in a GUI context (defined by {@link ItemTransforms.TransformType#GUI}
     * normally).<br>
     * Just includes some additional required transformations and settings.
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
        Lighting.setupForFlatItems();

        float partialTick = Minecraft.getInstance().getFrameTime();
        MultiBufferSource.BufferSource bSource =
            source instanceof MultiBufferSource.BufferSource
                ? ((MultiBufferSource.BufferSource) source)
                : Minecraft.getInstance().levelRenderer.renderBuffers.bufferSource();
        ResourceLocation textureLocation = config.textureLocation(stack);
        RenderType renderType = rendererPipeline.context()
            .getDefaultRenderType(stack, textureLocation, bSource, partialTick);
        boolean withGlint = currentItemStack != null && currentItemStack.hasFoil();
        VertexConsumer buffer = ItemRenderer.getFoilBuffer(source, renderType, true, withGlint);

        poseStack.pushPose();

        rendererPipeline.render(poseStack, model, stack, bSource, renderType, buffer, 0, partialTick, packedLight);

        bSource.endBatch();
        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();

        poseStack.popPose();
    }
}
