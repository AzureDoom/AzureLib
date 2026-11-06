package mod.azure.azurelib.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.cache.object.AzCube;
import mod.azure.azurelib.cache.object.AzMesh;
import mod.azure.azurelib.cache.object.AzQuad;
import mod.azure.azurelib.cache.object.AzSkinnedMesh;
import mod.azure.azurelib.cache.object.AzVertex;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.model.AzSkinBinding;
import mod.azure.azurelib.model.AzSkinning;
import mod.azure.azurelib.model.factory.primitive.MeshPolygons;
import mod.azure.azurelib.render.item.AzItemRendererPipelineContext;
import mod.azure.azurelib.util.client.RenderUtils;

/**
 * AzModelRenderer provides a generic and extensible base class for rendering models by processing hierarchical bone
 * structures recursively. It leverages a rendering pipeline and a layer renderer to facilitate advanced rendering
 * tasks, including layer application and animated texture processing.
 *
 * @param <K> The type of the key used to identify the animatable object. Typically, a UUID for items/entities and Long
 *            for BlockEntities.
 * @param <T> the type of animatable object this renderer supports
 */
public class AzModelRenderer<K, T> {

    private final Matrix4f poseStateCache = new Matrix4f();

    private final Matrix3f normalStateCache = new Matrix3f();

    private final Vector3f normalScratch = new Vector3f();

    private final Vector4f quadPosition = new Vector4f();

    private final Matrix4f savedPoseScratch = new Matrix4f();

    private final Matrix3f savedNormalScratch = new Matrix3f();

    // Mesh rendering scratch, reused across frames so meshes render without allocating.

    private final Matrix4f skinOwnerInverse = new Matrix4f();

    private final Matrix4f skinJoint = new Matrix4f();

    private float[] skinJointMatrices = new float[0];

    private float[] skinnedPositions = new float[0];

    private float[] skinnedNormalMatrices = new float[0];

    private int meshColor;

    private int meshOverlay;

    private int meshLight;

    private float meshUScale = 1f;

    private float meshVScale = 1f;

    private boolean meshUseOverride;

    private final AzRendererPipeline<K, T> rendererPipeline;

    protected final AzLayerRenderer<K, T> layerRenderer;

    private IntIntPair entityTextureSize;

    public AzModelRenderer(AzRendererPipeline<K, T> rendererPipeline, AzLayerRenderer<K, T> layerRenderer) {
        this.layerRenderer = layerRenderer;
        this.rendererPipeline = rendererPipeline;
    }

    /**
     * The actual render method that subtype renderers should override to handle their specific rendering tasks.<br>
     */
    protected void render(AzRendererPipelineContext<K, T> context, boolean isReRender) {
        var animatable = context.animatable();
        var model = context.bakedModel();

        rendererPipeline.updateAnimatedTextureFrame(animatable);

        for (var bone : model.getTopLevelBones()) {
            renderRecursively(context, bone, isReRender);
        }

        var config = rendererPipeline.config();
        config.renderEntry(context);
    }

    /**
     * Renders the provided {@link AzBone} and its associated child bones
     */
    protected void renderRecursively(AzRendererPipelineContext<K, T> context, AzBone bone, boolean isReRender) {
        var buffer = context.vertexConsumer();
        var bufferSource = context.multiBufferSource();
        var poseStack = context.poseStack();

        poseStack.pushPose();
        RenderUtils.prepMatrixForBone(poseStack, bone);

        context.setVertexConsumer(getOrRefreshRenderBuffer(isReRender, context, bone));

        if (
            !boneRenderOverride(
                poseStack,
                bone,
                bufferSource,
                buffer,
                context.partialTick(),
                context.packedLight(),
                context.packedOverlay(),
                context.renderColor()
            )
        )
            renderCubesOfBone(context, bone);

        if (!isReRender) {
            layerRenderer.applyRenderLayersForBone(context, bone);
        }

        renderChildBones(context, bone, isReRender);
        poseStack.popPose();
    }

    /**
     * Renders the {@link AzCube GeoCubes} associated with a given {@link AzBone}
     */
    protected void renderCubesOfBone(AzRendererPipelineContext<K, T> context, AzBone bone) {
        if (bone.isHidden() || context.vertexConsumer() == null) {
            return;
        }

        var cubes = bone.getCubes();

        for (int i = 0, size = cubes.size(); i < size; i++) {
            renderCube(context, cubes.get(i));
        }

        List<AzMesh> meshes = bone.getMeshes();

        for (int i = 0, size = meshes.size(); i < size; i++) {
            renderMesh(context, meshes.get(i));
        }

        for (AzSkinBinding binding : bone.getSkinBindings()) {
            renderSkinnedMesh(context, binding);
        }
    }

