package mod.azure.azurelib.model.factory;

import javax.annotation.Nullable;

import mod.azure.azurelib.cache.object.GeoCube;
import mod.azure.azurelib.cache.object.GeoQuad;
import mod.azure.azurelib.cache.object.GeoVertex;
import mod.azure.azurelib.loading.json.raw.Cube;
import mod.azure.azurelib.loading.json.raw.FaceUV;
import mod.azure.azurelib.loading.json.raw.ModelProperties;
import mod.azure.azurelib.loading.json.raw.UVUnion;
import mod.azure.azurelib.loading.object.BoneStructure;
import mod.azure.azurelib.loading.object.GeometryTree;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.model.factory.primitive.VertexSet;
import mod.azure.azurelib.util.math.Direction;
import mod.azure.azurelib.util.math.Vec3;
import mod.azure.azurelib.util.math.Vector3f;

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
     * Returns {@code null} for a quad with no area, so it is never rendered. Flat cubes (one dimension of zero) get
     * these on the four faces along their thin edge; they draw nothing but still cost four vertices each in every pass.
     */
    protected static GeoQuad dropIfDegenerate(GeoQuad quad) {
        if (quad == null) {
            return null;
        }

        GeoVertex[] vertices = quad.vertices();
        Vector3f p0 = vertices[0].position();
        Vector3f p1 = vertices[1].position();
        Vector3f p2 = vertices[2].position();
        Vector3f p3 = vertices[3].position();

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
        double[][] uvData;
        switch ((direction)) {
            case WEST:
                uvData = new double[][] {
                    new double[] { uv[0] + uvSizeVec.z + uvSizeVec.x, uv[1] + uvSizeVec.z },
                    new double[] { uvSizeVec.z, uvSizeVec.y }
                };
                break;
            case EAST:
                uvData = new double[][] {
                    new double[] { uv[0], uv[1] + uvSizeVec.z },
                    new double[] { uvSizeVec.z, uvSizeVec.y }
                };
                break;
            case NORTH:
                uvData = new double[][] {
                    new double[] { uv[0] + uvSizeVec.z, uv[1] + uvSizeVec.z },
                    new double[] { uvSizeVec.x, uvSizeVec.y }
                };
                break;
            case SOUTH:
                uvData = new double[][] {
                    new double[] { uv[0] + uvSizeVec.z + uvSizeVec.x + uvSizeVec.z, uv[1] + uvSizeVec.z },
                    new double[] { uvSizeVec.x, uvSizeVec.y }
                };
                break;
            case UP:
                uvData = new double[][] {
                    new double[] { uv[0] + uvSizeVec.z, uv[1] },
                    new double[] { uvSizeVec.x, uvSizeVec.z }
                };
                break;
            case DOWN:
                uvData = new double[][] {
                    new double[] { uv[0] + uvSizeVec.z + uvSizeVec.x, uv[1] + uvSizeVec.z },
                    new double[] { uvSizeVec.x, -uvSizeVec.z }
                };
                break;
            default:
                throw new IllegalStateException();
        }

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
