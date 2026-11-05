package mod.azure.azurelib.common.model.factory;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.util.ArrayDeque;
import java.util.List;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.common.cache.object.GeoCube;
import mod.azure.azurelib.common.cache.object.GeoMesh;
import mod.azure.azurelib.common.cache.object.GeoQuad;
import mod.azure.azurelib.common.cache.object.GeoSkinnedMesh;
import mod.azure.azurelib.common.loading.json.raw.Cube;
import mod.azure.azurelib.common.loading.json.raw.FaceUV;
import mod.azure.azurelib.common.loading.json.raw.ModelProperties;
import mod.azure.azurelib.common.loading.json.raw.PolyMesh;
import mod.azure.azurelib.common.loading.json.raw.SkinnedMesh;
import mod.azure.azurelib.common.loading.json.raw.UVUnion;
import mod.azure.azurelib.common.loading.object.BoneStructure;
import mod.azure.azurelib.common.loading.object.GeometryTree;
import mod.azure.azurelib.common.model.AzBakedModel;
import mod.azure.azurelib.common.model.AzBone;
import mod.azure.azurelib.common.model.AzRestPose;
import mod.azure.azurelib.common.model.factory.primitive.MeshPolygons;
import mod.azure.azurelib.common.model.factory.primitive.VertexSet;

/**
 * Abstract factory class for constructing baked models, bones, and cubes from raw input data such as geometry trees or
 * properties. This class provides a structure for defining the creation of complex 3D models, including managing
 * hierarchical relationships between bones and their associated components.
 */
public abstract class AzBakedModelFactory {

    /**
     * Construct the output model from the given {@link GeometryTree}.<br>
     */
    public abstract AzBakedModel constructGeoModel(GeometryTree geometryTree);

    /**
     * Construct a {@link AzBone} from the relevant raw input data
     *
     * @param boneStructure The {@code BoneStructure} comprising the structure of the bone and its children
     * @param properties    The loaded properties for the model
     * @param parent        The parent bone for this bone, or null if a top-level bone
     */
    public abstract AzBone constructBone(
        BoneStructure boneStructure,
        ModelProperties properties,
        @Nullable AzBone parent
    );

    /**
     * Construct a {@link GeoCube} from the relevant raw input data
     *
     * @param cube       The raw {@code Cube} comprising the structure and properties of the cube
     * @param properties The loaded properties for the model
     * @param bone       The bone this cube belongs to
     */
    public abstract GeoCube constructCube(Cube cube, ModelProperties properties, AzBone bone);

    /**
     * Construct a rigid {@link GeoMesh} from a bone's Bedrock {@code poly_mesh}. Positions are in the same absolute
     * bone space as cube origins.
     *
     * @return the mesh, or null if it has no drawable polygons
     */
    public @Nullable GeoMesh constructMesh(PolyMesh polyMesh, ModelProperties properties, AzBone bone) {
        MeshPolygons polygons = MeshPolygons.bake(
            polyMesh,
            (float) properties.textureWidth(),
            (float) properties.textureHeight()
        );
        GeoMesh mesh = GeoMesh.of(polygons);

        return mesh.isEmpty() ? null : mesh;
    }

    /**
     * Binds every skinned mesh in the bone tree to the geometry's rest pose, replacing each mesh with its bound copy.
     * Call this from {@link #constructGeoModel} once all bones are built; meshes left unbound don't render.
     * <p>
     * Joints the rest pose doesn't know (a name missing from the file) are bound to the owning bone's rest pose, which
     * is also what {@link AzBakedModel} falls back to when it resolves the joint, so those vertices stay rigid with the
     * owner.
     * </p>
     */
    public static void bindSkinnedMeshes(List<AzBone> topLevelBones, AzRestPose restPose) {
        ArrayDeque<AzBone> queue = new ArrayDeque<>(topLevelBones);

        while (!queue.isEmpty()) {
            AzBone bone = queue.poll();
            queue.addAll(bone.getChildBones());

            List<GeoSkinnedMesh> meshes = bone.getSkinnedMeshes();
            Matrix4fc ownerRest = restPose.modelMatrix(bone.getName());

            for (int m = 0; m < meshes.size(); m++) {
                GeoSkinnedMesh mesh = meshes.get(m);

                if (mesh.isBound()) {
                    continue;
                }

                Matrix4fc[] rest = new Matrix4fc[mesh.jointCount()];

                for (int i = 0; i < rest.length; i++) {
                    Matrix4fc jointRest = restPose.modelMatrix(mesh.joint(i));
                    rest[i] = jointRest != null ? jointRest : ownerRest != null ? ownerRest : IDENTITY;
                }

                if (ownerRest == null) {
                    AzureLib.LOGGER.warn(
                        "Skinned mesh '{}' is on bone '{}', which has no rest pose in its geometry",
                        mesh.name(),
                        bone.getName()
                    );
                }

                meshes.set(m, mesh.withBindPose(rest));
            }
        }
    }

