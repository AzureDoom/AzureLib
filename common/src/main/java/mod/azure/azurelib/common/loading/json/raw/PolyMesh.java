/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.loading.json.raw;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.common.util.JsonUtil;

/**
 * Container class for Bedrock {@code poly_mesh} information, only used in deserialization at startup.
 * <p>
 * Positions are in Bedrock geometry space (pixels, X mirrored, absolute like cube origins). UVs are in texture pixels
 * with a top-left origin unless {@code normalized_uvs} is true, in which case they are 0-1.
 * </p>
 */
public record PolyMesh(
    @Nullable Boolean normalizedUVs,
    double[][] normals,
    @Nullable PolysUnion polysUnion,
    double[][] positions,
    double[][] uvs
) {

    public boolean usesNormalizedUVs() {
        return normalizedUVs == Boolean.TRUE;
    }

    public static JsonDeserializer<PolyMesh> deserializer() throws JsonParseException {
        return (json, type, context) -> read(json.getAsJsonObject(), context);
    }

    static PolyMesh read(JsonObject obj, JsonDeserializationContext context) {
        Boolean normalizedUVs = JsonUtil.getOptionalBoolean(obj, "normalized_uvs");
        double[][] positions = MeshValidation.vectors(
            obj.get("positions"),
            "positions",
            3,
            MeshValidation.MAX_POSITIONS
        );
        double[][] normals = MeshValidation.vectors(obj.get("normals"), "normals", 3, MeshValidation.MAX_ATTRIBUTES);
        double[][] uvs = MeshValidation.vectors(obj.get("uvs"), "uvs", 2, MeshValidation.MAX_ATTRIBUTES);
        PolysUnion polys = obj.has("polys") ? context.deserialize(obj.get("polys"), PolysUnion.class) : null;
        PolyMesh mesh = new PolyMesh(normalizedUVs, normals, polys, positions, uvs);

        mesh.validate();
        return mesh;
    }

    public void validate() {
        if (polysUnion == null) {
            return;
        }

        if (polysUnion.type() != null) {
            int size = polysUnion.type().cornerCount();

            if (positions.length % size != 0) {
                throw MeshValidation.error(
                    "polys",
                    polysUnion.type().name().toLowerCase() + "_list needs a multiple of " + size + " positions, got "
                        + positions.length
                );
            }

            checkParallel("normals", normals.length);
            checkParallel("uvs", uvs.length);
            return;
        }

        int[][][] polygons = polysUnion.union();

        for (int p = 0; p < polygons.length; p++) {
            for (int c = 0; c < polygons[p].length; c++) {
                int[] corner = polygons[p][c];
                String at = "polys[" + p + "][" + c + "]";

                MeshValidation.checkIndex(corner[0], positions.length, at, "position");

                if (normals.length > 0) {
                    MeshValidation.checkIndex(corner[1], normals.length, at, "normal");
                }

                if (uvs.length > 0) {
                    MeshValidation.checkIndex(corner[2], uvs.length, at, "uv");
                }
            }
        }
    }

    private void checkParallel(String what, int count) {
        if (count > 0 && count < positions.length) {
            throw MeshValidation.error(
                what,
                "has " + count + " entries but the list shorthand needs one per position (" + positions.length + ")"
            );
        }
    }
}
