/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.animatable.instance;

import com.google.common.base.Suppliers;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import mod.azure.azurelib.common.internal.client.RenderProvider;
import mod.azure.azurelib.common.internal.common.animatable.SingletonGeoAnimatable;
import mod.azure.azurelib.common.platform.Services;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.object.DataTicket;

/**
 * The base cache class responsible for returning the {@link AnimatableManager} for a given instanceof of a
 * {@link GeoAnimatable}. This class is abstracted and not intended for direct use. See either
 * {@link SingletonAnimatableInstanceCache} or {@link InstancedAnimatableInstanceCache}
 *
 * @deprecated
 */
@Deprecated(forRemoval = true)
public abstract class AnimatableInstanceCache {

    protected final GeoAnimatable animatable;

    protected final Supplier<RenderProvider> renderProvider;

    protected AnimatableInstanceCache(GeoAnimatable animatable) {
        this.animatable = animatable;
        this.renderProvider = Suppliers.memoize(() -> {
            if (
                !(this.animatable instanceof SingletonGeoAnimatable singleton) || !Services.PLATFORM
                    .isEnvironmentClient()
            )
                return null;

            final AtomicReference<RenderProvider> consumer = new AtomicReference<>(RenderProvider.DEFAULT);

            singleton.createRenderer(consumer::set);

            return consumer.get();
        });
    }

    /**
     * This creates or gets the cached animatable manager for any unique ID.<br>
     * For itemstacks, this is typically a reserved ID provided by AzureLib. {@code Entities} and {@code BlockEntities}
     * pass their position or int ID. They typically only have one {@link AnimatableManager} per cache anyway
     *
     * @param uniqueId A unique ID. For every ID the same animation manager will be returned.
     */
    public abstract <T extends GeoAnimatable> AnimatableManager<T> getManagerForId(long uniqueId);

    /**
     * Helper method to set a data point in the {@link AnimatableManager#setData manager} for this animatable.
     *
     * @param uniqueId   The unique identifier for this animatable instance
     * @param dataTicket The DataTicket for the data
     * @param data       The data to store
     */
    public <D> void addDataPoint(long uniqueId, DataTicket<D> dataTicket, D data) {
        getManagerForId(uniqueId).setData(dataTicket, data);
    }

    /**
     * Helper method to get a data point from the {@link AnimatableManager#getData data collection} for this animatable.
     *
     * @param uniqueId   The unique identifier for this animatable instance
     * @param dataTicket The DataTicket for the data
     */
    public <D> D getDataPoint(long uniqueId, DataTicket<D> dataTicket) {
        return getManagerForId(uniqueId).getData(dataTicket);
    }

    /**
     * Get the {@link RenderProvider} for this animatable
     * <p>
     * Because only {@link SingletonGeoAnimatable}s use this functionality, it this method should not be used and will
     * always return null for anything other than a SingletonGeoAnimatable
     * <p>
     * The returned object is upcast to Object for side-safety
     *
     * @return The cached GeoRenderProvider instance for this animatable, or null if one does not exist
     */
    public Supplier<RenderProvider> getRenderProvider() {
        return this.renderProvider;
    }
}
