package mod.azure.azurelib.animation.play_behavior;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import mod.azure.azurelib.animation.controller.state.machine.AzAnimationControllerStateMachine;
import mod.azure.azurelib.animation.dispatch.command.sequence.AzAnimationSequence;
import mod.azure.azurelib.animation.dispatch.command.sequence.AzSequence;

/**
 * A play behavior that, whenever its animation finishes, picks the next animation from a weighted pool and plays it
 * with this same behavior, so the chain continues indefinitely.
 * <p>
 * The decision is made on the client at the moment the animation actually ends, using the length of the baked
 * animation. Nothing about animation length is hardcoded, so resource packs can freely retime or replace animations.
 * <p>
 * Only the pool's <em>name</em> travels over the network (via {@link AzPlayBehaviorRegistry}); the entries live in this
 * instance. The pool must therefore be built during common init so it is registered on both sides.
 *
 * <pre>{@code
 * public static final AzWeightedPoolBehavior XENO_IDLE = AzWeightedPoolBehavior.builder("mymod:xeno_idle")
 *     .add("idle", 6)
 *     .add("idle2", 3)
 *     .addNoRepeat("idle3", 1) // never plays twice in a row
 *     .build();
 *
 * // Server side, whenever the entity should be idling:
 * XENO_IDLE.sequence().toCommand("base").sendForEntity(this);
 * }</pre>
 *
 * Semantics:
 * <ul>
 * <li>The pool is meant to be the last stage of a sequence. If other stages are queued after it, it yields to them
 * after its first animation (behaves like {@code play_once}).</li>
 * <li>If a picked animation can't be found (e.g. a resource pack removed it), the next candidate is tried. If none
 * resolve, the current animation simply loops.</li>
 * <li>Picks are client-local, so two players watching the same entity may see different idles. That's usually what you
 * want for ambient variety; drive the choice from the server if it has to be in sync.</li>
 * </ul>
 */
public final class AzWeightedPoolBehavior extends AzPlayBehavior {

    /**
     * @param allowRepeat whether this entry may be picked right after itself; the pool-wide
     *                    {@link Builder#avoidImmediateRepeat()} overrides this to {@code false} for every entry
     */
    public record Entry(
        String animationName,
        double weight,
        boolean allowRepeat,
        AzAnimationSequence sequence
    ) {}

    private final List<Entry> entries;

    private final AzSequence startSequence;

    /** One single-stage start sequence per entry, parallel to {@link #entries}, cached so equality is stable. */
    private final List<AzSequence> entryStartSequences;