    private static final Matrix4fc IDENTITY = new Matrix4f();

    /**
     * Construct a {@link GeoSkinnedMesh} from an {@code azurelib:skinned_meshes} entry. The mesh is unbound; it gets
     * its bind pose from {@link #bindSkinnedMeshes}. Influences are clamped to {@link GeoSkinnedMesh#MAX_INFLUENCES}
     * per position (strongest first) and renormalized; positions with no usable influence are bound to the first joint,
     * which the Blockbench plugin always writes as the armature itself.
     *
     * @return the mesh, or null if it has no drawable polygons or joints
     */
    public @Nullable GeoSkinnedMesh constructSkinnedMesh(
        SkinnedMesh skinnedMesh,
        ModelProperties properties,
        AzBone bone
    ) {
        String[] joints = skinnedMesh.joints();

        if (joints.length == 0) {
            return null;
        }

        MeshPolygons polygons = MeshPolygons.bake(
            skinnedMesh.mesh(),
            (float) properties.textureWidth(),
            (float) properties.textureHeight()
        );

        if (polygons.quadCount() == 0) {
            return null;
        }

        int max = GeoSkinnedMesh.MAX_INFLUENCES;
        int positionCount = polygons.positionCount();
        int[] influenceJoints = new int[positionCount * max];
        float[] influenceWeights = new float[positionCount * max];
        int[][] sourceJoints = skinnedMesh.influenceJoints();
        float[][] sourceWeights = skinnedMesh.influenceWeights();

        for (int p = 0; p < positionCount; p++) {
            int base = p * max;
            int count = 0;
            double total = 0d;

            if (p < sourceJoints.length) {
                int[] pj = sourceJoints[p];
                float[] pw = sourceWeights[p];

                for (int i = 0; i < pj.length; i++) {
                    int joint = pj[i];
                    float weight = pw[i];

                    if (joint < 0 || joint >= joints.length || !(weight > 0f)) {
                        continue;
                    }

                    if (count < max) {
                        influenceJoints[base + count] = joint;
                        influenceWeights[base + count] = weight;
                        count++;
                    } else {
                        int weakest = base;

                        for (int k = base + 1; k < base + max; k++) {
                            if (influenceWeights[k] < influenceWeights[weakest]) {
                                weakest = k;
                            }
                        }

                        if (weight > influenceWeights[weakest]) {
                            influenceJoints[weakest] = joint;
                            influenceWeights[weakest] = weight;
                        }
                    }
                }
            }

            for (int k = base; k < base + max; k++) {
                total += influenceWeights[k];
            }

            if (!(total > 0d)) {
                influenceJoints[base] = 0;
                influenceWeights[base] = 1f;
                continue;
            }

            for (int k = base; k < base + max; k++) {
                influenceWeights[k] = (float) (influenceWeights[k] / total);
            }
        }

        return new GeoSkinnedMesh(skinnedMesh.name(), joints, polygons, influenceJoints, influenceWeights);
    }

    /**
     * Returns {@code null} for a quad with no area, so it is never rendered. Flat cubes (one dimension of zero) get
     * these on the four faces along their thin edge; they draw nothing but still cost four vertices each in every pass.
     */
    protected static GeoQuad dropIfDegenerate(GeoQuad quad) {
        if (quad == null) {
            return null;
        }

        var vertices = quad.vertices();
        var p0 = vertices[0].position();
        var p1 = vertices[1].position();
        var p2 = vertices[2].position();
        var p3 = vertices[3].position();

        float d1x = p2.x() - p0.x(), d1y = p2.y() - p0.y(), d1z = p2.z() - p0.z();
        float d2x = p3.x() - p1.x(), d2y = p3.y() - p1.y(), d2z = p3.z() - p1.z();
        float cx = d1y * d2z - d1z * d2y;
        float cy = d1z * d2x - d1x * d2z;
        float cz = d1x * d2y - d1y * d2x;

        return cx * cx + cy * cy + cz * cz < 1.0e-20f ? null : quad;
    }