    protected void renderMesh(AzRendererPipelineContext<K, T> context, AzMesh mesh) {
        PoseStack.Pose last = context.poseStack().last();
        Matrix4f pose = last.pose();
        Matrix3f normalMatrix = last.normal();
        VertexConsumer buffer = context.vertexConsumer();

        beginMeshVertices(context);

        for (int quad = 0, count = mesh.quadCount(); quad < count; quad++) {
            for (int corner = quad * 4, end = corner + 4; corner < end; corner++) {
                int p = corner * 3;
                int t = corner * 2;
                Vector4f position = pose.transform(
                    quadPosition.set(mesh.position(p), mesh.position(p + 1), mesh.position(p + 2), 1f)
                );
                Vector3f normal = normalMatrix.transform(
                    normalScratch.set(mesh.normal(p), mesh.normal(p + 1), mesh.normal(p + 2))
                );

                emitMeshVertex(buffer, position, mesh.uv(t), mesh.uv(t + 1), normal);
            }
        }
    }

    protected void renderSkinnedMesh(AzRendererPipelineContext<K, T> context, AzSkinBinding binding) {
        AzSkinnedMesh mesh = binding.mesh();
        if (!mesh.isBound() || mesh.isEmpty()) {
            return;
        }

        AzBone[][] jointChains = binding.jointChains();
        int jointCount = jointChains.length;
        int positionCount = mesh.positionCount();

        if (skinJointMatrices.length < jointCount * 12) {
            skinJointMatrices = new float[jointCount * 12];
        }

        if (skinnedPositions.length < positionCount * 3) {
            skinnedPositions = new float[positionCount * 3];
            skinnedNormalMatrices = new float[positionCount * 9];
        }

        AzSkinning.chainMatrix(binding.ownerChain(), skinOwnerInverse).invert();

        float[] k = skinJointMatrices;

        for (int j = 0; j < jointCount; j++) {
            AzSkinning.chainMatrix(jointChains[j], skinJoint);
            skinJoint.mulLocal(skinOwnerInverse).mul(mesh.inverseBindMatrix(j));

            int o = j * 12;
            k[o] = skinJoint.m00();
            k[o + 1] = skinJoint.m01();
            k[o + 2] = skinJoint.m02();
            k[o + 3] = skinJoint.m10();
            k[o + 4] = skinJoint.m11();
            k[o + 5] = skinJoint.m12();
            k[o + 6] = skinJoint.m20();
            k[o + 7] = skinJoint.m21();
            k[o + 8] = skinJoint.m22();
            k[o + 9] = skinJoint.m30();
            k[o + 10] = skinJoint.m31();
            k[o + 11] = skinJoint.m32();
        }

        MeshPolygons polygons = mesh.polygons();
        float[] outPositions = skinnedPositions;
        float[] outNormals = skinnedNormalMatrices;
        int max = AzSkinnedMesh.MAX_INFLUENCES;

        for (int p = 0; p < positionCount; p++) {
            float a0 = 0, a1 = 0, a2 = 0, a3 = 0, a4 = 0, a5 = 0, a6 = 0, a7 = 0, a8 = 0, a9 = 0, a10 = 0, a11 = 0;

            for (int i = p * max, end = i + max; i < end; i++) {
                float w = mesh.influenceWeight(i);

                if (w == 0f) {
                    continue;
                }

                int o = mesh.influenceJoint(i) * 12;
                a0 += w * k[o];
                a1 += w * k[o + 1];
                a2 += w * k[o + 2];
                a3 += w * k[o + 3];
                a4 += w * k[o + 4];
                a5 += w * k[o + 5];
                a6 += w * k[o + 6];
                a7 += w * k[o + 7];
                a8 += w * k[o + 8];
                a9 += w * k[o + 9];
                a10 += w * k[o + 10];
                a11 += w * k[o + 11];
            }

            int r = p * 3;
            float x = polygons.position(r);
            float y = polygons.position(r + 1);
            float z = polygons.position(r + 2);
            outPositions[r] = a0 * x + a3 * y + a6 * z + a9;
            outPositions[r + 1] = a1 * x + a4 * y + a7 * z + a10;
            outPositions[r + 2] = a2 * x + a5 * y + a8 * z + a11;

            float n0 = a4 * a8 - a5 * a7, n1 = a5 * a6 - a3 * a8, n2 = a3 * a7 - a4 * a6;
            float n3 = a7 * a2 - a8 * a1, n4 = a8 * a0 - a6 * a2, n5 = a6 * a1 - a7 * a0;
            float n6 = a1 * a5 - a2 * a4, n7 = a2 * a3 - a0 * a5, n8 = a0 * a4 - a1 * a3;
            float sign = (a0 * n0 + a1 * n1 + a2 * n2) < 0f ? -1f : 1f;

            int n = p * 9;
            outNormals[n] = n0 * sign;
            outNormals[n + 1] = n1 * sign;
            outNormals[n + 2] = n2 * sign;
            outNormals[n + 3] = n3 * sign;
            outNormals[n + 4] = n4 * sign;
            outNormals[n + 5] = n5 * sign;
            outNormals[n + 6] = n6 * sign;
            outNormals[n + 7] = n7 * sign;
            outNormals[n + 8] = n8 * sign;
        }

        PoseStack.Pose last = context.poseStack().last();
        Matrix4f pose = last.pose();
        Matrix3f normalMatrix = last.normal();
        VertexConsumer buffer = context.vertexConsumer();

        beginMeshVertices(context);

        for (int quad = 0, count = polygons.quadCount(); quad < count; quad++) {
            for (int corner = quad * 4, end = corner + 4; corner < end; corner++) {
                int positionIndex = polygons.cornerPosition(corner);
                int r = positionIndex * 3;
                int n = positionIndex * 9;
                int c = corner * 3;
                int t = corner * 2;
                float nx = polygons.cornerNormal(c);
                float ny = polygons.cornerNormal(c + 1);
                float nz = polygons.cornerNormal(c + 2);

                normalScratch.set(
                    outNormals[n] * nx + outNormals[n + 3] * ny + outNormals[n + 6] * nz,
                    outNormals[n + 1] * nx + outNormals[n + 4] * ny + outNormals[n + 7] * nz,
                    outNormals[n + 2] * nx + outNormals[n + 5] * ny + outNormals[n + 8] * nz
                );
                normalMatrix.transform(normalScratch);

                if (normalScratch.lengthSquared() > 1.0e-12f) {
                    normalScratch.normalize();
                }

                Vector4f position = pose.transform(
                    quadPosition.set(outPositions[r], outPositions[r + 1], outPositions[r + 2], 1f)
                );

                emitMeshVertex(buffer, position, polygons.cornerUv(t), polygons.cornerUv(t + 1), normalScratch);
            }
        }
    }

