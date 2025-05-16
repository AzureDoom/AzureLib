package mod.azure.azurelib.rewrite.render.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mod.azure.azurelib.rewrite.animation.impl.AzItemAnimator;
import mod.azure.azurelib.rewrite.model.AzBakedModel;
import mod.azure.azurelib.rewrite.render.AzProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * AzItemRenderer is an abstract base class for rendering custom animated items in a game framework. It provides
 * utilities for handling item models, textures, and animations via a configurable pipeline and provider system. This
 * class supports rendering of items both in GUI contexts and in-world as entities, enabling advanced visual effects
 * such as custom animations and lighting. <br>
 * The rendering process utilizes a pipeline to manage render layers, textures, and baked models, integrating with game
 * frame components like PoseStack and MultiBufferSource.
 */
public abstract class AzItemRenderer {

    private final AzItemRendererConfig config;

    private final AzProvider<ItemStack> provider;

    public final AzItemRendererPipeline rendererPipeline;

    private AzItemAnimator reusedAzItemAnimator;

    protected AzItemRenderer(
        AzItemRendererConfig config
    ) {
        this.rendererPipeline = createPipeline(config);
        this.provider = new AzProvider<>(config::createAnimator, config::modelLocation);
        this.config = config;
    }

    protected AzItemRendererPipeline createPipeline(AzItemRendererConfig config) {
        return new AzItemRendererPipeline(config, this);
    }

    public void renderByGui(
        ItemStack stack,
        MatrixStack poseStack,
        IRenderTypeBuffer source,
        int packedLight
    ) {
        AzBakedModel model = provider.provideBakedModel(stack);

        prepareAnimator(stack, model);

        AzItemGuiRenderUtil.renderInGui(config, rendererPipeline, stack, model, stack, poseStack, source, packedLight);
    }

    public void renderByItem(
        ItemStack stack,
        MatrixStack poseStack,
        IRenderTypeBuffer source,
        int packedLight
    ) {
        AzBakedModel model = provider.provideBakedModel(stack);
        float partialTick = Minecraft.getInstance().getRenderPartialTicks();
        ResourceLocation textureLocation = config.textureLocation(stack);
        RenderType renderType = rendererPipeline.context()
            .getDefaultRenderType(stack, textureLocation, source, partialTick);
        // TODO: Why the null check here?
        boolean withGlint = stack != null && stack.hasEffect();
        IVertexBuilder buffer = ItemRenderer.getBuffer(source, renderType, false, withGlint);

        prepareAnimator(stack, model);

        rendererPipeline.render(poseStack, model, stack, source, renderType, buffer, 0, partialTick, packedLight);
    }

    private void prepareAnimator(ItemStack stack, AzBakedModel model) {
        AzItemAnimator cachedEntityAnimator = (AzItemAnimator) provider.provideAnimator(stack);

        if (cachedEntityAnimator != null && model != null) {
            cachedEntityAnimator.setActiveModel(model);
        }

        // Point the renderer's current animator reference to the cached entity animator before rendering.
        reusedAzItemAnimator = cachedEntityAnimator;
    }

    public AzItemAnimator getAnimator() {
        return reusedAzItemAnimator;
    }

    public AzItemRendererConfig config() {
        return config;
    }
}
