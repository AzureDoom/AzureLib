package mod.azure.azurelib.common.hitbox;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public record AzHitbox(
    String name,
    String bone,
    float sizeX,
    float sizeY,
    float sizeZ,
    float offsetX,
    float offsetY,
    float offsetZ,
    float inflate,
    boolean anchorAtPivot,
    float damageMultiplier,
    Set<String> tags,
    AzHitboxRole role
) {

    public AzHitbox {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(bone, "bone");
        Objects.requireNonNull(role, "role");
        tags = Set.copyOf(tags);
    }

    public static boolean isPickable(AzHitbox hitbox) {
        return hitbox.role().isPickable();
    }

    public static boolean blocksProjectiles(AzHitbox hitbox) {
        return hitbox.role().blocksProjectiles();
    }

    public static Builder builder(String bone) {
        return new Builder(bone, bone);
    }

    public static Builder builder(String name, String bone) {
        return new Builder(name, bone);
    }

    public boolean fitsCubes() {
        return Float.isNaN(sizeX);
    }

    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    public static final class Builder {

        private final String name;

        private final String bone;

        private float sizeX = Float.NaN, sizeY = Float.NaN, sizeZ = Float.NaN;

        private float offsetX, offsetY, offsetZ;

        private float inflate;

        private boolean anchorAtPivot;

        private float damageMultiplier = 1f;

        private final Set<String> tags = new LinkedHashSet<>();

        private AzHitboxRole role = AzHitboxRole.COMBAT;

        private Builder(String name, String bone) {
            this.name = name;
            this.bone = bone;
        }

        public Builder size(float x, float y, float z) {
            if (!(x >= 0 && y >= 0 && z >= 0)) {
                throw new IllegalArgumentException("Hitbox size must be non-negative: " + x + ", " + y + ", " + z);
            }

            this.sizeX = x;
            this.sizeY = y;
            this.sizeZ = z;
            return this;
        }

        public Builder size(float size) {
            return size(size, size, size);
        }

        public Builder offset(float x, float y, float z) {
            this.offsetX = x;
            this.offsetY = y;
            this.offsetZ = z;
            return this;
        }

        public Builder inflate(float pixels) {
            this.inflate = pixels;
            return this;
        }

        public Builder anchorAtPivot() {
            this.anchorAtPivot = true;
            return this;
        }

        public Builder damageMultiplier(float multiplier) {
            this.damageMultiplier = multiplier;
            return this;
        }

        public Builder tag(String tag) {
            this.tags.add(tag);
            return this;
        }

        public Builder role(AzHitboxRole role) {
            this.role = role;
            return this;
        }

        public Builder interact() {
            return role(AzHitboxRole.INTERACT);
        }

        public Builder anchor() {
            return role(AzHitboxRole.ANCHOR);
        }

        public AzHitbox build() {
            return new AzHitbox(
                name,
                bone,
                sizeX,
                sizeY,
                sizeZ,
                offsetX,
                offsetY,
                offsetZ,
                inflate,
                anchorAtPivot,
                damageMultiplier,
                tags,
                role
            );
        }
    }
}
