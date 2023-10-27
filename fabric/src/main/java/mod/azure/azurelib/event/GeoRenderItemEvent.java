package mod.azure.azurelib.event;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.renderer.GeoItemRenderer;
import mod.azure.azurelib.renderer.GeoRenderer;
import mod.azure.azurelib.renderer.layer.GeoRenderLayer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;

/**
 * Renderer events for {@link ItemStack Items} being rendered by {@link GeoItemRenderer}
 */
public abstract class GeoRenderItemEvent implements GeoRenderEvent {
	private final GeoItemRenderer<?> renderer;

	public GeoRenderItemEvent(GeoItemRenderer<?> renderer) {
		this.renderer = renderer;
	}

	/**
	 * Returns the renderer for this event
	 */
	@Override
	public GeoItemRenderer<?> getRenderer() {
		return this.renderer;
	}

	/**
	 * Shortcut method for retrieving the ItemStack being rendered
	 */
	public ItemStack getItemStack() {
		return getRenderer().getCurrentItemStack();
	}

	/**
	 * Pre-render event for armor being rendered by {@link GeoItemRenderer}.<br>
	 * This event is called before rendering, but after {@link GeoRenderer#preRender}<br>
	 * <br>
	 * This event is <u>cancellable</u>.<br>
	 * If the event is cancelled by returning false in the {@link Listener}, the ItemStack will not be rendered and the corresponding {@link Post} event will not be fired.
	 */
	public static class Pre extends GeoRenderItemEvent {
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

		public Pre(GeoItemRenderer<?> renderer, PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {
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
		 * Event listener interface for the Item.Pre GeoRenderEvent.<br>
		 * Return false to cancel the render pass
		 */
		@FunctionalInterface
		public interface Listener {
			boolean handle(Pre event);
		}
	}

	/**
	 * Post-render event for ItemStacks being rendered by {@link GeoItemRenderer}.<br>
	 * This event is called after {@link GeoRenderer#postRender}
	 */
	public static class Post extends GeoRenderItemEvent {
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

		public Post(GeoItemRenderer<?> renderer, PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {
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
		 * Event listener interface for the Item.Post GeoRenderEvent
		 */
		@FunctionalInterface
		public interface Listener {
			void handle(Post event);
		}
	}

	/**
	 * One-time event for a {@link GeoItemRenderer} called on first initialisation.<br>
	 * Use this event to add render layers to the renderer as needed
	 */
	public static class CompileRenderLayers extends GeoRenderItemEvent {
		public static final Event<Listener> EVENT = EventFactory.createArrayBacked(Listener.class, post -> {}, listeners -> event -> {
			for (Listener listener : listeners) {
				listener.handle(event);
			}
		});

		public CompileRenderLayers(GeoItemRenderer<?> renderer) {
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
		 * Event listener interface for the Item.CompileRenderLayers GeoRenderEvent
		 */
		@FunctionalInterface
		public interface Listener {
			void handle(CompileRenderLayers event);
		}
	}
}