    private AzWeightedPoolBehavior(String name, List<Builder.PendingEntry> pending, String start, boolean avoidRepeat) {
        super(name);

        var built = new ArrayList<Entry>(pending.size());

        for (var p : pending) {
            var sequence = AzSequence.create().then(p.name(), this).toAnimationSequence();
            built.add(new Entry(p.name(), p.weight(), p.allowRepeat() && !avoidRepeat, sequence));
        }

        this.entries = List.copyOf(built);
        this.entryStartSequences = built.stream().map(e -> AzSequence.create().then(e.animationName(), this)).toList();
        this.startSequence = AzSequence.create().then(start, this);
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    /**
     * @return a sequence that starts the pool with its start animation; dispatch it like any other sequence
     */
    public AzSequence sequence() {
        return startSequence;
    }

    /**
     * @return a sequence that starts the pool on a weighted-random entry. Each call may return a different sequence, so
     *         pick once when entering a state and keep re-sending that same instance; calling this every tick would
     *         restart the chain whenever the pick changes.
     */
    public AzSequence randomSequence() {
        var entry = pickWeighted(entries);
        return entryStartSequences.get(entries.indexOf(entry));
    }

    public List<Entry> entries() {
        return entries;
    }

    @Override
    public void onFinish(AzAnimationControllerStateMachine.Context<?> context) {
        pickNext(context);
    }

    private <T> void pickNext(AzAnimationControllerStateMachine.Context<T> context) {
        var controller = context.animationController();

        if (!controller.animationQueue().isEmpty()) {
            AzPlayBehaviors.advanceOrStop(context);
            return;
        }

        var animatable = context.animationContext().animatable();
        var candidates = new ArrayList<>(entries);

        var current = controller.currentAnimation();

        if (current != null && candidates.size() > 1) {
            var lastName = current.animation().name();
            candidates.removeIf(entry -> !entry.allowRepeat() && entry.animationName().equals(lastName));
        }

        while (!candidates.isEmpty()) {
            var entry = pickWeighted(candidates);
            var queued = controller.tryCreateAnimationQueue(animatable, entry.sequence());

            if (!queued.isEmpty()) {
                controller.animationQueue().addAll(queued);
                context.stateMachine().transition();
                return;
            }

            candidates.remove(entry);
        }

        AzPlayBehaviors.LOOP.onFinish(context);
    }

    private static Entry pickWeighted(List<Entry> candidates) {
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("Cannot pick from an empty candidate list");
        }

        var total = 0D;

        for (var entry : candidates) {
            total += entry.weight();
        }

        var roll = ThreadLocalRandom.current().nextDouble(total);

        for (var entry : candidates) {
            roll -= entry.weight();

            if (roll < 0) {
                return entry;
            }
        }

        return candidates.getLast();
    }

    public static final class Builder {

        private record PendingEntry(
            String name,
            double weight,
            boolean allowRepeat
        ) {}

        private final String name;

        private final List<PendingEntry> entries = new ArrayList<>();

        private String start;

        private boolean avoidRepeat;

        private Builder(String name) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Pool name must not be null or blank");
            }

            this.name = name;
        }

        /**
         * Adds an animation to the pool. Weights are relative: 6/3/1 means 60%/30%/10%.
         */
        public Builder add(String animationName, double weight) {
            return add(animationName, weight, true);
        }

        /**
         * Adds an animation that is never picked twice in a row. Use for detail animations (a sniff, a yawn) that read
         * as obviously repeated when looped, while seamless base loops stay on {@link #add(String, double)}.
         */
        public Builder addNoRepeat(String animationName, double weight) {
            return add(animationName, weight, false);
        }

        /**
         * Adds an animation to the pool.
         *
         * @param allowRepeat {@code false} to prevent this entry from being picked right after itself
         */
        public Builder add(String animationName, double weight, boolean allowRepeat) {
            Objects.requireNonNull(animationName, "animationName");

            if (!(weight > 0) || Double.isInfinite(weight)) {
                throw new IllegalArgumentException("Weight for '" + animationName + "' must be > 0, was " + weight);
            }

            entries.add(new PendingEntry(animationName, weight, allowRepeat));
            return this;
        }

        /**
         * The animation the pool starts with. Defaults to the first entry added.
         */
        public Builder startWith(String animationName) {
            this.start = Objects.requireNonNull(animationName, "animationName");
            return this;
        }

        /**
         * Marks every entry as no-repeat. Shorthand for using {@link #addNoRepeat} on all of them.
         */
        public Builder avoidImmediateRepeat() {
            this.avoidRepeat = true;
            return this;
        }

        /**
         * Builds and registers the pool. Call from common init so the client can resolve it by name.
         */
        public AzWeightedPoolBehavior build() {
            if (entries.isEmpty()) {
                throw new IllegalStateException("Pool '" + name + "' has no entries");
            }

            if (AzPlayBehaviorRegistry.getOrNull(name) != null) {
                throw new IllegalStateException("A play behavior named '" + name + "' is already registered");
            }

            var startName = start != null ? start : entries.getFirst().name();
            var pool = new AzWeightedPoolBehavior(name, entries, startName, avoidRepeat);
            AzPlayBehaviorRegistry.register(pool);
            return pool;
        }
    }
}
