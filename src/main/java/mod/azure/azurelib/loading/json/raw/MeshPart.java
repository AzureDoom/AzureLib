package mod.azure.azurelib.loading.json.raw;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.Objects;

import mod.azure.azurelib.util.GsonHelper;

public final class MeshPart {

    public static final String KEY = "azurelib:mesh_parts";

    public static final double DEFAULT_SMOOTH_ANGLE = 60;

    private final String name;

    private final boolean smooth;

    private final double smoothAngle;

    private final int start;

    private final int count;

    public MeshPart(
        String name,
        boolean smooth,
        double smoothAngle,
        int start,
        int count
    ) {
        this.name = name;
        this.smooth = smooth;
        this.smoothAngle = smoothAngle;
        this.start = start;
        this.count = count;
    }

    public String name() {
        return this.name;
    }

    public boolean smooth() {
        return this.smooth;
    }

    public double smoothAngle() {
        return this.smoothAngle;
    }

    public int start() {
        return this.start;
    }

    public int count() {
        return this.count;
    }

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

        return size == parts.length
            ? parts
            : Arrays.copyOf(parts, size);
    }

    private static MeshPart read(JsonObject obj) {
        JsonArray polys = GsonHelper.getAsJsonArray(
            obj,
            "polys",
            null
        );

        if (polys == null || polys.size() < 2) {
            return null;
        }

        int start = polys.get(0).getAsInt();
        int count = polys.get(1).getAsInt();

        if (start < 0 || count <= 0) {
            return null;
        }

        String shading = GsonHelper.getAsString(
            obj,
            "shading",
            "flat"
        );

        double angle = GsonHelper.getAsDouble(
            obj,
            "smooth_angle",
            DEFAULT_SMOOTH_ANGLE
        );

        return new MeshPart(
            GsonHelper.getAsString(obj, "name", null),
            "smooth".equalsIgnoreCase(shading),
            Math.max(0, Math.min(180, angle)),
            start,
            count
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof MeshPart)) {
            return false;
        }

        MeshPart other = (MeshPart) obj;

        return Objects.equals(this.name, other.name)
            && this.smooth == other.smooth
            && Double.compare(this.smoothAngle, other.smoothAngle) == 0
            && this.start == other.start
            && this.count == other.count;
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(this.name);
        result = 31 * result + Boolean.hashCode(this.smooth);
        result = 31 * result + Double.hashCode(this.smoothAngle);
        result = 31 * result + Integer.hashCode(this.start);
        result = 31 * result + Integer.hashCode(this.count);

        return result;
    }

    @Override
    public String toString() {
        return "MeshPart["
            + "name=" + this.name
            + ", smooth=" + this.smooth
            + ", smoothAngle=" + this.smoothAngle
            + ", start=" + this.start
            + ", count=" + this.count
            + "]";
    }
}
