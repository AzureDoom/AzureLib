package mod.azure.azurelib.common.api.common.event;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.azure.azurelib.common.api.client.renderer.GeoArmorRenderer;
import mod.azure.azurelib.common.api.client.renderer.GeoEntityRenderer;
import mod.azure.azurelib.common.api.client.renderer.layer.GeoRenderLayer;
import mod.azure.azurelib.common.internal.common.cache.object.BakedGeoModel;
import mod.azure.azurelib.common.internal.common.event.GeoRenderEvent;
import mod.azure.azurelib.common.platform.Services;
import mod.azure.azurelib.common.platform.services.GeoRenderPhaseEventFactory;
import mod.azure.azurelib.common.internal.client.renderer.GeoRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Renderer events for armor pieces being rendered by {@link GeoArmorRenderer}
 */
public abstract class GeoRenderArmorEvent implements GeoRenderEvent {
		private final GeoArmorRenderer<?> renderer;

		public GeoRenderArmorEvent(GeoArmorRenderer<?> renderer) {
			this.renderer = renderer;
		}

		/**
		 * Returns the renderer for this event
		 */
		@Override
		public GeoArmorRenderer<?> getRenderer() {
			return this.renderer;
		}

		/**
		 * Shortcut method for retrieving the entity being rendered
		 */
		@Nullable
		public net.minecraft.world.entity.Entity getEntity() {
			return getRenderer().getCurrentEntity();
		}

		/**
		 * Shortcut method for retrieving the ItemStack relevant to the armor piece being rendered
		 */
		@Nullable
		public ItemStack getItemStack() {
			return getRenderer().getCurrentStack();
		}

		/**
		 * Shortcut method for retrieving the equipped slot of the armor piece being rendered
		 */
		@Nullable
		public EquipmentSlot getEquipmentSlot() {
			return getRenderer().getCurrentSlot();
		}

		/**
		 * Pre-render event for armor pieces being rendered by {@link GeoArmorRenderer}.<br>
		 * This event is called before rendering, but after {@link GeoRenderer#preRender}<br>
		 * <br>
		 * This event is <u>cancellable</u>.<br>
		 * If the event is cancelled by returning false in the {@link Listener}, the armor piece will not be rendered and the corresponding {@link Post} event will not be fired.
		 */
		public static class Pre extends GeoRenderArmorEvent {
			public static final GeoRenderPhaseEventFactory.GeoRenderPhaseEvent EVENT = Services.GEO_RENDER_PHASE_EVENT_FACTORY.create();

			private final PoseStack poseStack;
			private final BakedGeoModel model;
			private final MultiBufferSource bufferSource;
			private final float partialTick;
			private final int packedLight;

			public Pre(GeoArmorRenderer<?> renderer, PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {
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
		 * Post-render event for armor pieces being rendered by {@link GeoEntityRenderer}.<br>
		 * This event is called after {@link GeoRenderer#postRender}
		 */
		public static class Post extends GeoRenderArmorEvent {
			public static final GeoRenderPhaseEventFactory.GeoRenderPhaseEvent EVENT = Services.GEO_RENDER_PHASE_EVENT_FACTORY.create();

			private final PoseStack poseStack;
			private final BakedGeoModel model;
			private final MultiBufferSource bufferSource;
			private final float partialTick;
			private final int packedLight;

			public Post(GeoArmorRenderer<?> renderer, PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {
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
			 * Event listener interface for the Armor.Post GeoRenderEvent
			 */
			@FunctionalInterface
			public interface Listener {
				void handle(Post event);
			}
		}

		/**
		 * One-time event for a {@link GeoArmorRenderer} called on first initialisation.<br>
		 * Use this event to add render layers to the renderer as needed
		 */
		public static class CompileRenderLayers extends GeoRenderArmorEvent {
			public static final GeoRenderPhaseEventFactory.GeoRenderPhaseEvent EVENT  = Services.GEO_RENDER_PHASE_EVENT_FACTORY.create();

			public CompileRenderLayers(GeoArmorRenderer<?> renderer) {
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
			 * Event listener interface for the Armor.CompileRenderLayers GeoRenderEvent
			 */
			@FunctionalInterface
			public interface Listener {
				void handle(CompileRenderLayers event);
			}
		}
	}