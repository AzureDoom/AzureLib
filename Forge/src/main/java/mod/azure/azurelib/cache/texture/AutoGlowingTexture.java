package mod.azure.azurelib.cache.texture;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.IRenderCall;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.resource.GeoGlowingTextureMeta;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Texture object type responsible for AzureLib's emissive render textures
 * 
 * @see <a href="https://github.com/bernie-g/AzureLib/wiki/Emissive-Textures-Glow-Layer">AzureLib Wiki - Glow Layers</a>
 */
public class AutoGlowingTexture extends GeoAbstractTexture {
//	private static final RenderState.ShadeModelState SHADER_STATE = new RenderState.ShadeModelState(GameRenderer::getRendertypeEntityTranslucentShader);
//	private static final RenderState.TransparencyStateShard TRANSPARENCY_STATE = new RenderState.TransparencyStateShard("translucent_transparency", () -> {
//		RenderSystem.enableBlend();
//		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//	}, () -> {
//		RenderSystem.disableBlend();
//		RenderSystem.defaultBlendFunc();
//	});
//	private static final RenderState.WriteMaskStateShard WRITE_MASK = new RenderState.WriteMaskStateShard(true, true);
//	private static final Function<ResourceLocation, RenderType> RENDER_TYPE_FUNCTION = Util.memoize(texture -> {
//		RenderStateShard.TextureStateShard textureState = new RenderStateShard.TextureStateShard(texture, false, false);
//
//		return RenderType.create("geo_glowing_layer", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder().setShaderState(SHADER_STATE).setTextureState(textureState).setTransparencyState(TRANSPARENCY_STATE).setWriteMaskState(WRITE_MASK).createCompositeState(false));
//	});
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
	protected IRenderCall loadTexture(IResourceManager resourceManager, Minecraft mc) throws IOException {
		Texture originalTexture;

		try {
			originalTexture = mc.submit(() -> mc.getTextureManager().getTexture(this.textureBase)).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new IOException("Failed to load original texture: " + this.textureBase, e);
		}

		IResource textureBaseResource = resourceManager.getResource(this.textureBase);
		NativeImage baseImage = originalTexture instanceof DynamicTexture ? ((DynamicTexture) originalTexture).getPixels() : NativeImage.read(textureBaseResource.getInputStream());
		NativeImage glowImage = null;
		TextureMetadataSection textureBaseMeta = textureBaseResource.getMetadata(TextureMetadataSection.SERIALIZER);
		boolean blur = textureBaseMeta != null && textureBaseMeta.isBlur();
		boolean clamp = textureBaseMeta != null && textureBaseMeta.isClamp();

		try {
			IResource glowLayerResource = resourceManager.getResource(this.glowLayer);
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

				if (!FMLEnvironment.production) {
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
//		return RENDER_TYPE_FUNCTION.apply(getEmissiveResource(texture));
		return null;
	}
}