    private void beginMeshVertices(AzRendererPipelineContext<K, T> context) {
        Identifier textureOverride = context.getTextureOverride();
        IntIntPair boneTextureSize = textureOverride != null ? context.computeTextureSize(textureOverride) : null;

        meshUseOverride = textureOverride != null && boneTextureSize != null && entityTextureSize != null;
        meshUScale = meshUseOverride ? (float) entityTextureSize.firstInt() / boneTextureSize.firstInt() : 1f;
        meshVScale = meshUseOverride ? (float) entityTextureSize.secondInt() / boneTextureSize.secondInt() : 1f;
        meshColor = meshUseOverride ? -1 : context.renderColor();
        meshOverlay = context.packedOverlay();
        meshLight = context.packedLight();
    }

    private void emitMeshVertex(VertexConsumer buffer, Vector4f position, float u, float v, Vector3f normal) {
        putFull(
            buffer,
            position.x(),
            position.y(),
            position.z(),
            meshColor,
            u * meshUScale,
            v * meshVScale,
            meshOverlay,
            meshLight,
            normal.x(),
            normal.y(),
            normal.z()
        );
    }

    /**
     * Render the child bones of a given {@link AzBone}.<br>
     * Note that this does not render the bone itself. That should be done through
     * {@link AzModelRenderer#renderCubesOfBone} separately
     */
    protected void renderChildBones(AzRendererPipelineContext<K, T> context, AzBone bone, boolean isReRender) {
        if (bone.isHidingChildren())
            return;

        var children = bone.getChildBones();

        for (int i = 0, size = children.size(); i < size; i++) {
            renderRecursively(context, children.get(i), isReRender);
        }
    }

