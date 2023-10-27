package mod.azure.azurelib.event;

import mod.azure.azurelib.renderer.GeoRenderer;

/**
 * AzureLib events base-class for the various event stages of rendering.<br>
 */
public interface GeoRenderEvent {
	/**
	 * Returns the renderer for this event
	 * @see mod.azure.azurelib.renderer.DynamicGeoEntityRenderer DynamicGeoEntityRenderer
	 * @see mod.azure.azurelib.renderer.GeoArmorRenderer GeoArmorRenderer
	 * @see mod.azure.azurelib.renderer.GeoBlockRenderer GeoBlockRenderer
	 * @see mod.azure.azurelib.renderer.GeoEntityRenderer GeoEntityRenderer
	 * @see mod.azure.azurelib.animatable.GeoItem GeoItem
	 * @see mod.azure.azurelib.renderer.GeoObjectRenderer GeoObjectRenderer
	 * @see mod.azure.azurelib.renderer.GeoReplacedEntityRenderer GeoReplacedEntityRenderer
	 */
	GeoRenderer<?> getRenderer();
}
