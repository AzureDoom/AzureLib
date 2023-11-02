package mod.azure.azurelib.common.internal.common.event;

import mod.azure.azurelib.common.api.client.renderer.*;
import mod.azure.azurelib.common.api.common.animatable.GeoItem;
import mod.azure.azurelib.common.internal.client.renderer.GeoRenderer;

/**
 * AzureLib events base-class for the various event stages of rendering.<br>
 */
public interface GeoRenderEvent {
	/**
	 * Returns the renderer for this event
	 * @see DynamicGeoEntityRenderer DynamicGeoEntityRenderer
	 * @see GeoArmorRenderer GeoArmorRenderer
	 * @see GeoBlockRenderer GeoBlockRenderer
	 * @see GeoEntityRenderer GeoEntityRenderer
	 * @see GeoItem GeoItem
	 * @see GeoObjectRenderer GeoObjectRenderer
	 * @see GeoReplacedEntityRenderer GeoReplacedEntityRenderer
	 */
	GeoRenderer<?> getRenderer();
}
