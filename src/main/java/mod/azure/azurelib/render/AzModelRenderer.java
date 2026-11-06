package mod.azure.azurelib.render;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.cache.object.*;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.model.AzSkinBinding;
import mod.azure.azurelib.model.AzSkinning;
import mod.azure.azurelib.model.factory.primitive.MeshPolygons;
import mod.azure.azurelib.render.item.AzItemRendererPipelineContext;
import mod.azure.azurelib.render.vertex.MultiBufferSource;
import mod.azure.azurelib.render.vertex.PoseStack;
import mod.azure.azurelib.render.vertex.RenderType;
import mod.azure.azurelib.render.vertex.VertexConsumer;
import mod.azure.azurelib.util.IntIntPair;
import mod.azure.azurelib.util.ResourceLocation;
import mod.azure.azurelib.util.client.RenderUtils;
import mod.azure.azurelib.util.math.Matrix3f;
import mod.azure.azurelib.util.math.Matrix4f;
import mod.azure.azurelib.util.math.Vector3f;
import mod.azure.azurelib.util.math.Vector4f;

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

    private final AzRendererPipeline<K, T> rendererPipeline;

    protected final AzLayerRenderer<K, T> layerRenderer;

    /*
     * Scratch objects reused for every bone, cube and vertex instead of allocating new ones. Rendering is single
     * threaded and none of these are held across calls, so one set per renderer is enough.
     */
    private final Matrix4f poseStateCache = new Matrix4f();

    private final Matrix3f normalStateCache = new Matrix3f();

    private final Vector3f flatNormalScratch = new Vector3f();

    private final Vector3f normalScratch = new Vector3f();

    private final Vector4f quadPosition = new Vector4f();

    // Mesh rendering scratch, reused across frames so meshes render without allocating. Skinning maths uses
    // column-major float[16] matrices (see AzSkinning).

    private final float[] skinOwnerInverse = new float[16];

    private final float[] skinJoint = new float[16];

    private final float[] skinScratch = new float[16];

    private float[] skinJointMatrices = new float[0];

    private float[] skinnedPositions = new float[0];

    private float[] skinnedNormalMatrices = new float[0];

    private float meshRed;

    private float meshGreen;

    private float meshBlue;

    private float meshAlpha;

    private int meshOverlay;

    private int meshLight;

    private float meshUScale = 1f;

    private float meshVScale = 1f;

    private Matrix4f[] savedBonePoses = new Matrix4f[0];

    private Matrix3f[] savedBoneNormals = new Matrix3f[0];

    private int boneDepth;

    private IntIntPair entityTextureSize;

    /**
     * The buffer and render type the current render pass draws bones into when they have no override. Bones with a
     * texture or render type override draw into their own buffer, and the bones after them return to this one, so an
     * override never leaks into sibling or child bones.
     */
    @Nullable
    private VertexConsumer passBuffer;

    @Nullable
    private RenderType passRenderType;

    /**
     * Whether an override bone has requested its own buffer since the pass buffer was last used. In 1.19.2 the buffer
     * source reuses one shared {@link BufferBuilder} for every non-fixed render type, so that request re-points the
     * pass buffer's builder at the override's render type; it has to be switched back before the pass buffer is used
     * again.
     */
    private boolean passBufferDisplaced;

    public AzModelRenderer(AzRendererPipeline<K, T> rendererPipeline, AzLayerRenderer<K, T> layerRenderer) {
        this.layerRenderer = layerRenderer;
        this.rendererPipeline = rendererPipeline;
    }

    /**
     * The actual render method that subtype renderers should override to handle their specific rendering tasks.<br>
     */
    protected void render(AzRendererPipelineContext<K, T> context, boolean isReRender) {
        T animatable = context.animatable();
        AzBakedModel model = context.bakedModel();

        rendererPipeline.updateAnimatedTextureFrame(animatable);

        VertexConsumer previousPassBuffer = this.passBuffer;
        RenderType previousPassRenderType = this.passRenderType;
        boolean previousPassBufferDisplaced = this.passBufferDisplaced;
        ResourceLocation previousTextureOverride = context.getTextureOverride();

        this.passBuffer = context.vertexConsumer();
        this.passRenderType = context.renderType();
        this.passBufferDisplaced = false;

        try {
            for (AzBone bone : model.getTopLevelBones()) {
                renderRecursively(context, bone, isReRender);
            }
        } finally {
            if (this.passBuffer != null) {
                restorePassBuffer(context.multiBufferSource());
                context.setVertexConsumer(this.passBuffer);
            }

            context.setTextureOverride(previousTextureOverride);
            this.passBuffer = previousPassBuffer;
            this.passRenderType = previousPassRenderType;
            this.passBufferDisplaced = previousPassBufferDisplaced;
        }

        AzRendererConfig<K, T> config = rendererPipeline.config();
        config.renderEntry(context);
    }

    /**
     * Renders the provided {@link AzBone} and its associated child bones
     */
    protected void renderRecursively(AzRendererPipelineContext<K, T> context, AzBone bone, boolean isReRender) {
        VertexConsumer buffer = context.vertexConsumer();
        MultiBufferSource bufferSource = context.multiBufferSource();
        PoseStack poseStack = context.poseStack();

        int slot = saveBonePose(poseStack);

        try {
            renderBone(context, bone, isReRender, buffer, bufferSource, poseStack);
        } finally {
            restoreBonePose(poseStack, slot);
        }
    }

    private void renderBone(
        AzRendererPipelineContext<K, T> context,
        AzBone bone,
        boolean isReRender,
        VertexConsumer buffer,
        MultiBufferSource bufferSource,
        PoseStack poseStack
    ) {
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
                context.red(),
                context.green(),
                context.blue(),
                context.alpha()
            )
        )
            renderCubesOfBone(context, bone);

        if (!isReRender) {
            layerRenderer.applyRenderLayersForBone(context, bone);
        }

        renderChildBones(context, bone, isReRender);
    }

    /**
     * Saves the current pose so a bone can transform it in place, without the allocation of
     * {@link PoseStack#pushPose()}. Pair every call with {@link #restoreBonePose} (in a {@code finally}), in reverse
     * order. Code between the two may still push and pop the pose stack normally.
     *
     * @return the slot to pass to {@link #restoreBonePose}
     */
    protected final int saveBonePose(PoseStack poseStack) {
        int slot = boneDepth++;

        if (slot >= savedBonePoses.length) {
            int size = Math.max(16, savedBonePoses.length * 2);
            Matrix4f[] poses = Arrays.copyOf(savedBonePoses, size);
            Matrix3f[] normals = Arrays.copyOf(savedBoneNormals, size);

            for (int i = savedBonePoses.length; i < size; i++) {
                poses[i] = new Matrix4f();
                normals[i] = new Matrix3f();
            }

            savedBonePoses = poses;
            savedBoneNormals = normals;
        }

        PoseStack.Pose last = poseStack.last();
        savedBonePoses[slot].load(last.pose());
        savedBoneNormals[slot].load(last.normal());

        return slot;
    }

    /**
     * Restores the pose saved by {@link #saveBonePose}.
     */
    protected final void restoreBonePose(PoseStack poseStack, int slot) {
        PoseStack.Pose last = poseStack.last();
        last.pose().load(savedBonePoses[slot]);
        last.normal().load(savedBoneNormals[slot]);
        boneDepth = slot;
    }

    /**
     * Renders the {@link GeoCube GeoCubes} associated with a given {@link AzBone}
     */
    protected void renderCubesOfBone(AzRendererPipelineContext<K, T> context, AzBone bone) {
        if (bone.isHidden()) {
            return;
        }

        // Cubes apply their own (pre-baked) transform to scratch matrices in renderCube, so the pose stack is left
        // untouched and needs no save/restore per cube.
        List<GeoCube> cubes = bone.getCubes();

        for (int i = 0, size = cubes.size(); i < size; i++) {
            renderCube(context, cubes.get(i));
        }

        List<GeoMesh> meshes = bone.getMeshes();

        for (int i = 0, size = meshes.size(); i < size; i++) {
            renderMesh(context, meshes.get(i));
        }

        for (AzSkinBinding binding : bone.getSkinBindings()) {
            renderSkinnedMesh(context, binding);
        }
    }

    /**
     * Renders a rigid {@link GeoMesh} under the current bone pose, the same way cubes are rendered.
     */
    protected void renderMesh(AzRendererPipelineContext<K, T> context, GeoMesh mesh) {
        PoseStack.Pose last = context.poseStack().last();
        Matrix4f pose = last.pose();
        Matrix3f normalMatrix = last.normal();
        VertexConsumer buffer = context.vertexConsumer();
        AzQuadFilter quadFilter = context.getTextureOverride() == null ? context.quadFilter() : null;

        beginMeshVertices(context);

        for (int quad = 0, count = mesh.quadCount(); quad < count; quad++) {
            int b = quad * 4;

            if (
                    quadFilter != null
                            && !quadFilter.test(mesh.uvBound(b), mesh.uvBound(b + 1), mesh.uvBound(b + 2), mesh.uvBound(b + 3))
            ) {
                continue;
            }

            for (int corner = quad * 4, end = corner + 4; corner < end; corner++) {
                int p = corner * 3;
                int t = corner * 2;

                quadPosition.set(mesh.position(p), mesh.position(p + 1), mesh.position(p + 2), 1f);
                quadPosition.transform(pose);
                normalScratch.set(mesh.normal(p), mesh.normal(p + 1), mesh.normal(p + 2));
                normalScratch.transform(normalMatrix);

                emitMeshVertex(buffer, mesh.uv(t), mesh.uv(t + 1));
            }
        }
    }

    /**
     * Renders a {@link GeoSkinnedMesh} owned by the current bone with linear blend skinning.
     * <p>
     * Each joint contributes {@code K = owner(t)^-1 * joint(t) * joint(rest)^-1}: its movement since the rest pose,
     * expressed relative to the owner, whose transform is already on the pose stack. Positions are skinned once each
     * and shared by every corner that uses them. Normals use the inverse transpose of each position's blended matrix,
     * so they stay perpendicular to the surface under non-uniform bone scale, and are renormalized.
     * </p>
     * <p>
     * Linear blend skinning itself averages matrices, so strong twists, or very different scales on neighbouring
     * joints, can still pinch or thin the mesh where their weights blend (the "candy-wrapper" effect). Lighting stays
     * consistent with the deformed surface; the surface is what's approximate.
     * </p>
     */
    protected void renderSkinnedMesh(AzRendererPipelineContext<K, T> context, AzSkinBinding binding) {
        GeoSkinnedMesh mesh = binding.mesh();

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

        AzSkinning.chainMatrix(binding.ownerChain(), skinOwnerInverse);
        AzSkinning.invert(skinOwnerInverse, skinOwnerInverse);

        float[] k = skinJointMatrices;

        for (int j = 0; j < jointCount; j++) {
            AzSkinning.chainMatrix(jointChains[j], skinJoint);
            AzSkinning.mul(skinOwnerInverse, skinJoint, skinJoint, skinScratch);
            mesh.mulInverseBind(j, skinJoint, skinScratch);

            // Keep the affine 3x4 part, column by column.
            int o = j * 12;
            k[o] = skinJoint[0];
            k[o + 1] = skinJoint[1];
            k[o + 2] = skinJoint[2];
            k[o + 3] = skinJoint[4];
            k[o + 4] = skinJoint[5];
            k[o + 5] = skinJoint[6];
            k[o + 6] = skinJoint[8];
            k[o + 7] = skinJoint[9];
            k[o + 8] = skinJoint[10];
            k[o + 9] = skinJoint[12];
            k[o + 10] = skinJoint[13];
            k[o + 11] = skinJoint[14];
        }

        MeshPolygons polygons = mesh.polygons();
        float[] outPositions = skinnedPositions;
        float[] outNormals = skinnedNormalMatrices;
        int max = GeoSkinnedMesh.MAX_INFLUENCES;

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

            // Normals need the inverse transpose of the blended 3x3, not the 3x3 itself, or non-uniform scale
            // tilts them toward the stretched axis. With columns c0, c1, c2 the inverse transpose is
            // [c1 x c2, c2 x c0, c0 x c1] / det; the 1/det only changes length, which is normalized away later,
            // so keep its sign (mirrored scale) and skip the division.
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
        AzQuadFilter quadFilter = context.getTextureOverride() == null ? context.quadFilter() : null;

        beginMeshVertices(context);

        for (int quad = 0, count = polygons.quadCount(); quad < count; quad++) {
            int b = quad * 4;

            if (
                    quadFilter != null
                            && !quadFilter.test(
                            polygons.uvBound(b),
                            polygons.uvBound(b + 1),
                            polygons.uvBound(b + 2),
                            polygons.uvBound(b + 3)
                    )
            ) {
                continue;
            }

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
                normalScratch.transform(normalMatrix);
                normalScratch.normalize();

                quadPosition.set(outPositions[r], outPositions[r + 1], outPositions[r + 2], 1f);
                quadPosition.transform(pose);

                emitMeshVertex(buffer, polygons.cornerUv(t), polygons.cornerUv(t + 1));
            }
        }
    }

    /** Captures the per-pass vertex state shared by every mesh vertex, mirroring {@link #createVerticesOfQuad}. */
    private void beginMeshVertices(AzRendererPipelineContext<K, T> context) {
        ResourceLocation textureOverride = context.getTextureOverride();
        IntIntPair boneTextureSize = textureOverride != null ? context.computeTextureSize(textureOverride) : null;
        boolean useOverride = textureOverride != null && boneTextureSize != null && entityTextureSize != null;

        meshUScale = useOverride ? (float) entityTextureSize.firstInt() / boneTextureSize.firstInt() : 1f;
        meshVScale = useOverride ? (float) entityTextureSize.secondInt() / boneTextureSize.secondInt() : 1f;
        meshRed = context.red();
        meshGreen = context.green();
        meshBlue = context.blue();
        meshAlpha = context.alpha();
        meshOverlay = context.packedOverlay();
        meshLight = context.packedLight();
    }

    /** Writes one mesh vertex from {@link #quadPosition} and {@link #normalScratch}. */
    private void emitMeshVertex(VertexConsumer buffer, float u, float v) {
        buffer.vertex(
                quadPosition.x(),
                quadPosition.y(),
                quadPosition.z(),
                meshRed,
                meshGreen,
                meshBlue,
                meshAlpha,
                u * meshUScale,
                v * meshVScale,
                meshOverlay,
                meshLight,
                normalScratch.x(),
                normalScratch.y(),
                normalScratch.z()
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

        // Indexed rather than for-each: the list iterator was allocated for every bone on every frame.
        List<AzBone> children = bone.getChildBones();

        for (int i = 0, size = children.size(); i < size; i++) {
            renderRecursively(context, children.get(i), isReRender);
        }
    }

    /**
     * Renders an individual {@link GeoCube}.<br>
     * This tends to be called recursively from something like {@link AzModelRenderer#renderCubesOfBone}
     */
    protected void renderCube(AzRendererPipelineContext<K, T> context, GeoCube cube) {
        PoseStack.Pose poseEntry = context.poseStack().last();
        GeoCube.Transform transform = cube.transform();
        Matrix4f poseMatrix;
        Matrix3f normalMatrix;

        if (transform.identity()) {
            poseMatrix = poseEntry.pose();
            normalMatrix = poseEntry.normal();
        } else {
            // The baked transforms are shared by every renderer, so multiply into scratch copies, never into them.
            poseStateCache.load(poseEntry.pose());
            poseStateCache.multiply(transform.pose());
            normalStateCache.load(poseEntry.normal());
            normalStateCache.mul(transform.normal());
            poseMatrix = poseStateCache;
            normalMatrix = normalStateCache;
        }

        int normalFlips = cube.normalFlips();
        // Quads drawn with a bone texture override use that texture's UVs, which a filter for the main texture can't
        // judge, so those are always drawn.
        AzQuadFilter quadFilter = context.getTextureOverride() == null ? context.quadFilter() : null;

        for (GeoQuad quad : cube.quads()) {
            if (quad == null || (quadFilter != null && !passes(quad, quadFilter))) {
                continue;
            }

            Vector3f quadNormal = quad.normal();

            if (normalFlips != 0) {
                // Quad normals are shared baked data, so flip a copy.
                flatNormalScratch.set(quadNormal.x(), quadNormal.y(), quadNormal.z());
                quadNormal = flatNormalScratch;
                RenderUtils.fixInvertedFlatCube(normalFlips, quadNormal);
            }

            createVerticesOfQuad(context, quad, poseMatrix, normalMatrix, quadNormal);
        }
    }

    private static boolean passes(GeoQuad quad, AzQuadFilter filter) {
        GeoVertex[] vertices = quad.vertices();
        float minU = vertices[0].texU(), maxU = minU, minV = vertices[0].texV(), maxV = minV;

        for (int i = 1; i < vertices.length; i++) {
            float u = vertices[i].texU();
            float v = vertices[i].texV();
            minU = Math.min(minU, u);
            maxU = Math.max(maxU, u);
            minV = Math.min(minV, v);
            maxV = Math.max(maxV, v);
        }

        return filter.test(minU, minV, maxU, maxV);
    }

    /**
     * Applies the {@link GeoQuad Quad's} {@link GeoVertex vertices} to the given {@link VertexConsumer buffer} for
     * rendering
     */
    protected void createVerticesOfQuad(
        AzRendererPipelineContext<K, T> context,
        GeoQuad quad,
        Matrix4f poseMatrix,
        Matrix3f normalMatrix,
        Vector3f normal
    ) {
        VertexConsumer buffer = context.vertexConsumer();
        int packedOverlay = context.packedOverlay();
        int packedLight = context.packedLight();
        ResourceLocation textureOverride = context.getTextureOverride();
        IntIntPair boneTextureSize = textureOverride != null ? context.computeTextureSize(textureOverride) : null;
        boolean useOverride = textureOverride != null && boneTextureSize != null && entityTextureSize != null;
        float uScale = useOverride ? (float) entityTextureSize.firstInt() / boneTextureSize.firstInt() : 1f;
        float vScale = useOverride ? (float) entityTextureSize.secondInt() / boneTextureSize.secondInt() : 1f;

        normalScratch.set(normal.x(), normal.y(), normal.z());
        normalScratch.transform(normalMatrix);

        float nx = normalScratch.x(), ny = normalScratch.y(), nz = normalScratch.z();
        float red = context.red(), green = context.green(), blue = context.blue(), alpha = context.alpha();

        for (GeoVertex vertex : quad.vertices()) {
            Vector3f position = vertex.position();

            quadPosition.set(position.x(), position.y(), position.z(), 1.0F);
            quadPosition.transform(poseMatrix);

            float u = useOverride ? vertex.texU() * uScale : vertex.texU();
            float v = useOverride ? vertex.texV() * vScale : vertex.texV();

            buffer.vertex(
                quadPosition.x(),
                quadPosition.y(),
                quadPosition.z(),
                red,
                green,
                blue,
                alpha,
                u,
                v,
                packedOverlay,
                packedLight,
                nx,
                ny,
                nz
            );
        }
    }

    /**
     * Provides an override logic for rendering a specific bone. This method allows custom handling of how an individual
     * bone should be rendered.
     *
     * @param poseStack     The pose stack used for managing transformations during rendering.
     * @param bone          The bone targeted for rendering or customization.
     * @param bufferSource  The source of buffers used in the rendering process.
     * @param buffer        The vertex consumer to which the rendering data is submitted.
     * @param partialTick   The partial tick for interpolating transformations and animations.
     * @param packedLight   The packed lightmap coordinates for lighting calculations.
     * @param packedOverlay The packed overlay coordinates for additional rendering effects.
     * @param red           The red color multiplier applied to the bone's rendering.
     * @param green         The green color multiplier applied to the bone's rendering.
     * @param blue          The blue color multiplier applied to the bone's rendering.
     * @param alpha         The alpha/transparency value applied to the bone's rendering.
     * @return A boolean indicating whether this override has applied custom rendering logic. If true, the custom
     *         behavior has been applied; otherwise, the default rendering will proceed.
     */
    public boolean boneRenderOverride(
        PoseStack poseStack,
        AzBone bone,
        MultiBufferSource bufferSource,
        VertexConsumer buffer,
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
     * Determines if a specific bone should override the default render type. This method can be used to apply custom
     * rendering behavior to individual bones by specifying a different {@link RenderType}.
     *
     * @param bone         The bone that is being considered for a render type override.
     * @param animatable   The animation-related object associated with this renderer, typically an entity or
     *                     geo-animatable instance.
     * @param texturePath  The resource location of the texture associated with the bone.
     * @param bufferSource The buffer source used for rendering operations.
     * @param partialTick  The partial tick progress for interpolating
     */
    @Nullable
    public RenderType getRenderTypeOverrideForBone(
        AzBone bone,
        T animatable,
        ResourceLocation texturePath,
        MultiBufferSource bufferSource,
        float partialTick
    ) {
        return null;
    }

    /**
     * Retrieves a texture override for a specific bone during rendering, if applicable. This method can be used to
     * specify a custom texture for individual bones.
     *
     * @param bone        The bone for which the texture override is being requested.
     * @param animatable  The animation-related object associated with the renderer, typically an entity or
     *                    geo-animatable instance.
     * @param partialTick The partial tick progress for interpolating animations.
     * @return A {@link ResourceLocation} pointing to the overridden texture for the specified bone, or null if no
     *         override is applied.
     */
    @Nullable
    public ResourceLocation getTextureOverrideForBone(AzBone bone, T animatable, float partialTick) {
        return null;
    }

    public void handleAnimation(AzAnimator<K, T> animator, T animatable, float partialTick) {
        animator.animate(animatable, partialTick);
    }

    /**
     * Retrieves or refreshes the {@link VertexConsumer} for rendering based on the current buffer state and rendering
     * context. Depending on the type and state of the current {@link VertexConsumer}, this method determines whether to
     * reuse the existing buffer or obtain a fresh one from the {@link MultiBufferSource}.
     *
     * @param context    The rendering context containing information about the current buffer, the buffer source, and
     *                   rendering pipeline data.
     * @param bone       The {@link AzBone} being rendered, which may influence the behavior or context of the buffer
     *                   retrieval.
     * @param renderType The {@link RenderType} specifying the desired render characteristics or pipeline for rendering.
     * @return The appropriate {@link VertexConsumer} for rendering, either the existing buffer or a refreshed/new one.
     */
    public VertexConsumer getOrRefreshBufferRenderType(
        AzItemRendererPipelineContext context,
        AzBone bone,
        RenderType renderType
    ) {
        // AzBufferSource consumers never go stale on 1.7.10, so there is nothing to refresh.
        return context.multiBufferSource().getBuffer(renderType);
    }

    public VertexConsumer getOrRefreshRenderBuffer(
        boolean isReRender,
        AzRendererPipelineContext<K, T> context,
        AzBone bone
    ) {
        AzRendererConfig<K, T> config = rendererPipeline.config();
        MultiBufferSource bufferSource = context.multiBufferSource();
        T animatable = context.animatable();

        ResourceLocation texture = config.boneTextureOverrideProvider(animatable, bone);
        RenderType renderTypeOverride = config.boneRenderTypeOverrideProvider(animatable, bone);

        context.setTextureOverride(texture);

        if (texture != null && renderTypeOverride == null) {
            ResourceLocation baseTexture = config.textureLocation(context.currentEntity(), animatable);
            RenderType baseRenderType = config.getRenderType(context.currentEntity(), animatable);

            renderTypeOverride = context.getDefaultRenderType(
                animatable,
                texture,
                bufferSource,
                context.partialTick(),
                retargetRenderType(baseRenderType, baseTexture, texture),
                config.alpha(animatable)
            );
        }

        if (renderTypeOverride != null) {
            passBufferDisplaced = true;
            return bufferSource.getBuffer(renderTypeOverride);
        }

        VertexConsumer buffer = passBuffer != null ? passBuffer : context.vertexConsumer();
        RenderType renderType = passRenderType != null ? passRenderType : context.renderType();

        if (buffer == null || renderType == null) {
            return buffer;
        }

        if (passBuffer != null) {
            restorePassBuffer(bufferSource);
        }

        VertexConsumer refreshed = refreshBuffer(buffer, bufferSource, renderType);

        if (refreshed != buffer && passBuffer != null) {
            passBuffer = refreshed;
        }

        return refreshed;
    }

    /**
     * If an override bone took the shared buffer over for its own render type, switches it back to the pass's render
     * type. The pass buffer holds the same {@link BufferBuilder} instance, so asking the buffer source for the pass's
     * render type is enough: it ends the override's batch and restarts the shared builder for this pass, and every
     * wrapper around it (outline, foil, sprite) keeps working.
     */
    private void restorePassBuffer(MultiBufferSource bufferSource) {
        if (passBufferDisplaced && passRenderType != null) {
            bufferSource.getBuffer(passRenderType);
        }

        passBufferDisplaced = false;
    }

    /**
     * Returns {@code buffer}, or a fresh buffer for {@code renderType} in its place if the batch it was writing to has
     * been closed.
     */
    /**
     * On modern versions a buffer can be ended underneath the renderer (e.g. when a layer renders vanilla items), and
     * has to be re-fetched. {@link mod.azure.azurelib.render.vertex.AzBufferSource AzBufferSource}'s consumers stay
     * valid for the whole frame, so this simply returns the buffer.
     */
    protected VertexConsumer refreshBuffer(
        VertexConsumer buffer,
        MultiBufferSource bufferSource,
        RenderType renderType
    ) {
        return buffer;
    }

    protected RenderType retargetRenderType(
        @Nullable RenderType renderType,
        @Nullable ResourceLocation baseTexture,
        ResourceLocation newTexture
    ) {
        if (newTexture.equals(baseTexture)) {
            return renderType != null ? renderType : RenderType.entityCutout(newTexture);
        }

        if (renderType != null && baseTexture != null) {
            if (renderType == RenderType.entityCutoutNoCull(baseTexture)) {
                return RenderType.entityCutoutNoCull(newTexture);
            }

            if (renderType == RenderType.entityTranslucent(baseTexture)) {
                return RenderType.entityTranslucent(newTexture);
            }

            if (renderType == RenderType.entityTranslucentCull(baseTexture)) {
                return RenderType.entityTranslucentCull(newTexture);
            }

            if (renderType == RenderType.entitySolid(baseTexture)) {
                return RenderType.entitySolid(newTexture);
            }

            if (renderType == RenderType.armorCutoutNoCull(baseTexture)) {
                return RenderType.armorCutoutNoCull(newTexture);
            }
        }

        return RenderType.entityCutout(newTexture);
    }

    /**
     * Determines whether the given {@link VertexConsumer} requires a buffer refresh. This involves checking the
     * specific type of the {@link VertexConsumer} and applying appropriate logic to evaluate its state.
     *
     * @param buffer The {@link VertexConsumer} instance to evaluate.
     * @return {@code true} if the buffer needs to be refreshed; {@code false} otherwise.
     */
    protected boolean needsBufferRefresh(VertexConsumer buffer) {
        return false;
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
