package mod.azure.azurelib.render.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.render.vertex.AzBufferSource;
import mod.azure.azurelib.render.vertex.MultiBufferSource;
import mod.azure.azurelib.render.vertex.PoseStack;
import mod.azure.azurelib.render.vertex.RenderType;
import mod.azure.azurelib.render.vertex.VertexConsumer;

/**
 * Utility for rendering items in a GUI context.
 */
public class AzItemGuiRenderUtil {

    /**
     * Renders an item in the GUI. Items configured with {@code useEntityGuiLighting()} get world-style entity lighting;
     * everything else keeps 1.12.2's standard GUI item lighting.
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
        float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();
        RenderType renderType = context.getDefaultRenderType(
            stack,
            config.textureLocation(context.currentEntity(), stack),
            source,
            partialTick,
            config.getRenderType(context.currentEntity(), stack),
            config.alpha(stack)
        );
        boolean withGlint = currentItemStack != null && currentItemStack.hasEffect();
        VertexConsumer buffer = AzBufferSource.getFoilBuffer(source, renderType, withGlint);

        poseStack.pushPose();
        rendererPipeline.render(poseStack, model, stack, source, renderType, buffer, 0, partialTick, packedLight);

        if (source instanceof AzBufferSource) {
            ((AzBufferSource) source).endBatch();
        }

        restoreGuiItemState();
        if (entityLighting) {
            // Put back the GUI lights the hotbar/container set up, in the same space they were set in.
            setLightsInEyeSpace(false);
        }
        poseStack.popPose();
    }

    /**
     * Puts GL state back to what {@code RenderItem#renderItemModelIntoGUI} set up before handing off to us.
     * {@link RenderType#clearRenderState()} leaves blend disabled, but vanilla leaves it <em>enabled</em> after a GUI
     * item, and later GUI code relies on that: {@code InventoryEffectRenderer#drawActivePotionEffects} draws with alpha
     * test off and never enables blend itself, so with blend off every transparent texel of the effect background is
     * drawn as an opaque black box. It only shows up with the cursor-held stack because that is the last item drawn
     * before the potion effects, with nothing in between to re-enable blend.
     */
    private static void restoreGuiItemState() {
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        // The color array leaves the current GL colour undefined; force a real glColor rather than a cached no-op.
        GlStateManager.resetColor();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        // Vanilla binds the block atlas again after us, but don't leave our model texture bound in the meantime.
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
    }

    /**
     * Sets the item lights with an identity model-view so their direction doesn't depend on the item's GUI transform
     * (which includes RenderItem's Y flip and the display rotation).
     */
    private static void setLightsInEyeSpace(boolean entityStyle) {
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        if (entityStyle) {
            RenderHelper.enableStandardItemLighting();
        } else {
            RenderHelper.enableGUIStandardItemLighting();
        }
        GlStateManager.popMatrix();
    }
}
