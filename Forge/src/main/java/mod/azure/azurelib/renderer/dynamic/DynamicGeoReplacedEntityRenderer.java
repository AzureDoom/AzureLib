/**
 * This class is a fork of the matching class found in the Geckolib repository.
 * Original source: https://github.com/bernie-g/geckolib
 * Copyright © 2024 Bernie-G.
 * Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.renderer.dynamic;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mod.azure.azurelib.cache.object.*;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.model.GeoModel;
import mod.azure.azurelib.renderer.GeoReplacedEntityRenderer;
import mod.azure.azurelib.util.RenderUtils;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Extended special-entity renderer for more advanced or dynamic models.<br>
 * Because of the extra performance cost of this renderer, it is advised to avoid using it unnecessarily, and consider
 * whether the benefits are worth the cost for your needs.
 */
@Deprecated()
public abstract class DynamicGeoReplacedEntityRenderer<E extends Entity, T extends GeoAnimatable> extends GeoReplacedEntityRenderer<E,T> {

    protected static Map<ResourceLocation, Tuple<Integer, Integer>> TEXTURE_DIMENSIONS_CACHE = new Object2ObjectOpenHashMap<>();

    protected ResourceLocation textureOverride = null;

    protected DynamicGeoReplacedEntityRenderer(EntityRendererManager renderManager, GeoModel<T> model, T animatable) {
        super(renderManager, model, animatable);
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
            IRenderTypeBuffer bufferSource,
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
            MatrixStack poseStack,
            GeoBone bone,
            IRenderTypeBuffer bufferSource,
            IVertexBuilder buffer,
            float partialTick,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        return false;
    }

    /**
     * Renders the provided {@link GeoBone} and its associated child bones
     */
    @Override
    public void renderRecursively(
            MatrixStack poseStack,
            T animatable,
            GeoBone bone,
            RenderType renderType,
            IRenderTypeBuffer bufferSource,
            IVertexBuilder buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        poseStack.push();
        RenderUtils.translateMatrixToBone(poseStack, bone);
        RenderUtils.translateToPivotPoint(poseStack, bone);
        RenderUtils.rotateMatrixAroundBone(poseStack, bone);
        RenderUtils.scaleMatrixForBone(poseStack, bone);

        if (bone.isTrackingMatrices()) {
            Matrix4f poseState = new Matrix4f(poseStack.getLast().getMatrix());
            Matrix4f localMatrix = RenderUtils.invertAndMultiplyMatrices(poseState, this.entityRenderTranslations);

            bone.setModelSpaceMatrix(RenderUtils.invertAndMultiplyMatrices(poseState, this.modelRenderTranslations));
            localMatrix.translate(new Vector3f(getRenderOffset(this.currentEntity, 1)));
            bone.setLocalSpaceMatrix(localMatrix);
            Matrix4f worldState = localMatrix.copy();

            worldState.translate(new Vector3f(this.currentEntity.getPositionVec()));
            bone.setWorldSpaceMatrix(worldState);
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
                        red,
                        green,
                        blue,
                        alpha
                )
        )
            super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, red,
                    green,
                    blue,
                    alpha);

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
                red,
                green,
                blue,
                alpha
        );

        poseStack.pop();
    }

    /**
     * Called after rendering the model to buffer. Post-render modifications should be performed here.<br>
     * {@link MatrixStack} transformations will be unused and lost once this method ends
     */
    @Override
    public void postRender(
            MatrixStack poseStack,
            T animatable,
            BakedGeoModel model,
            IRenderTypeBuffer bufferSource,
            IVertexBuilder buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
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
                red,
                green,
                blue,
                alpha
        );
    }

    /**
     * Applies the {@link GeoQuad Quad's} {@link GeoVertex vertices} to the given {@link IVertexBuilder buffer} for
     * rendering.<br>
     * Custom override to handle custom non-baked textures for ExtendedGeoEntityRenderer
     */
    @Override
    public void createVerticesOfQuad(
            GeoQuad quad,
            Matrix4f poseState,
            Vector3f normal,
            IVertexBuilder buffer,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        if (this.textureOverride == null) {
            super.createVerticesOfQuad(
                    quad,
                    poseState,
                    normal,
                    buffer,
                    packedLight,
                    packedOverlay,
                    red,
                    green,
                    blue,
                    alpha
            );

            return;
        }

        Tuple<Integer, Integer> boneTextureSize = computeTextureSize(this.textureOverride);
        Tuple<Integer, Integer> entityTextureSize = computeTextureSize(getTextureLocation(this.animatable));

        if (boneTextureSize == null || entityTextureSize == null) {
            super.createVerticesOfQuad(
                    quad,
                    poseState,
                    normal,
                    buffer,
                    packedLight,
                    packedOverlay,
                    red,
                    green,
                    blue,
                    alpha
            );

            return;
        }

        for (GeoVertex vertex : quad.getVertices()) {
            Vector4f vector4f = new Vector4f(vertex.position().getX(), vertex.position().getY(), vertex.position().getZ(), 1);
            float texU = (vertex.texU() * entityTextureSize.getA()) / boneTextureSize.getA();
            float texV = (vertex.texV() * entityTextureSize.getB()) / boneTextureSize.getB();

            vector4f.transform(poseState);

            buffer.addVertex(vector4f.getX(), vector4f.getX(), vector4f.getZ(), red, green, blue, alpha, texU, texV,
                    packedOverlay, packedLight, normal.getX(), normal.getX(), normal.getZ());
        }
    }

    /**
     * Retrieve or compute the height and width of a given texture from its {@link ResourceLocation}.<br>
     * This is used for dynamically mapping vertices on a given quad.<br>
     * This is inefficient however, and should only be used where required.
     */
    protected Tuple<Integer, Integer> computeTextureSize(ResourceLocation texture) {
        return TEXTURE_DIMENSIONS_CACHE.computeIfAbsent(texture, RenderUtils::getTextureDimensions);
    }
}