    /**
     * Renders an individual {@link AzCube}.<br>
     * This tends to be called recursively from something like {@link AzModelRenderer#renderCubesOfBone}
     */
    protected void renderCube(AzRendererPipelineContext<K, T> context, AzCube cube) {
        var last = context.poseStack().last();
        var transform = cube.transform();
        var poseState = poseStateCache.set(last.pose());
        Matrix3f normalisedPoseState;

        if (transform.identity()) {
            normalisedPoseState = last.normal();
        } else {
            poseState.mul(transform.pose());
            normalisedPoseState = normalStateCache.set(last.normal()).mul(transform.normal());
        }

        var normalFlips = cube.normalFlips();

        for (var quad : cube.quads()) {
            if (quad == null) {
                continue;
            }

            normalScratch.set(quad.normal());
            normalisedPoseState.transform(normalScratch);
            var normal = normalScratch;

            RenderUtils.fixInvertedFlatCube(normalFlips, normal);
            createVerticesOfQuad(context, quad, poseState, normal);
        }
    }

    /**
     * Applies the {@link AzQuad Quad's} {@link AzVertex vertices} to the given {@link VertexConsumer buffer} for
     * rendering
     */
    protected void createVerticesOfQuad(
        AzRendererPipelineContext<K, T> context,
        AzQuad quad,
        Matrix4f poseState,
        Vector3f normal
    ) {
        var buffer = context.vertexConsumer();
        if (buffer == null) {
            return;
        }
        var color = context.renderColor();
        var packedOverlay = context.packedOverlay();
        var packedLight = context.packedLight();
        var textureOverride = context.getTextureOverride();
        var boneTextureSize = textureOverride == null ? null : context.computeTextureSize(textureOverride);
        boolean useOverride = textureOverride != null && boneTextureSize != null && entityTextureSize != null;
        float nx = normal.x(), ny = normal.y(), nz = normal.z();

        for (var vertex : quad.vertices()) {
            var position = vertex.position();
            var vector4f = poseState.transform(quadPosition.set(position.x(), position.y(), position.z(), 1.0f));

            if (useOverride) {
                var texU = (vertex.texU() * entityTextureSize.firstInt()) / boneTextureSize.firstInt();
                var texV = (vertex.texV() * entityTextureSize.secondInt()) / boneTextureSize.secondInt();

                putFull(
                    buffer,
                    vector4f.x(),
                    vector4f.y(),
                    vector4f.z(),
                    color,
                    texU,
                    texV,
                    packedOverlay,
                    packedLight,
                    nx,
                    ny,
                    nz
                );
            } else {
                putFull(
                    buffer,
                    vector4f.x(),
                    vector4f.y(),
                    vector4f.z(),
                    color,
                    vertex.texU(),
                    vertex.texV(),
                    packedOverlay,
                    packedLight,
                    nx,
                    ny,
                    nz
                );
            }
        }
    }

    /**
     * 26.2 removed the all-in-one {@code VertexConsumer#addVertex(x, y, z, color, u, v, overlay, light, nx, ny, nz)}
     * convenience overload in favour of a builder-style chain; this restores a single call site for it.
     */
    private static void putFull(
        VertexConsumer buffer,
        float x,
        float y,
        float z,
        int color,
        float u,
        float v,
        int overlay,
        int light,
        float normalX,
        float normalY,
        float normalZ
    ) {
        buffer.addVertex(x, y, z)
            .setColor(color)
            .setUv(u, v)
            .setOverlay(overlay)
            .setLight(light)
            .setNormal(normalX, normalY, normalZ);
    }

    /**
     * Override method for rendering a specific bone. This method can be customized to apply specific transformations,
     * modify the bone's render properties, or override its rendering behavior entirely.
     *
     * @param poseStack     The pose stack used for handling transformations (rotation, scaling, and translation).
     * @param bone          The bone that is being rendered.
     * @param buffer        The vertex consumer buffer used for writing vertex data during rendering.
     * @param partialTick   The partial tick progress for interpolating between frames.
     * @param packedLight   The packed light value for the rendered bone.
     * @param packedOverlay The packed overlay value for the rendered bone.
     * @param colour        The color modifier for the rendered output.
     * @return A boolean indicating whether the bone's rendering behavior has been overridden successfully.
     */
    @SuppressWarnings("unused")
    public boolean boneRenderOverride(
        PoseStack poseStack,
        AzBone bone,
        AzBufferSource bufferSource,
        VertexConsumer buffer,
        float partialTick,
        int packedLight,
        int packedOverlay,
        int colour
    ) {
        return false;
    }

