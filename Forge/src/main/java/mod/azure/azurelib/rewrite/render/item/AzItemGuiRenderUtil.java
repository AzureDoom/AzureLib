package mod.azure.azurelib.rewrite.render.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mod.azure.azurelib.rewrite.model.AzBakedModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class AzItemGuiRenderUtil {

    /**
     * Wrapper method to handle rendering the item in a GUI context (defined by
     * {@link ItemCameraTransforms.TransformType#GUI} normally).<br>
     * Just includes some additional required transformations and settings.
     */
    public static void renderInGui(
        AzItemRendererConfig config,
        AzItemRendererPipeline rendererPipeline,
        ItemStack stack,
        AzBakedModel model,
        ItemStack currentItemStack,
        MatrixStack poseStack,
        IRenderTypeBuffer source,
        int packedLight
    ) {
        RenderHelper.setupGuiFlatDiffuseLighting();

        float partialTick = Minecraft.getInstance().getRenderPartialTicks();
        IRenderTypeBuffer.Impl bSource =
            source instanceof IRenderTypeBuffer.Impl
                ? ((IRenderTypeBuffer.Impl) source)
                : Minecraft.getInstance().worldRenderer.renderTypeTextures.getBufferSource();
        ResourceLocation textureLocation = config.textureLocation(stack);
        RenderType renderType = rendererPipeline.context()
            .getDefaultRenderType(stack, textureLocation, bSource, partialTick);
        boolean withGlint = currentItemStack != null && currentItemStack.hasEffect();
        IVertexBuilder buffer = ItemRenderer.getBuffer(source, renderType, true, withGlint);

        poseStack.push();

        rendererPipeline.render(poseStack, model, stack, bSource, renderType, buffer, 0, partialTick, packedLight);

        bSource.finish();
        RenderSystem.enableDepthTest();
        RenderHelper.setupGui3DDiffuseLighting();

        poseStack.pop();
    }
}
