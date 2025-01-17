/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.api.client.renderer.dynamic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Map;

import mod.azure.azurelib.common.api.client.model.GeoModel;
import mod.azure.azurelib.common.api.client.renderer.GeoItemRenderer;
import mod.azure.azurelib.common.internal.client.util.RenderUtils;
import mod.azure.azurelib.common.internal.common.cache.object.*;
import mod.azure.azurelib.core.animatable.GeoAnimatable;

/**
 * Extended special-item renderer for more advanced or dynamic models.<br>
 * Because of the extra performance cost of this renderer, it is advised to avoid using it unnecessarily, and consider
 * whether the benefits are worth the cost for your needs.
 */
public abstract class DynamicGeoItemRenderer<T extends Item & GeoAnimatable> extends GeoItemRenderer<T> {

    protected static final Map<ResourceLocation, IntIntPair> TEXTURE_DIMENSIONS_CACHE =
        new Object2ObjectOpenHashMap<>();

    protected ResourceLocation textureOverride = null;

    protected DynamicGeoItemRenderer(GeoModel<T> model) {
        super(model);
    }

    /**
     * For each bone rendered, this method is called.<br>
     * If a ResourceLocation is returned, the renderer will render the bone using that texture instead of the
     * default.<br>
     * This can be useful for custom rendering on a per-bone basis.<br>
     * There is a somewhat significant performance cost involved in this however, so only use as needed.
     *
     * @return The specified ResourceLocation, or null if no override
     */
    @Nullable
    protected ResourceLocation getTextureOverrideForBone(GeoBone bone, T animatable, float partialTick) {
        return null;
    }

    /**
     * For each bone rendered, this method is called.<br>
     * If a RenderType is returned, the renderer will render the bone using that RenderType instead of the default.<br>
     * This can be useful for custom rendering operations on a per-bone basis.<br>
     * There is a somewhat significant performance cost involved in this however, so only use as needed.
     *
     * @return The specified RenderType, or null if no override
     */
    @Nullable
    protected RenderType getRenderTypeOverrideForBone(
        GeoBone bone,
        T animatable,
        ResourceLocation texturePath,
        MultiBufferSource bufferSource,
        float partialTick
    ) {
        return null;
    }

    /**
     * Override this to handle a given {@link GeoBone GeoBone's} rendering in a particular way
     *
     * @return Whether the renderer should skip rendering the {@link GeoCube cubes} of the given GeoBone or not
     */
    protected boolean boneRenderOverride(
        PoseStack poseStack,
        GeoBone bone,
        MultiBufferSource bufferSource,
        VertexConsumer buffer,
        float partialTick,
        int packedLight,
        int packedOverlay,
        int colour
    ) {
        return false;
    }

    /**
     * Renders the provided {@link GeoBone} and its associated child bones
     */
    @Override
    public void renderRecursively(
        PoseStack poseStack,
        T animatable,
        GeoBone bone,
        RenderType renderType,
        MultiBufferSource bufferSource,
        VertexConsumer buffer,
        boolean isReRender,
        float partialTick,
        int packedLight,
        int packedOverlay,
        int colour
    ) {
        poseStack.pushPose();
        RenderUtils.translateMatrixToBone(poseStack, bone);
        RenderUtils.translateToPivotPoint(poseStack, bone);
        RenderUtils.rotateMatrixAroundBone(poseStack, bone);
        RenderUtils.scaleMatrixForBone(poseStack, bone);

        if (bone.isTrackingMatrices()) {
            Matrix4f poseState = new Matrix4f(poseStack.last().pose());
            Matrix4f localMatrix = RenderUtils.invertAndMultiplyMatrices(poseState, this.itemRenderTranslations);

            bone.setModelSpaceMatrix(RenderUtils.invertAndMultiplyMatrices(poseState, this.modelRenderTranslations));
            bone.setLocalSpaceMatrix(
                RenderUtils.translateMatrix(localMatrix, getRenderOffset(this.animatable, 1).toVector3f())
            );
            bone.setLocalSpaceMatrix(
                RenderUtils.translateMatrix(localMatrix, getRenderOffset(this.animatable, 1).toVector3f())
            );
        }

        RenderUtils.translateAwayFromPivotPoint(poseStack, bone);

        this.textureOverride = getTextureOverrideForBone(bone, this.animatable, partialTick);
        ResourceLocation texture = this.textureOverride == null
            ? getTextureLocation(this.animatable)
            : this.textureOverride;
        RenderType renderTypeOverride = getRenderTypeOverrideForBone(
            bone,
            this.animatable,
            texture,
            bufferSource,
            partialTick
        );

        if (texture != null && renderTypeOverride == null)
            renderTypeOverride = getRenderType(this.animatable, texture, bufferSource, partialTick);

        if (renderTypeOverride != null)
            buffer = bufferSource.getBuffer(renderTypeOverride);

        if (
            !boneRenderOverride(
                poseStack,
                bone,
                bufferSource,
                buffer,
                partialTick,
                packedLight,
                packedOverlay,
                colour
            )
        )
            super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, colour);