    public void handleAnimation(AzAnimator<?, T> animator, T animatable, float partialTick) {
        animator.animate(animatable, partialTick);
    }

    public VertexConsumer getOrRefreshBufferRenderType(
        AzItemRendererPipelineContext context,
        AzBone bone,
        RenderType renderType
    ) {
        return context.multiBufferSource().getBuffer(renderType);
    }

    /**
     * Retrieves the appropriate {@link VertexConsumer} for rendering, or refreshes the render buffer if needed.
     * Depending on the rendering context and state of the current buffer, this method determines whether to reuse the
     * existing buffer or acquire a new one.
     *
     * @param isReRender Indicates whether this is a re-render operation. If true, the current buffer is reused.
     * @param context    The rendering context containing relevant information like the current buffer, buffer source,
     *                   and render type.
     * @return The {@link VertexConsumer} that should be used for rendering, potentially refreshed based on the buffer's
     *         state and the given render context.
     */
    public VertexConsumer getOrRefreshRenderBuffer(
        boolean isReRender,
        AzRendererPipelineContext<K, T> context,
        AzBone bone
    ) {
        var config = rendererPipeline.config();
        var bufferSource = context.multiBufferSource();

        var textureOverride = config.boneTextureOverrideProvider(bone);
        var renderTypeOverride = config.boneRenderTypeOverrideProvider(bone);

        context.setTextureOverride(textureOverride);

        if (isReRender && textureOverride == null && renderTypeOverride == null) {
            return context.vertexConsumer();
        }

        RenderType activeRenderType;

        if (renderTypeOverride != null) {
            activeRenderType = renderTypeOverride;
        } else if (textureOverride != null) {
            var baseTexture = config.textureLocation(context.currentEntity(), context.animatable());
            var baseRenderType = config.getRenderType(context.currentEntity(), context.animatable());

            activeRenderType = context.getDefaultRenderType(
                context.animatable(),
                textureOverride,
                bufferSource,
                context.partialTick(),
                retargetRenderType(baseRenderType, baseTexture, textureOverride),
                config.alpha(context.animatable())
            );
        } else {
            activeRenderType = context.renderType();
        }

        if (activeRenderType == null) {
            return null;
        }

        return bufferSource.getBuffer(activeRenderType);
    }

    /**
     * Returns the equivalent of {@code renderType} for a different texture, used for bones with a texture override.
     * <p>
     * Render types can't be re-pointed at another texture directly, so this recognizes the standard entity render types
     * AzureLib and most renderers use and rebuilds the matching one for {@code newTexture}. Anything else falls back to
     * {@link RenderTypes#entityCutout}. For full control over an overridden bone's render type, use the bone render
     * type override provider instead, which takes precedence over this.
     * </p>
     *
     * @param renderType  the render type configured for the model, built for {@code baseTexture}
     * @param baseTexture the model's main texture
     * @param newTexture  the texture the bone should render with
     */
    protected RenderType retargetRenderType(
        @Nullable RenderType renderType,
        @Nullable Identifier baseTexture,
        Identifier newTexture
    ) {
        if (newTexture.equals(baseTexture)) {
            return renderType != null ? renderType : RenderTypes.entityCutout(newTexture);
        }

        if (renderType != null && baseTexture != null) {
            if (renderType == RenderTypes.entityTranslucent(baseTexture)) {
                return RenderTypes.entityTranslucent(newTexture);
            }

            if (renderType == RenderTypes.entityTranslucentEmissive(baseTexture)) {
                return RenderTypes.entityTranslucentEmissive(newTexture);
            }

            if (renderType == RenderTypes.armorCutoutNoCull(baseTexture)) {
                return RenderTypes.armorCutoutNoCull(newTexture);
            }
        }

        return RenderTypes.entityCutout(newTexture);
    }

    public void cacheTexture(AzRendererPipelineContext<K, T> context) {
        this.entityTextureSize = context.computeTextureSize(
            rendererPipeline.config().textureLocation(context.currentEntity(), context.animatable())
        );
    }

    public void clearCacheTexture() {
        this.entityTextureSize = null;
    }
}
