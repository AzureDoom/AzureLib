package mod.azure.azurelib.rewrite.render;

import com.mojang.blaze3d.matrix.MatrixStack;

/**
 * Represents a phased renderer interface used as part of a rendering pipeline. Allows customization at specific stages
 * of the rendering process for animatable models. <br>
 * The interface provides two key methods, enabling actions to be taken before and after the core rendering operations
 * within the pipeline, specifically focusing on transforming and modifying render contexts. <br>
 * This is part of a flexible rendering system that enables complex rendering logic while maintaining separation of
 * concerns and modularity.
 *
 * @param <T> The type of animatable object being rendered.
 */
public interface AzPhasedRenderer<T> {

    /**
     * Called before rendering the model to buffer. Allows for render modifications and preparatory work such as scaling
     * and translating.<br>
     * {@link MatrixStack} translations made here are kept until the end of the render process
     */
    void preRender(AzRendererPipelineContext<T> context, boolean isReRender);

    /**
     * Called after rendering the model to buffer. Post-render modifications should be performed here.<br>
     * {@link MatrixStack} transformations will be unused and lost once this method ends
     */
    void postRender(AzRendererPipelineContext<T> context, boolean isReRender);
}
