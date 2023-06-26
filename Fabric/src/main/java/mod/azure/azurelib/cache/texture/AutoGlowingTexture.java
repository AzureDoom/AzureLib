package mod.azure.azurelib.cache.texture;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.resource.GeoGlowingTextureMeta;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * Texture object type responsible for AzureLib's emissive render textures
 */
public class AutoGlowingTexture extends GeoAbstractTexture {

	static class GlowRenderType extends RenderType {

		public GlowRenderType(String p_i225992_1_, VertexFormat p_i225992_2_, int p_i225992_3_, int p_i225992_4_, boolean p_i225992_5_, boolean p_i225992_6_, Runnable p_i225992_7_, Runnable p_i225992_8_) {
			super(p_i225992_1_, p_i225992_2_, p_i225992_3_, p_i225992_4_, p_i225992_5_, p_i225992_6_, p_i225992_7_, p_i225992_8_);
			// TODO Auto-generated constructor stub
		}

		public static RenderType emissive(ResourceLocation texture) {
			return RenderType.create("geo_glowing_layer", DefaultVertexFormat.NEW_ENTITY, GL11.GL_QUADS, 256, RenderType.CompositeState.builder().setAlphaState(RenderType.DEFAULT_ALPHA).setCullState(RenderType.NO_CULL).setTextureState(new TextureStateShard(texture, false, false)).setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY).setOverlayState(RenderType.OVERLAY).createCompositeState(true));
		}
	}

	private static final String APPENDIX = "_glowmask";

	protected final ResourceLocation textureBase;
	protected final ResourceLocation glowLayer;

	public AutoGlowingTexture(ResourceLocation originalLocation, ResourceLocation location) {
		this.textureBase = originalLocation;
		this.glowLayer = location;
	}

	/**
	 * Get the emissive resource equivalent of the input resource path.<br>
	 * Additionally prepares the texture manager for the missing texture if the resource is not present
	 * 
	 * @return The glowlayer resourcepath for the provided input path
	 */
	private static ResourceLocation getEmissiveResource(ResourceLocation baseResource) {
		ResourceLocation path = appendToPath(baseResource, APPENDIX);

		generateTexture(path, textureManager -> textureManager.register(path, new AutoGlowingTexture(baseResource, path)));

		return path;
	}

	/**
	 * Generates the glow layer {@link NativeImage} and appropriately modifies the base texture for use in glow render layers
	 */
	@Nullable
	@Override
	protected RenderCall loadTexture(ResourceManager resourceManager, Minecraft mc) throws IOException {
		AbstractTexture originalTexture;

		try {
			originalTexture = mc.submit(() -> mc.getTextureManager().getTexture(this.textureBase)).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new IOException("Failed to load original texture: " + this.textureBase, e);
		}

		Resource textureBaseResource = resourceManager.getResource(this.textureBase);
		NativeImage baseImage = originalTexture instanceof DynamicTexture ? ((DynamicTexture) originalTexture).getPixels() : NativeImage.read(textureBaseResource.getInputStream());
		NativeImage glowImage = null;
		TextureMetadataSection textureBaseMeta = textureBaseResource.getMetadata(TextureMetadataSection.SERIALIZER);
		boolean blur = textureBaseMeta != null && textureBaseMeta.isBlur();
		boolean clamp = textureBaseMeta != null && textureBaseMeta.isClamp();

		try {
			Resource glowLayerResource = resourceManager.getResource(this.glowLayer);
			GeoGlowingTextureMeta glowLayerMeta = null;

			if (glowLayerResource != null) {
				glowImage = NativeImage.read(glowLayerResource.getInputStream());
				glowLayerMeta = GeoGlowingTextureMeta.fromExistingImage(glowImage);
			} else {
				GeoGlowingTextureMeta meta = textureBaseResource.getMetadata(GeoGlowingTextureMeta.DESERIALIZER);
				if (meta != null) {
					glowLayerMeta = meta;
					glowImage = new NativeImage(baseImage.getWidth(), baseImage.getHeight(), true);
				}
			}

			if (glowLayerMeta != null) {
				glowLayerMeta.createImageMask(baseImage, glowImage);

				if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
					printDebugImageToDisk(this.textureBase, baseImage);
					printDebugImageToDisk(this.glowLayer, glowImage);
				}
			}
		} catch (IOException e) {
			AzureLib.LOGGER.warn("Resource failed to open for glowlayer meta: {}", this.glowLayer, e);
		}

		NativeImage mask = glowImage;

		if (mask == null)
			return null;

		return () -> {
			uploadSimple(getId(), mask, blur, clamp);

			if (originalTexture instanceof DynamicTexture) {
				((DynamicTexture) originalTexture).upload();
			} else {
				uploadSimple(originalTexture.getId(), baseImage, blur, clamp);
			}
		};
	}

	/**
	 * Return a cached instance of the RenderType for the given texture for GeoGlowingLayer rendering.
	 * 
	 * @param texture The texture of the resource to apply a glow layer to
	 */
	public static RenderType getRenderType(ResourceLocation texture) {
		return GlowRenderType.emissive(getEmissiveResource(texture));
	}
}
