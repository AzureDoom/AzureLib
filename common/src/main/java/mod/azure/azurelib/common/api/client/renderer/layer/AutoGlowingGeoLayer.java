/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.api.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import mod.azure.azurelib.common.internal.client.renderer.GeoRenderer;
import mod.azure.azurelib.common.internal.common.cache.object.BakedGeoModel;
import mod.azure.azurelib.common.internal.common.cache.texture.AutoGlowingTexture;
import mod.azure.azurelib.core.animatable.GeoAnimatable;

/**
 * {@link GeoRenderLayer} for rendering the auto-generated glowlayer functionality implemented by AzureLib using the
 * <i>_glowing</i> appendixed texture files.
 */
@Deprecated(forRemoval = true)
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
    public void render(
        PoseStack poseStack,
        T animatable,
        BakedGeoModel bakedModel,
        RenderType renderType,
        MultiBufferSource bufferSource,
        VertexConsumer buffer,
        float partialTick,
        int packedLight,
        int packedOverlay
    ) {
        renderType = getRenderType(animatable);

        if (renderType != null) {
            getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                animatable,
                renderType,
                bufferSource.getBuffer(renderType),
                partialTick,
                15728640,
                packedOverlay,
                getRenderer().getRenderColor(animatable, partialTick, packedLight).argbInt()
            );
        }
    }
}
