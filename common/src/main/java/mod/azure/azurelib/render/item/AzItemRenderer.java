package mod.azure.azurelib.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import mod.azure.azurelib.AzureLib;
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

    private final AzProvider<UUID, ItemStack> provider;

    public final AzItemRendererPipeline rendererPipeline;

    @Nullable
    private AzItemAnimator reusedAzItemAnimator;

    protected AzItemRenderer(
        AzItemRendererConfig config
    ) {
        this.rendererPipeline = createPipeline(config);
        this.provider = new AzProvider<>(
            config::createAnimator,
            config::modelLocation,
            animator -> {
                if (animator.getTag() != null && animator.getTag().contains(AzureLib.ITEM_UUID_TAG)) {
                    animator.getTag().getUUID(AzureLib.ITEM_UUID_TAG);
                }
                return UUID.randomUUID();
            }
        );
        this.config = config;
    }

    protected AzItemRendererPipeline createPipeline(AzItemRendererConfig config) {
        return new AzItemRendererPipeline(config, this);
    }

    public void renderByGui(
        ItemStack stack,
        ItemDisplayContext transformType,
        @NotNull PoseStack poseStack,
        @NotNull MultiBufferSource source,
        int packedLight
    ) {
        var context = rendererPipeline.context();
        var model = provider.provideBakedModel(context.currentEntity(), stack);
        var itemContext = (AzItemRendererPipelineContext) context;

        itemContext.setTransformType(transformType);

        prepareAnimator(stack, model);

        AzItemGuiRenderUtil.renderInGui(config, rendererPipeline, stack, model, stack, poseStack, source, packedLight);
    }

    public void renderByItem(
        ItemStack stack,
        ItemDisplayContext transformType,
        @NotNull PoseStack poseStack,
        @NotNull MultiBufferSource source,
        int packedLight
    ) {
        var context = rendererPipeline.context();
        var model = provider.provideBakedModel(null, stack);
        var partialTick = Minecraft.getInstance().getFrameTime();
        var textureLocation = config.textureLocation(context.currentEntity(), stack);
        var renderType = rendererPipeline.context()
            .getDefaultRenderType(
                stack,
                textureLocation,
                source,
                partialTick,
                config.getRenderType(context.currentEntity(), stack),
                config.alpha(stack)
            );
        // TODO: Why the null check here?
        var withGlint = stack != null && stack.hasFoil();
        var buffer = ItemRenderer.getFoilBufferDirect(source, renderType, false, withGlint);
        var itemContext = (AzItemRendererPipelineContext) context;

        itemContext.setTransformType(transformType);
        prepareAnimator(stack, model);

        rendererPipeline.render(poseStack, model, stack, source, renderType, buffer, 0, partialTick, packedLight);
    }

    private void prepareAnimator(ItemStack stack, AzBakedModel model) {
        // Point the renderer's current animator reference to the cached entity animator before rendering.
        reusedAzItemAnimator = (AzItemAnimator) provider.provideAnimator(
            rendererPipeline.context().currentEntity(),
            stack
        );
    }

    public @Nullable AzItemAnimator getAnimator() {
        return reusedAzItemAnimator;
    }

    public AzItemRendererConfig config() {
        return config;
    }
}
