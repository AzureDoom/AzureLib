package mod.azure.azurelib.render;

import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Represents a phased renderer interface used as part of a rendering pipeline. Allows customization at specific stages
 * of the rendering process for animatable models. <br>
 * The interface provides two key methods, enabling actions to be taken before and after the core rendering operations
 * within the pipeline, specifically focusing on transforming and modifying render contexts. <br>
 * This is part of a flexible rendering system that enables complex rendering logic while maintaining separation of
 * concerns and modularity.
 *
 * @param <K> The type of the key used to identify the animatable object. Typically, a UUID for items/entities and Long
 *            for BlockEntities.
 * @param <T> The type of animatable object being rendered.
 */
public interface AzPhasedRenderer<K, T> {

    /**
     * Called before rendering the model to buffer. Allows for render modifications and preparatory work such as scaling
     * and translating.<br>
     * {@link PoseStack} translations made here are kept until the end of the render process
     */
    void preRender(AzRendererPipelineContext<K, T> context, boolean isReRender);

    /**
     * Called after rendering the model to buffer. Post-render modifications should be performed here.<br>
     * {@link PoseStack} transformations will be unused and lost once this method ends
     */
    void postRender(AzRendererPipelineContext<K, T> context, boolean isReRender);
}
