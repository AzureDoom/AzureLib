package mod.azure.azurelib.common.api.common.event;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.azure.azurelib.common.api.client.renderer.DynamicGeoEntityRenderer;
import mod.azure.azurelib.common.api.client.renderer.GeoEntityRenderer;
import mod.azure.azurelib.common.api.client.renderer.layer.GeoRenderLayer;
import mod.azure.azurelib.common.internal.common.cache.object.BakedGeoModel;
import mod.azure.azurelib.common.internal.common.event.GeoRenderEvent;
import mod.azure.azurelib.common.platform.Services;
import mod.azure.azurelib.common.platform.services.GeoRenderPhaseEventFactory;
import mod.azure.azurelib.common.internal.client.renderer.GeoRenderer;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Renderer events for {@link net.minecraft.world.entity.Entity Entities} being rendered by {@link GeoEntityRenderer}, as well as
 * {@link DynamicGeoEntityRenderer DynamicGeoEntityRenderer}
 */
public abstract class GeoRenderEntityEvent implements GeoRenderEvent {
	private final GeoEntityRenderer<?> renderer;

	public GeoRenderEntityEvent(GeoEntityRenderer<?> renderer) {
		this.renderer = renderer;
	}

	/**
	 * Returns the renderer for this event
	 */
	@Override
	public GeoEntityRenderer<?> getRenderer() {
		return this.renderer;
	}

	/**
	 * Shortcut method for retrieving the entity being rendered
	 */
	public net.minecraft.world.entity.Entity getEntity() {
		return this.renderer.getAnimatable();
	}

	/**
	 * Pre-render event for entities being rendered by {@link GeoEntityRenderer}.<br>
	 * This event is called before rendering, but after {@link GeoRenderer#preRender}<br>
	 * <br>
	 * This event is <u>cancellable</u>.<br>
	 * If the event is cancelled by returning false in the {@link Listener}, the entity will not be rendered and the corresponding {@link Post} event will not be fired.
	 */
	public static class Pre extends GeoRenderEntityEvent {
		public static final GeoRenderPhaseEventFactory.GeoRenderPhaseEvent EVENT  = Services.GEO_RENDER_PHASE_EVENT_FACTORY.create();

		private final PoseStack poseStack;
		private final BakedGeoModel model;
		private final MultiBufferSource bufferSource;
		private final float partialTick;
		private final int packedLight;

		public Pre(GeoEntityRenderer<?> renderer, PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {
			super(renderer);

			this.poseStack = poseStack;
			this.model = model;
			this.bufferSource = bufferSource;
			this.partialTick = partialTick;
			this.packedLight = packedLight;
		}

		public PoseStack getPoseStack() {
			return this.poseStack;
		}

		public BakedGeoModel getModel() {
			return this.model;
		}

		public MultiBufferSource getBufferSource() {
			return this.bufferSource;
		}

		public float getPartialTick() {
			return this.partialTick;
		}

		public int getPackedLight() {
			return this.packedLight;
		}

		/**
		 * Event listener interface for the Armor.Pre GeoRenderEvent.<br>
		 * Return false to cancel the render pass
		 */
		@FunctionalInterface
		public interface Listener {
			boolean handle(Pre event);
		}
	}

	/**
	 * Post-render event for entities being rendered by {@link GeoEntityRenderer}.<br>
	 * This event is called after {@link GeoRenderer#postRender}
	 */
	public static class Post extends GeoRenderEntityEvent {
		public static final GeoRenderPhaseEventFactory.GeoRenderPhaseEvent EVENT  = Services.GEO_RENDER_PHASE_EVENT_FACTORY.create();

		private final PoseStack poseStack;
		private final BakedGeoModel model;
		private final MultiBufferSource bufferSource;
		private final float partialTick;
		private final int packedLight;

		public Post(GeoEntityRenderer<?> renderer, PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {
			super(renderer);

			this.poseStack = poseStack;
			this.model = model;
			this.bufferSource = bufferSource;
			this.partialTick = partialTick;
			this.packedLight = packedLight;
		}

		public PoseStack getPoseStack() {
			return this.poseStack;
		}

		public BakedGeoModel getModel() {
			return this.model;
		}

		public MultiBufferSource getBufferSource() {
			return this.bufferSource;
		}

		public float getPartialTick() {
			return this.partialTick;
		}

		public int getPackedLight() {
			return this.packedLight;
		}

		/**
		 * Event listener interface for the Entity.Post GeoRenderEvent
		 */
		@FunctionalInterface
		public interface Listener {
			void handle(Post event);
		}
	}

	/**
	 * One-time event for a {@link GeoEntityRenderer} called on first initialisation.<br>
	 * Use this event to add render layers to the renderer as needed
	 */
	public static class CompileRenderLayers extends GeoRenderEntityEvent {
		public static final GeoRenderPhaseEventFactory.GeoRenderPhaseEvent EVENT  = Services.GEO_RENDER_PHASE_EVENT_FACTORY.create();

		public CompileRenderLayers(GeoEntityRenderer<?> renderer) {
			super(renderer);
		}

		/**
		 * Adds a {@link GeoRenderLayer} to the renderer.<br>
		 * Type-safety is not checked here, so ensure that your layer is compatible with this animatable and renderer
		 */
		public void addLayer(GeoRenderLayer renderLayer) {
			getRenderer().addRenderLayer(renderLayer);
		}

		/**
		 * Event listener interface for the Entity.CompileRenderLayers GeoRenderEvent
		 */
		@FunctionalInterface
		public interface Listener {
			void handle(CompileRenderLayers event);
		}
	}
}