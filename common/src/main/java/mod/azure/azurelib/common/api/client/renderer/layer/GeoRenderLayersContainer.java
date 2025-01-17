/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.api.client.renderer.layer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

import mod.azure.azurelib.common.internal.client.renderer.GeoRenderer;
import mod.azure.azurelib.core.animatable.GeoAnimatable;

/**
 * Base interface for a container for {@link GeoRenderLayer GeoRenderLayers}<br>
 * Each renderer should contain an instance of this, for holding its layers and handling events.
 */
@Deprecated(forRemoval = true)
public class GeoRenderLayersContainer<T extends GeoAnimatable> {

    private final GeoRenderer<T> renderer;

    private final List<GeoRenderLayer<T>> layers = new ObjectArrayList<>();

    private boolean compiledLayers = false;

    public GeoRenderLayersContainer(GeoRenderer<T> renderer) {
        this.renderer = renderer;
    }

    /**
     * Get the {@link GeoRenderLayer} list for usage
     */
    public List<GeoRenderLayer<T>> getRenderLayers() {
        if (!this.compiledLayers)
            fireCompileRenderLayersEvent();

        return this.layers;
    }

    /**
     * Add a new render layer to the container
     */
    public void addLayer(GeoRenderLayer<T> layer) {
        this.layers.add(layer);
    }

    /**
     * Create and fire the relevant {@code CompileRenderLayers} event hook for the owning renderer
     */
    public void fireCompileRenderLayersEvent() {
        this.compiledLayers = true;

        this.renderer.fireCompileRenderLayersEvent();
    }
}