    /**
     * Builtin method to construct the quad list from the various vertices and related data, to make it easier.<br>
     * Vertices have already been mirrored here if {@code mirror} is true
     */
    public GeoQuad[] buildQuads(
        UVUnion uvUnion,
        VertexSet vertices,
        Cube cube,
        float textureWidth,
        float textureHeight,
        boolean mirror
    ) {
        GeoQuad[] quads = new GeoQuad[6];

        quads[0] = dropIfDegenerate(
            buildQuad(vertices, cube, uvUnion, textureWidth, textureHeight, mirror, Direction.WEST)
        );
        quads[1] = dropIfDegenerate(
            buildQuad(vertices, cube, uvUnion, textureWidth, textureHeight, mirror, Direction.EAST)
        );
        quads[2] = dropIfDegenerate(
            buildQuad(vertices, cube, uvUnion, textureWidth, textureHeight, mirror, Direction.NORTH)
        );
        quads[3] = dropIfDegenerate(
            buildQuad(vertices, cube, uvUnion, textureWidth, textureHeight, mirror, Direction.SOUTH)
        );
        quads[4] = dropIfDegenerate(
            buildQuad(vertices, cube, uvUnion, textureWidth, textureHeight, mirror, Direction.UP)
        );
        quads[5] = dropIfDegenerate(
            buildQuad(vertices, cube, uvUnion, textureWidth, textureHeight, mirror, Direction.DOWN)
        );

        return quads;
    }

    /**
     * Build an individual quad
     */
    public GeoQuad buildQuad(
        VertexSet vertices,
        Cube cube,
        UVUnion uvUnion,
        float textureWidth,
        float textureHeight,
        boolean mirror,
        Direction direction
    ) {
        if (!uvUnion.isBoxUV()) {
            FaceUV faceUV = uvUnion.faceUV().fromDirection(direction);

            if (faceUV == null)
                return null;

            return GeoQuad.build(
                vertices.verticesForQuad(direction, false, mirror || cube.mirror() == Boolean.TRUE),
                faceUV.uv(),
                faceUV.uvSize(),
                faceUV.uvRotation(),
                textureWidth,
                textureHeight,
                mirror,
                direction
            );
        }

        double[] uv = cube.uv().boxUVCoords();
        double[] uvSize = cube.size();
        Vec3 uvSizeVec = new Vec3(Math.floor(uvSize[0]), Math.floor(uvSize[1]), Math.floor(uvSize[2]));
        double[][] uvData = switch (direction) {
            case WEST -> new double[][] {
                new double[] { uv[0] + uvSizeVec.z + uvSizeVec.x, uv[1] + uvSizeVec.z },
                new double[] { uvSizeVec.z, uvSizeVec.y }
            };
            case EAST -> new double[][] {
                new double[] { uv[0], uv[1] + uvSizeVec.z },
                new double[] { uvSizeVec.z, uvSizeVec.y }
            };
            case NORTH -> new double[][] {
                new double[] { uv[0] + uvSizeVec.z, uv[1] + uvSizeVec.z },
                new double[] { uvSizeVec.x, uvSizeVec.y }
            };
            case SOUTH -> new double[][] {
                new double[] { uv[0] + uvSizeVec.z + uvSizeVec.x + uvSizeVec.z, uv[1] + uvSizeVec.z },
                new double[] { uvSizeVec.x, uvSizeVec.y }
            };
            case UP -> new double[][] {
                new double[] { uv[0] + uvSizeVec.z, uv[1] },
                new double[] { uvSizeVec.x, uvSizeVec.z }
            };
            case DOWN -> new double[][] {
                new double[] { uv[0] + uvSizeVec.z + uvSizeVec.x, uv[1] + uvSizeVec.z },
                new double[] { uvSizeVec.x, -uvSizeVec.z }
            };
        };

        return GeoQuad.build(
            vertices.verticesForQuad(direction, true, mirror || cube.mirror() == Boolean.TRUE),
            uvData[0],
            uvData[1],
            FaceUV.Rotation.NONE,
            textureWidth,
            textureHeight,
            mirror,
            direction
        );
    }
}
