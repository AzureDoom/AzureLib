package mod.azure.azurelib.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.animation.impl.AzItemAnimator;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.render.AzProvider;

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

    @Nullable
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
        @NotNull PoseStack poseStack,
        @NotNull MultiBufferSource source,
        int packedLight
    ) {
        @Nullable
        AzBakedModel model = provider.provideBakedModel(stack);

        prepareAnimator(stack, model);

        AzItemGuiRenderUtil.renderInGui(config, rendererPipeline, stack, model, stack, poseStack, source, packedLight);
    }

    public void renderByItem(
        ItemStack stack,
        @NotNull PoseStack poseStack,
        @NotNull MultiBufferSource source,
        int packedLight
    ) {
        AzBakedModel model = provider.provideBakedModel(stack);
        float partialTick = Minecraft.getInstance().getFrameTime();
        ResourceLocation textureLocation = config.textureLocation(stack);
        RenderType renderType = rendererPipeline.context()
            .getDefaultRenderType(stack, textureLocation, source, partialTick);
        // TODO: Why the null check here?
        boolean withGlint = stack != null && stack.hasFoil();
        VertexConsumer buffer = ItemRenderer.getFoilBuffer(source, renderType, false, withGlint);

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

    public @Nullable AzItemAnimator getAnimator() {
        return reusedAzItemAnimator;
    }

    public AzItemRendererConfig config() {
        return config;
    }
}
