package mod.azure.azurelib.common.render.lod;

/**
 * Configures distance-based LOD thresholds for AzureLib entity rendering.
 * <p>
 * Two independent LOD systems are controlled here:
 * </p>
 * <ul>
 * <li><b>Bone LOD</b> — hides bones beyond a depth threshold past a given distance. Depth 0 = root bones, depth 1 =
 * their children, etc. Bones deeper than {@code boneLodDepth} are hidden when the entity is further than
 * {@code boneLodDistance} blocks from the camera.</li>
 * <li><b>Animation LOD</b> — reduces animation update frequency past a distance. Beyond {@code animLodDistance},
 * animations update once every {@code animLodTickInterval} ticks instead of every frame; frames in between reuse the
 * last pose.</li>
 * </ul>
 */
public final class AzLodConfig {

    public static final AzLodConfig DISABLED = new AzLodConfig(
        Double.MAX_VALUE,
        Integer.MAX_VALUE,
        Double.MAX_VALUE,
        1
    );

    /**
     * A built-in LOD preset: past 40 blocks, bones deeper than depth 3 are hidden; past 48 blocks, animations update
     * once every 2 ticks.
     * <p>
     * Not applied automatically. Renderers use {@link #DISABLED} unless you opt in with
     * {@code AzEntityRendererConfig.Builder#withLodConfig(AzLodConfig.DEFAULT)}, or with your own config from
     * {@link #builder()}.
     * </p>
     */
    public static final AzLodConfig DEFAULT = builder()
        .boneLod(40, 3)
        .animLod(48, 2)
        .build();

    private final double boneLodDistanceSq;

    private final int boneLodDepth;

    private final double animLodDistanceSq;

    private final int animLodTickInterval;

    private AzLodConfig(
        double boneLodDistanceSq,
        int boneLodDepth,
        double animLodDistanceSq,
        int animLodTickInterval
    ) {
        this.boneLodDistanceSq = boneLodDistanceSq;
        this.boneLodDepth = boneLodDepth;
        this.animLodDistanceSq = animLodDistanceSq;
        this.animLodTickInterval = animLodTickInterval;
    }

    public double boneLodDistanceSq() {
        return boneLodDistanceSq;
    }

    public int boneLodDepth() {
        return boneLodDepth;
    }

    public double animLodDistanceSq() {
        return animLodDistanceSq;
    }

    public int animLodTickInterval() {
        return animLodTickInterval;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private double boneLodDistance = Double.MAX_VALUE;

        private int boneLodDepth = Integer.MAX_VALUE;

        private double animLodDistance = Double.MAX_VALUE;

        private int animLodTickInterval = 1;

        private Builder() {}

        /**
         * Beyond {@code distance} blocks, hide bones deeper than {@code maxDepth} in the hierarchy.
         * <p>
         * Example: {@code boneLod(24, 2)} — past 24 blocks, only render the top 3 levels of bones (depths 0, 1, 2).
         * Tail chains, finger bones, and other fine-detail subtrees deeper than depth 2 become invisible without any
         * model changes.
         * </p>
         *
         * @param distance Distance in blocks at which bone LOD activates
         * @param maxDepth Max bone hierarchy depth to keep visible (0-indexed; 0 = root only)
         */
        public Builder boneLod(double distance, int maxDepth) {
            this.boneLodDistance = distance;
            this.boneLodDepth = maxDepth;
            return this;
        }

        /**
         * Beyond {@code distance} blocks, reduce animation updates to once every {@code tickInterval} ticks.
         * <p>
         * Example: {@code animLod(32, 3)} — past 32 blocks, animate at ~7 fps (20/3) instead of per-frame. The entity
         * still moves smoothly, but its pose only updates every 3 ticks and holds in between, saving most of the
         * per-bone CPU work. Animation time keeps advancing, so each update lands on the correct point of the
         * animation.
         * </p>
         *
         * @param distance     Distance in blocks at which animation LOD activates
         * @param tickInterval Update every N ticks (minimum 1)
         */
        public Builder animLod(double distance, int tickInterval) {
            this.animLodDistance = distance;
            this.animLodTickInterval = Math.max(1, tickInterval);
            return this;
        }

        public AzLodConfig build() {
            return new AzLodConfig(
                boneLodDistance * boneLodDistance,
                boneLodDepth,
                animLodDistance * animLodDistance,
                animLodTickInterval
            );
        }
    }
}
