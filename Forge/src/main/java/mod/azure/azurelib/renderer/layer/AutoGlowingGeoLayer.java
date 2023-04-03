package mod.azure.azurelib.renderer.layer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.cache.texture.AutoGlowingTexture;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.renderer.GeoRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

/**
 * {@link GeoRenderLayer} for rendering the auto-generated glowlayer functionality implemented by AzureLib using
 * the <i>_glowing</i> appendixed texture files.
 * @see <a href="https://github.com/bernie-g/AzureLib/wiki/Emissive-Textures-Glow-Layer">AzureLib Wiki - Glow Layers</a>
 */
public class AutoGlowingGeoLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {
	public AutoGlowingGeoLayer(GeoRenderer<T> renderer) {
		super(renderer);
	}

	/**
	 * Get the render type to use for this glowlayer renderer.<br>
	 * Uses {@link RenderType#eyes(ResourceLocation)} by default, which may not be ideal in all circumstances.
	 */
	protected RenderType getRenderType(T animatable) {
		return AutoGlowingTexture.getRenderType(getTextureResource(animatable));
	}

	/**
	 * This is the method that is actually called by the render for your render layer to function.<br>
	 * This is called <i>after</i> the animatable has been rendered, but before supplementary rendering like nametags.
	 */
	@Override
	public void render(MatrixStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, IRenderTypeBuffer bufferSource, IVertexBuilder buffer, float partialTick, int packedLight, int packedOverlay) {
		RenderType emissiveRenderType = getRenderType(animatable);

		getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, emissiveRenderType,
				bufferSource.getBuffer(emissiveRenderType), partialTick, 15728640, OverlayTexture.NO_OVERLAY,
				1, 1, 1, 1);
	}
}
