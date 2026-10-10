package mod.azure.azurelib.util.client;

import net.minecraft.client.model.ModelRenderer;

import java.util.Map;
import java.util.WeakHashMap;

public final class ModelPartScaleStore {

    private static final Map<ModelRenderer, Scale> SCALES = new WeakHashMap<>();

    public static void setScale(ModelRenderer part, float x, float y, float z) {
        SCALES.put(part, new Scale(x, y, z));
    }

    public static Scale getScale(ModelRenderer part) {
        return SCALES.getOrDefault(part, Scale.ONE);
    }

    public static final class Scale {

        private final float x;

        private final float y;

        private final float z;

        public Scale(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public float x() {
            return this.x;
        }

        public float y() {
            return this.y;
        }

        public float z() {
            return this.z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof Scale))
                return false;
            Scale other = (Scale) o;
            return Float.compare(this.x, other.x) == 0
                && Float.compare(this.y, other.y) == 0
                && Float.compare(this.z, other.z) == 0;
        }

        @Override
        public int hashCode() {
            int result = 0;
            result = 31 * result + Float.hashCode(this.x);
            result = 31 * result + Float.hashCode(this.y);
            result = 31 * result + Float.hashCode(this.z);
            return result;
        }

        @Override
        public String toString() {
            return "Scale[x=" + this.x + ", y=" + this.y + ", z=" + this.z + "]";
        }

        public static final Scale ONE = new Scale(1.0F, 1.0F, 1.0F);
    }
}
