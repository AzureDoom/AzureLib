package mod.azure.azurelib.event;

import com.mojang.blaze3d.vertex.PoseStack;

import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.renderer.GeoObjectRenderer;
import mod.azure.azurelib.renderer.GeoRenderer;
import mod.azure.azurelib.renderer.GeoReplacedEntityRenderer;
import mod.azure.azurelib.renderer.layer.GeoRenderLayer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.renderer.MultiBufferSource;

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
