/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.loading.json.raw;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;

/**
 * Container class for the {@code polys} entry of a {@link PolyMesh}, only used in deserialization at startup.
 * <p>
 * Either an explicit polygon list or one of the {@link Type} shorthands. In the explicit form each polygon is a list of
 * three or more corners, and each corner is {@code [positionIndex, normalIndex, uvIndex]}. Polygons may have different
 * corner counts, so triangles and quads can be mixed in one mesh.
 * </p>
 */
public final class PolysUnion {

    private final int[][][] union;

    private final Type type;

    /**
     * @param union the explicit polygons ({@code union[polygon][corner][attribute]}), empty when {@code type} is set
     * @param type  the shorthand form, or null for explicit polygons
     */
    public PolysUnion(int[][][] union, Type type) {
        this.union = union;
        this.type = type;
    }

    public int[][][] union() {
        return union;
    }

    public Type type() {
        return type;
    }

    public static JsonDeserializer<PolysUnion> deserializer() throws JsonParseException {
        return (json, type, context) -> {
            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                Type shorthand = Type.fromName(json.getAsString());

                if (shorthand == null) {
                    throw new JsonParseException(
                        "polys: unknown shorthand '" + json.getAsString() + "', expected tri_list or quad_list"
                    );
                }

                return new PolysUnion(new int[0][][], shorthand);
            }

            if (!json.isJsonArray()) {
                throw new JsonParseException("Invalid format for PolysUnion, must be either string or array");
            }

            JsonArray polygons = json.getAsJsonArray();
            MeshValidation.checkCount("polys", polygons.size(), MeshValidation.MAX_POLYGONS);
            int[][][] union = new int[polygons.size()][][];

            for (int p = 0; p < polygons.size(); p++) {
                String polygon = "polys[" + p + "]";
                JsonArray corners = MeshValidation.array(polygons.get(p), polygon);

                if (corners.size() < 3) {
                    throw MeshValidation.error(polygon, "needs at least 3 corners, got " + corners.size());
                }

                MeshValidation.checkCount(polygon, corners.size(), MeshValidation.MAX_POLYGON_CORNERS);
                union[p] = new int[corners.size()][];

                for (int c = 0; c < corners.size(); c++) {
                    String at = polygon + "[" + c + "]";
                    JsonArray attributes = MeshValidation.array(corners.get(c), at);

                    if (attributes.size() == 0) {
                        throw MeshValidation.error(at, "must list at least a position index");
                    }

                    int[] corner = new int[3];

                    for (int a = 0; a < 3; a++) {
                        // A missing normal or uv index reuses the position index, like the list shorthands.
                        corner[a] = a < attributes.size() ? MeshValidation.index(attributes.get(a), at) : corner[0];
                    }

                    union[p][c] = corner;
                }
            }

            return new PolysUnion(union, null);
        };
    }

    public enum Type {

        QUAD("quad_list"),
        TRI("tri_list");

        private final String serializedName;

        Type(String serializedName) {
            this.serializedName = serializedName;
        }

        public int cornerCount() {
            return this == QUAD ? 4 : 3;
        }

        public String serializedName() {
            return serializedName;
        }

        public static Type fromName(String name) {
            for (Type value : values()) {
                if (value.serializedName.equals(name)) {
                    return value;
                }
            }

            return null;
        }
    }
}