        if (renderTypeOverride != null)
            buffer = bufferSource.getBuffer(
                getRenderType(this.animatable, getTextureLocation(this.animatable), bufferSource, partialTick)
            );

        if (!isReRender)
            applyRenderLayersForBone(
                poseStack,
                animatable,
                bone,
                renderType,
                bufferSource,
                buffer,
                partialTick,
                packedLight,
                packedOverlay
            );

        super.renderChildBones(
            poseStack,
            animatable,
            bone,
            renderType,
            bufferSource,
            buffer,
            isReRender,
            partialTick,
            packedLight,
            packedOverlay,
            colour
        );

        poseStack.popPose();
    }

    /**
     * Called after rendering the model to buffer. Post-render modifications should be performed here.<br>
     * {@link PoseStack} transformations will be unused and lost once this method ends
     */
    @Override
    public void postRender(
        PoseStack poseStack,
        T animatable,
        BakedGeoModel model,
        MultiBufferSource bufferSource,
        VertexConsumer buffer,
        boolean isReRender,
        float partialTick,
        int packedLight,
        int packedOverlay,
        int colour
    ) {
        this.textureOverride = null;

        super.postRender(
            poseStack,
            animatable,
            model,
            bufferSource,
            buffer,
            isReRender,
            partialTick,
            packedLight,
            packedOverlay,
            colour
        );
    }

    /**
     * Applies the {@link GeoQuad Quad's} {@link GeoVertex vertices} to the given {@link VertexConsumer buffer} for
     * rendering.<br>
     * Custom override to handle custom non-baked textures for ExtendedGeoEntityRenderer
     */
    @Override
    public void createVerticesOfQuad(
        GeoQuad quad,
        Matrix4f poseState,
        Vector3f normal,
        VertexConsumer buffer,
        int packedLight,
        int packedOverlay,
        int colour
    ) {
        if (this.textureOverride == null) {
            super.createVerticesOfQuad(
                quad,
                poseState,
                normal,
                buffer,
                packedLight,
                packedOverlay,
                colour
            );

            return;
        }

        IntIntPair boneTextureSize = computeTextureSize(this.textureOverride);
        IntIntPair entityTextureSize = computeTextureSize(getTextureLocation(this.animatable));

        if (boneTextureSize == null || entityTextureSize == null) {
            super.createVerticesOfQuad(
                quad,
                poseState,
                normal,
                buffer,
                packedLight,
                packedOverlay,
                colour
            );

            return;
        }

        for (GeoVertex vertex : quad.vertices()) {
            Vector4f vector4f = poseState.transform(
                new Vector4f(vertex.position().x(), vertex.position().y(), vertex.position().z(), 1.0f)
            );
            float texU = (vertex.texU() * entityTextureSize.firstInt()) / boneTextureSize.firstInt();
            float texV = (vertex.texV() * entityTextureSize.secondInt()) / boneTextureSize.secondInt();

            buffer.addVertex(
                vector4f.x(),
                vector4f.y(),
                vector4f.z(),
                -1,
                texU,
                texV,
                packedOverlay,
                packedLight,
                normal.x(),
                normal.y(),
                normal.z()
            );
        }
    }

    /**
     * Retrieve or compute the height and width of a given texture from its {@link ResourceLocation}.<br>
     * This is used for dynamically mapping vertices on a given quad.<br>
     * This is inefficient however, and should only be used where required.
     */
    protected IntIntPair computeTextureSize(ResourceLocation texture) {
        return TEXTURE_DIMENSIONS_CACHE.computeIfAbsent(texture, RenderUtils::getTextureDimensions);
    }
}
