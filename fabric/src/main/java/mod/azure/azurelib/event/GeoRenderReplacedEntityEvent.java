package mod.azure.azurelib.event;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.renderer.GeoRenderer;
import mod.azure.azurelib.renderer.GeoReplacedEntityRenderer;
import mod.azure.azurelib.renderer.layer.GeoRenderLayer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Renderer events for miscellaneous {@link mod.azure.azurelib.animatable.GeoReplacedEntity replaced entities} being rendered by {@link GeoReplacedEntityRenderer}
 */
abstract class GeoRenderReplacedEntityEvent implements GeoRenderEvent {
	private final GeoReplacedEntityRenderer<?, ?> renderer;

	public GeoRenderReplacedEntityEvent(GeoReplacedEntityRenderer<?, ?> renderer) {
		this.renderer = renderer;
	}

	/**
	 * Returns the renderer for this event
	 */
	@Override
	public GeoReplacedEntityRenderer<?, ?> getRenderer() {
		return this.renderer;
	}

	/**
	 * Shortcut method to get the Entity currently being rendered
	 */
	public net.minecraft.world.entity.Entity getReplacedEntity() {
		return getRenderer().getCurrentEntity();
	}

	/**
	 * Pre-render event for replaced entities being rendered by {@link GeoReplacedEntityRenderer<?, ?>}.<br>
	 * This event is called before rendering, but after {@link GeoRenderer#preRender}<br>
	 * <br>
	 * This event is <u>cancellable</u>.<br>
	 * If the event is cancelled by returning false in the {@link Listener}, the entity will not be rendered and the corresponding {@link Post} event will not be fired.
	 */
	public static class Pre extends GeoRenderReplacedEntityEvent {
		public static final Event<Listener> EVENT = EventFactory.createArrayBacked(Listener.class, event -> true, listeners -> event -> {
			for (Listener listener : listeners) {
				if (!listener.handle(event))
					return false;
			}

			return true;
		});

		private final PoseStack poseStack;
		private final BakedGeoModel model;
		private final MultiBufferSource bufferSource;
		private final float partialTick;
		private final int packedLight;

		public Pre(GeoReplacedEntityRenderer<?, ?> renderer, PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {
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
		 * Event listener interface for the ReplacedEntity.Pre GeoRenderEvent.<br>
		 * Return false to cancel the render pass
		 */
		@FunctionalInterface
		public interface Listener {
			boolean handle(Pre event);
		}
	}

	/**
	 * Post-render event for replaced entities being rendered by {@link GeoReplacedEntityRenderer}.<br>
	 * This event is called after {@link GeoRenderer#postRender}
	 */
	public static class Post extends GeoRenderReplacedEntityEvent {
		public static final Event<Listener> EVENT = EventFactory.createArrayBacked(Listener.class, post -> {}, listeners -> event -> {
			for (Listener listener : listeners) {
				listener.handle(event);
			}
		});

		private final PoseStack poseStack;
		private final BakedGeoModel model;
		private final MultiBufferSource bufferSource;
		private final float partialTick;
		private final int packedLight;

		public Post(GeoReplacedEntityRenderer<?, ?> renderer, PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {
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
		 * Event listener interface for the ReplacedEntity.Post GeoRenderEvent
		 */
		@FunctionalInterface
		public interface Listener {
			void handle(Post event);
		}
	}

	/**
	 * One-time event for a {@link GeoReplacedEntityRenderer} called on first initialisation.<br>
	 * Use this event to add render layers to the renderer as needed
	 */
	public static class CompileRenderLayers extends GeoRenderReplacedEntityEvent {
		public static final Event<Listener> EVENT = EventFactory.createArrayBacked(Listener.class, post -> {}, listeners -> event -> {
			for (Listener listener : listeners) {
				listener.handle(event);
			}
		});

		public CompileRenderLayers(GeoReplacedEntityRenderer<?, ?> renderer) {
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
		 * Event listener interface for the ReplacedEntity.CompileRenderLayers GeoRenderEvent
		 */
		@FunctionalInterface
		public interface Listener {
			void handle(CompileRenderLayers event);
		}
	}
}