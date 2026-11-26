package mod.azure.azurelib.loading.json.raw;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public record MeshPart(
    @Nullable String name,
    boolean smooth,
    double smoothAngle,
    int start,
    int count
) {

    public static final String KEY = "azurelib:mesh_parts";

    public static final double DEFAULT_SMOOTH_ANGLE = 60;

    public static MeshPart[] readAll(JsonObject owner) {
        if (!owner.has(KEY) || !owner.get(KEY).isJsonArray()) {
            return new MeshPart[0];
        }

        JsonArray array = owner.getAsJsonArray(KEY);
        MeshPart[] parts = new MeshPart[array.size()];
        int size = 0;

        for (JsonElement element : array) {
            if (element.isJsonObject()) {
                MeshPart part = read(element.getAsJsonObject());

                if (part != null) {
                    parts[size++] = part;
                }
            }
        }

        return size == parts.length ? parts : Arrays.copyOf(parts, size);
    }

    private static @Nullable MeshPart read(JsonObject obj) {
        JsonArray polys = GsonHelper.getAsJsonArray(obj, "polys", null);

        if (polys == null || polys.size() < 2) {
            return null;
        }

        int start = polys.get(0).getAsInt();
        int count = polys.get(1).getAsInt();

        if (start < 0 || count <= 0) {
            return null;
        }

        String shading = GsonHelper.getAsString(obj, "shading", "flat");
        double angle = GsonHelper.getAsDouble(obj, "smooth_angle", DEFAULT_SMOOTH_ANGLE);

        return new MeshPart(
            GsonHelper.getAsString(obj, "name", null),
            "smooth".equalsIgnoreCase(shading),
            Math.max(0, Math.min(180, angle)),
            start,
            count
        );
    }
}
