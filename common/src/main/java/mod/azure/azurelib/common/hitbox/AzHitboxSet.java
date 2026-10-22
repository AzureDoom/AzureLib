package mod.azure.azurelib.common.hitbox;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

public final class AzHitboxSet {

    public static final int MAX_HITBOXES = 64;

    private final List<AzHitbox> hitboxes;

    private final Map<String, Integer> indexByName;

    private final boolean keepEntityBox;

    private final boolean serverSync;

    private final int reportIntervalTicks;

    private final int poseTimeoutTicks;

    private final float maxReach;

    private final float maxHalfExtent;

    private AzHitboxSet(Builder builder) {
        this.hitboxes = List.copyOf(builder.hitboxes);
        var map = new HashMap<String, Integer>();

        for (int i = 0; i < hitboxes.size(); i++) {
            if (map.put(hitboxes.get(i).name(), i) != null) {
                throw new IllegalArgumentException("Duplicate hitbox name: " + hitboxes.get(i).name());
            }
        }

        this.indexByName = Collections.unmodifiableMap(map);
        this.keepEntityBox = builder.keepEntityBox;
        this.serverSync = builder.serverSync;
        this.reportIntervalTicks = builder.reportIntervalTicks;
        this.poseTimeoutTicks = builder.poseTimeoutTicks;
        this.maxReach = builder.maxReach;
        this.maxHalfExtent = builder.maxHalfExtent;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int size() {
        return hitboxes.size();
    }

    public boolean isEmpty() {
        return hitboxes.isEmpty();
    }

    public AzHitbox get(int index) {
        return hitboxes.get(index);
    }

    public List<AzHitbox> hitboxes() {
        return hitboxes;
    }

    public int indexOf(String name) {
        var index = indexByName.get(name);
        return index == null ? -1 : index;
    }

    public @Nullable AzHitbox byName(String name) {
        var index = indexOf(name);
        return index < 0 ? null : hitboxes.get(index);
    }

    public boolean keepEntityBox() {
        return keepEntityBox;
    }

    public boolean serverSync() {
        return serverSync;
    }

    public int reportIntervalTicks() {
        return reportIntervalTicks;
    }

    public int poseTimeoutTicks() {
        return poseTimeoutTicks;
    }

    public float maxHalfExtent() {
        return maxHalfExtent;
    }

    public double resolveMaxReach(Entity entity) {
        if (maxReach > 0) {
            return maxReach;
        }

        return Math.max(4.0, 3.0 * Math.max(entity.getBbWidth(), entity.getBbHeight()));
    }

    public static final class Builder {

        private final List<AzHitbox> hitboxes = new ArrayList<>();

        private boolean keepEntityBox = false;

        private boolean serverSync = true;

        private int reportIntervalTicks = 2;

        private int poseTimeoutTicks = 10;

        private float maxReach = -1;

        private float maxHalfExtent = 4f;

        private Builder() {}

        public Builder add(String bone) {
            return add(AzHitbox.builder(bone).build());
        }

        public Builder add(String bone, UnaryOperator<AzHitbox.Builder> configure) {
            return add(configure.apply(AzHitbox.builder(bone)).build());
        }

        public Builder add(String name, String bone, UnaryOperator<AzHitbox.Builder> configure) {
            return add(configure.apply(AzHitbox.builder(name, bone)).build());
        }

        public Builder add(AzHitbox hitbox) {
            if (hitboxes.size() >= MAX_HITBOXES) {
                throw new IllegalStateException("An AzHitboxSet can hold at most " + MAX_HITBOXES + " hitboxes");
            }

            hitboxes.add(hitbox);
            return this;
        }

        public Builder keepEntityBox(boolean keep) {
            this.keepEntityBox = keep;
            return this;
        }

        public Builder clientOnly() {
            this.serverSync = false;
            return this;
        }

        public Builder reportInterval(int ticks) {
            this.reportIntervalTicks = Math.max(1, ticks);
            return this;
        }

        public Builder poseTimeout(int ticks) {
            this.poseTimeoutTicks = Math.max(1, ticks);
            return this;
        }

        public Builder maxReach(float blocks) {
            this.maxReach = blocks;
            return this;
        }

        public Builder maxHalfExtent(float blocks) {
            this.maxHalfExtent = blocks;
            return this;
        }

        public AzHitboxSet build() {
            return new AzHitboxSet(this);
        }
    }
}
