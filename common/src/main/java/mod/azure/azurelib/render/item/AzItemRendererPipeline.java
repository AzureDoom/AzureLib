package mod.azure.azurelib.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import java.util.UUID;
import java.util.stream.Stream;

import mod.azure.azurelib.cache.texture.AnimatableTexture;
import mod.azure.azurelib.render.AzLayerRenderer;
import mod.azure.azurelib.render.AzRendererConfig;
import mod.azure.azurelib.render.AzRendererPipeline;
import mod.azure.azurelib.render.AzRendererPipelineContext;

/**
 * Extends the {@link AzRendererPipeline} to provide a specific implementation for rendering {@link ItemStack} objects.
 * This pipeline includes methods and configurations designed for item rendering and leverages additional utilities such
 * as translation matrices and scaling functionalities for accurate rendering.
 */
public class AzItemRendererPipeline extends AzRendererPipeline<UUID, ItemStack> {

    private final AzItemRenderer itemRenderer;

    protected Matrix4f itemRenderTranslations = new Matrix4f();

    protected Matrix4f modelRenderTranslations = new Matrix4f();

    public AzItemRendererPipeline(AzItemRendererConfig config, AzItemRenderer itemRenderer) {
        super(config);
        this.itemRenderer = itemRenderer;
    }

    @Override
    protected AzRendererPipelineContext<UUID, ItemStack> createContext(
        AzRendererPipeline<UUID, ItemStack> rendererPipeline
    ) {
        return config.pipelineContext(this);
    }

    @Override
    protected AzItemModelRenderer createModelRenderer(AzLayerRenderer<UUID, ItemStack> layerRenderer) {
        return (AzItemModelRenderer) config.modelRendererProvider(this, layerRenderer);
    }

    @Override
    protected AzLayerRenderer<UUID, ItemStack> createLayerRenderer(AzRendererConfig<UUID, ItemStack> config) {
        return new AzLayerRenderer<>(config::renderLayers);
    }

    /**
     * Called before rendering the model to buffer. Allows for render modifications and preparatory work such as scaling
     * and translating.<br>
     * {@link PoseStack} translations made here are kept until the end of the render process
     */
    @Override
    public void preRender(AzRendererPipelineContext<UUID, ItemStack> context, boolean isReRender) {
        var itemContext = (AzItemRendererPipelineContext) context;
        var poseStack = context.poseStack();
        this.itemRenderTranslations = new Matrix4f(poseStack.last().pose());

        var config = itemRenderer.config();
        var scaleWidth = config.scaleWidth(context.animatable());
        var scaleHeight = config.scaleHeight(context.animatable());
        scaleModelForRender(context, scaleWidth, scaleHeight, isReRender);

        if (!isReRender) {
            var useNewOffset = config.useNewOffset();
            poseStack.translate(0.5f, useNewOffset ? 0.0f : 0.51f, 0.5f);
        }

        // If the item model has the leftArm or rightArm bone, hide them.
        Stream.of("leftArm", "rightArm")
            .forEach(
                boneName -> context
                    .bakedModel()
                    .getBone(boneName)
                    .ifPresent(bone -> {
                        bone.setHidden(true);
                        bone.setChildrenHidden(false);
                    })
            );

        if (config.alpha(context.animatable()) < 1) {
            itemContext.setAlpha(config.alpha(context.animatable()));
            itemContext.setTranslucent(true);
        }
        config.preRenderEntry(context);
    }

    @Override
    public void postRender(AzRendererPipelineContext<UUID, ItemStack> context, boolean isReRender) {
        config.postRenderEntry(context);
    }

    /**
     * Update the current frame of a {@link AnimatableTexture potentially animated} texture used by this
     * GeoRenderer.<br>
     * This should only be called immediately prior to rendering, and only
     *
     * @see AnimatableTexture#setAndUpdate(ResourceLocation, int)
     */
    @Override
    public void updateAnimatedTextureFrame(ItemStack animatable) {
        AnimatableTexture.setAndUpdate(config.textureLocation(context().currentEntity(), animatable));
    }

    public AzItemRenderer getRenderer() {
        return itemRenderer;
    }
}
