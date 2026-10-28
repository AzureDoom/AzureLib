package mod.azure.azurelib.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.impl.AzItemAnimator;
import mod.azure.azurelib.animation.impl.AzItemAnimatorView;
import mod.azure.azurelib.animation.molang.AzMolangQueryContext;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.render.AzBufferSource;
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
            animator -> animator.get(
                AzureLib.AZ_ID.get()
            )
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
        @NotNull AzBufferSource source,
        int packedLight
    ) {
        // Everything below that looks up the stack's animator (provider, layers, model renderer) resolves to the
        // animator for this display context's view.
        var previousView = AzItemAnimatorView.push(config.animatorView(transformType));
        var previousDisplay = AzMolangQueryContext.INSTANCE.setItemDisplayContext(transformType);

        try {
            var context = rendererPipeline.context();
            var model = provider.provideBakedModel(context.currentEntity(), stack);
            var itemContext = (AzItemRendererPipelineContext) context;

            itemContext.setTransformType(transformType);

            prepareAnimator(stack, model);

            AzItemGuiRenderUtil.renderInGui(
                config,
                rendererPipeline,
                stack,
                model,
                stack,
                poseStack,
                source,
                packedLight
            );
        } finally {
            AzMolangQueryContext.INSTANCE.setItemDisplayContext(previousDisplay);
            AzItemAnimatorView.restore(previousView);
        }
    }

    public void renderByItem(
        ItemStack stack,
        ItemDisplayContext transformType,
        @NotNull PoseStack poseStack,
        @NotNull AzBufferSource source,
        int packedLight
    ) {
        // Everything below that looks up the stack's animator (provider, layers, model renderer) resolves to the
        // animator for this display context's view.
        var previousView = AzItemAnimatorView.push(config.animatorView(transformType));
        // Lets Molang answer query.is_first_person for the item's own view.
        var previousDisplay = AzMolangQueryContext.INSTANCE.setItemDisplayContext(transformType);

        try {
            renderByItemInView(stack, transformType, poseStack, source, packedLight);
        } finally {
            AzMolangQueryContext.INSTANCE.setItemDisplayContext(previousDisplay);
            AzItemAnimatorView.restore(previousView);
        }
    }

    private void renderByItemInView(
        ItemStack stack,
        ItemDisplayContext transformType,
        @NotNull PoseStack poseStack,
        @NotNull AzBufferSource source,
        int packedLight
    ) {
        var context = rendererPipeline.context();
        var model = provider.provideBakedModel(context.currentEntity(), stack);
        var partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();
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
        var itemContext = (AzItemRendererPipelineContext) context;

        itemContext.setTransformType(transformType);

        prepareAnimator(stack, model);

        rendererPipeline.render(poseStack, model, stack, source, renderType, null, 0, partialTick, packedLight);
    }

    public void renderSpecial(
        ItemStack stack,
        PoseStack poseStack,
        AzBufferSource source,
        int packedLight,
        int packedOverlay
    ) {
        var transformType = AzItemDisplayContextCapture.current();
        // Everything below that looks up the stack's animator (provider, layers, model renderer) resolves to the
        // animator for this display context's view.
        var previousView = AzItemAnimatorView.push(config.animatorView(transformType));
        // Lets Molang answer query.is_first_person for the item's own view.
        var previousDisplay = AzMolangQueryContext.INSTANCE.setItemDisplayContext(transformType);

        try {
            renderSpecialInView(stack, transformType, poseStack, source, packedLight, packedOverlay);
        } finally {
            AzMolangQueryContext.INSTANCE.setItemDisplayContext(previousDisplay);
            AzItemAnimatorView.restore(previousView);
        }
    }

    private void renderSpecialInView(
        ItemStack stack,
        ItemDisplayContext transformType,
        PoseStack poseStack,
        AzBufferSource source,
        int packedLight,
        int packedOverlay
    ) {
        var context = rendererPipeline.context();
        var itemContext =
            (AzItemRendererPipelineContext) context;

        itemContext.setTransformType(transformType);

        var model = provider.provideBakedModel(
            context.currentEntity(),
            stack
        );

        var partialTick = Minecraft.getInstance()
            .getDeltaTracker()
            .getGameTimeDeltaTicks();

        var textureLocation = config.textureLocation(
            context.currentEntity(),
            stack
        );

        var renderType = context.getDefaultRenderType(
            stack,
            textureLocation,
            source,
            partialTick,
            config.getRenderType(
                context.currentEntity(),
                stack
            ),
            config.alpha(stack)
        );

        prepareAnimator(stack, model);

        rendererPipeline.render(
            poseStack,
            model,
            stack,
            source,
            renderType,
            null,
            packedOverlay,
            partialTick,
            packedLight
        );
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
