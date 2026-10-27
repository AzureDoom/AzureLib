package mod.azure.azurelib.animation.dispatch.command.sequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import mod.azure.azurelib.animation.dispatch.command.stage.AzAnimationStage;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehavior;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import mod.azure.azurelib.animation.property.AzAnimationStageProperties;

/**
 * When each stage of an {@link AzSequence} plays and when each of its events fires, in game ticks from the moment the
 * sequence is started, worked out from an {@link AzSequenceTiming}. This mirrors how the animation controller plays a
 * sequence:
 * <ul>
 * <li>Every stage starts with a transition of {@code transitionLength / speed} ticks (the controller's transition timer
 * runs at playback speed), blending from the current pose to the stage's first pose.</li>
 * <li>The animation then plays from its start tick offset to its end at {@code speed} animation ticks per game tick. A
 * repeating stage plays {@code repeatXTimes} times back to back, without transitions in between.</li>
 * <li>Looping, ping-pong and holding stages never finish; a freezing stage stops at its freeze point. Nothing after
 * them can be reached.</li>
 * </ul>
 * Each stage uses its own speed, transition length, start offset, repeat count, freeze point and direction when it set
 * one, otherwise the timing's (controller's) value. Stages played "as authored" are timed as play-once stages, since
 * the server can't read the animation file to know better; give a stage an explicit behavior if events after it depend
 * on it repeating or freezing.
 * <p>
 * Changes made to the controller while the sequence runs (another command changing the speed, say) aren't visible here,
 * and the client only starts the sequence once the command packet arrives, so expect a tick of slack in either
 * direction.
 */
public final class AzSequenceTimeline {

    private static final Comparator<ScheduledEvent> BY_TICK = Comparator.comparingDouble(ScheduledEvent::tick);

    private final List<Stage> stages;

    private final List<ScheduledEvent> events;

    private final List<SkippedEvent> skipped;

    private final double duration;

    private AzSequenceTimeline(
        List<Stage> stages,
        List<ScheduledEvent> events,
        List<SkippedEvent> skipped,
        double duration
    ) {
        this.stages = Collections.unmodifiableList(new ArrayList<>(stages));
        this.events = Collections.unmodifiableList(new ArrayList<>(events));
        this.skipped = Collections.unmodifiableList(new ArrayList<>(skipped));
        this.duration = duration;
    }

    public static final class Stage {

        private final int index;

        private final String animationName;

        private final AzPlayBehavior playBehavior;

        private final double arrivalTick;

        private final double startTick;

        private final double endTick;

        private final boolean finishes;

        /**
         * When one stage plays. All times are game ticks after the sequence starts, or {@link Double#NaN} where an
         * unknown animation length makes them impossible to work out.
         *
         * @param index         the stage's index in {@link AzSequence#stages()}
         * @param animationName the stage's animation
         * @param playBehavior  the behavior the stage is timed with
         * @param arrivalTick   when the transition into the stage begins
         * @param startTick     when the stage's animation itself starts playing (after the transition)
         * @param endTick       when it finishes (see {@link AzSequenceEvent.Anchor#STAGE_END}); infinite if it never
         *                      gets there
         * @param finishes      whether the sequence moves on to the next stage after {@code endTick}
         */
        public Stage(
            int index,
            String animationName,
            AzPlayBehavior playBehavior,
            double arrivalTick,
            double startTick,
            double endTick,
            boolean finishes
        ) {
            this.index = index;
            this.animationName = animationName;
            this.playBehavior = playBehavior;
            this.arrivalTick = arrivalTick;
            this.startTick = startTick;
            this.endTick = endTick;
            this.finishes = finishes;
        }

        public int index() {
            return index;
        }

        public String animationName() {
            return animationName;
        }

        public AzPlayBehavior playBehavior() {
            return playBehavior;
        }

        public double arrivalTick() {
            return arrivalTick;
        }

        public double startTick() {
            return startTick;
        }

        public double endTick() {
            return endTick;
        }

        public boolean finishes() {
            return finishes;
        }
    }

    /**
     * An event and the game tick, counted from the start of the sequence, it is due on. Fractional ticks fire on the
     * first whole tick at or after them.
     */
    public static final class ScheduledEvent {

        private AzSequenceEvent event;

        private final double tick;

        public ScheduledEvent(AzSequenceEvent event, double tick) {
            this.event = event;
            this.tick = tick;
        }

        public AzSequenceEvent event() {
            return this.event;
        }

        public double tick() {
            return this.tick;
        }
    }

    /**
     * An event that will not fire, and why.
     */
    public static final class SkippedEvent {

        private final AzSequenceEvent event;

        private final String reason;

        public SkippedEvent(
            AzSequenceEvent event,
            String reason
        ) {
            this.event = event;
            this.reason = reason;
        }

        public AzSequenceEvent event() {
            return this.event;
        }

        public String reason() {
            return this.reason;
        }
    }

    /**
     * Works out the timeline of {@code sequence} played with {@code timing}.
     */
    public static AzSequenceTimeline compute(AzSequence sequence, AzSequenceTiming timing) {
        List<AzAnimationStage> sequenceStages = sequence.stages();
        ArrayList<AzSequenceTimeline.Stage> stages = new ArrayList<Stage>(sequenceStages.size());
        AzSequenceTimeline.StageTiming[] stageInfo = new StageTiming[sequenceStages.size()];

        double arrival = 0D;
        boolean reachable = true;

        for (int i = 0; i < sequenceStages.size(); i++) {
            AzSequenceTimeline.StageTiming info = StageTiming.of(sequenceStages.get(i), timing);
            stageInfo[i] = info;

            if (!reachable) {
                info.unreachable = true;
                stages.add(
                    new Stage(
                        i,
                        info.name,
                        info.behavior,
                        Double.POSITIVE_INFINITY,
                        Double.POSITIVE_INFINITY,
                        Double.POSITIVE_INFINITY,
                        false
                    )
                );
                continue;
            }

            info.arrival = arrival;
            info.bodyStart = arrival + info.transitionTicks();
            info.end = info.bodyStart + info.bodyTicks();

            stages.add(new Stage(i, info.name, info.behavior, info.arrival, info.bodyStart, info.end, info.finishes));

            if (!info.finishes || info.end == Double.POSITIVE_INFINITY) {
                reachable = false;
            }

            arrival = info.end;
        }

        ArrayList<AzSequenceTimeline.ScheduledEvent> scheduled = new ArrayList<ScheduledEvent>();
        ArrayList<SkippedEvent> skipped = new ArrayList<SkippedEvent>();

        for (AzSequenceEvent event : sequence.events()) {
            resolve(event, stageInfo, scheduled, skipped);
        }

        // Stable: events due on the same tick keep the sequence's order.
        scheduled.sort(BY_TICK);

        double duration = stages.isEmpty()
            ? 0
            : stages.get(stages.size() - 1).finishes()
                ? stages.get(stages.size() - 1).endTick()
                : Double.POSITIVE_INFINITY;

        return new AzSequenceTimeline(stages, scheduled, skipped, duration);
    }

    private static void resolve(
        AzSequenceEvent event,
        StageTiming[] stageInfo,
        List<ScheduledEvent> scheduled,
        List<SkippedEvent> skipped
    ) {
        if (!event.isAnchored()) {
            scheduled.add(new ScheduledEvent(event, event.tick()));
            return;
        }

        if (event.stage() < 0 || event.stage() >= stageInfo.length) {
            skipped.add(new SkippedEvent(event, "it points at stage " + event.stage() + ", which doesn't exist"));
            return;
        }

        AzSequenceTimeline.StageTiming info = stageInfo[event.stage()];

        if (info.unreachable) {
            skipped.add(
                new SkippedEvent(event, "stage '" + info.name + "' comes after a stage that never finishes")
            );
            return;
        }

        double tick;

        switch (event.anchor()) {
            case STAGE_START:
                tick = info.bodyStart;
                break;
            case STAGE_END:
                tick = info.end;
                break;
            case KEYFRAME:
                tick = info.keyframeTick(event.stageTick());
                break;
            case SEQUENCE:
                throw new IllegalStateException("unreachable");
            default:
                throw new IllegalStateException("Unexpected anchor: " + event.anchor());
        }

        if (Double.isNaN(tick)) {
            skipped.add(new SkippedEvent(event, info.missingLengthReason(event)));
        } else if (tick == Double.POSITIVE_INFINITY) {
            skipped.add(new SkippedEvent(event, "stage '" + info.name + "' never gets there"));
        } else {
            scheduled.add(new ScheduledEvent(event, tick));
        }
    }

    /**
     * @return every stage, in order, with when it plays
     */
    public List<Stage> stages() {
        return stages;
    }

    /**
     * @return every event that will fire, ordered by the tick it is due on
     */
    public List<ScheduledEvent> events() {
        return events;
    }

    /**
     * @return events that can't be placed (an animation length is unknown, or the animation never reaches them)
     */
    public List<SkippedEvent> skipped() {
        return skipped;
    }

    /**
     * @return when the last stage finishes, in game ticks; infinite if the sequence ends on a stage that never
     *         finishes, {@link Double#NaN} if a length is unknown
     */
    public double duration() {
        return duration;
    }

    @Override
    public String toString() {
        return "AzSequenceTimeline[stages=" + stages + ", events=" + events + ", skipped=" + skipped + "]";
    }

    /**
     * One stage's effective settings and resolved times.
     */
    private static final class StageTiming {

        private String name;

        private AzPlayBehavior behavior;

        private double speed;

        private double transitionLength;

        private double startTickOffset;

        private double freezeTick;

        private int plays;

        private boolean reversed;

        private double length;

        private boolean finishes;

        private boolean unreachable;

        private double arrival = Double.NaN;

        private double bodyStart = Double.NaN;

        private double end = Double.NaN;

        static StageTiming of(AzAnimationStage stage, AzSequenceTiming timing) {
            AzAnimationStageProperties properties = stage.properties();
            StageTiming info = new StageTiming();

            info.name = stage.name();
            info.behavior = properties.playBehavior();
            info.speed = properties.hasAnimationSpeed() ? properties.animationSpeed() : timing.animationSpeed();
            info.transitionLength = properties.hasTransitionLength()
                ? properties.transitionLength()
                : timing.transitionLength();
            info.startTickOffset = Math.max(
                0,
                properties.hasStartTickOffset() ? properties.startTickOffset() : timing.startTickOffset()
            );
            info.reversed = properties.hasReversing() ? properties.isReversing() : timing.isReversing();
            info.length = timing.length(stage.name());

            double repeat = properties.hasRepeatXTimes() ? properties.repeatXTimes() : timing.repeatXTimes();
            info.plays = info.behavior == AzPlayBehaviors.REPEAT_X_TIMES && repeat > 1 ? (int) Math.ceil(repeat) : 1;

            double freeze = properties.hasFreezeTickOffset()
                ? properties.freezeTickOffset()
                : timing.freezeTickOffset();
            info.freezeTick = info.behavior == AzPlayBehaviors.FREEZE_ON_FRAME && freeze > 0 ? freeze : 0;

            info.finishes = info.behavior != AzPlayBehaviors.LOOP
                && info.behavior != AzPlayBehaviors.PING_PONG
                && info.behavior != AzPlayBehaviors.HOLD_ON_LAST_FRAME
                && info.behavior != AzPlayBehaviors.FREEZE_ON_FRAME;

            return info;
        }

        double transitionTicks() {
            if (!(transitionLength > 0)) {
                // The transition state still runs for one update, but completes on it.
                return 0;
            }

            return speed > 0 ? transitionLength / speed : Double.POSITIVE_INFINITY;
        }

        /**
         * How far the animation plays before this stage hands over (or stops moving), in game ticks.
         */
        double bodyTicks() {
            double stopAt;

            if (freezeTick > 0) {
                // A freeze point past the end holds the last frame; with the length unknown, trust the freeze point.
                stopAt = Double.isNaN(length) ? freezeTick : Math.min(freezeTick, length);
            } else {
                stopAt = length;
            }

            if (Double.isNaN(stopAt)) {
                return Double.NaN;
            }

            double progress = Math.max(0, stopAt - startTickOffset);

            if (progress == 0) {
                return 0;
            }

            return speed > 0 ? progress / speed * plays : Double.POSITIVE_INFINITY;
        }

        /**
         * When the animation shows {@code sampleTick} (keyframe time) for the first time, in game ticks.
         */
        double keyframeTick(double sampleTick) {
            if (reversed && Double.isNaN(length)) {
                return Double.NaN;
            }

            if (!Double.isNaN(length) && (sampleTick < 0 || sampleTick > length)) {
                return Double.POSITIVE_INFINITY;
            }

            double progress = reversed ? length - sampleTick : sampleTick;

            if (freezeTick > 0 && progress > freezeTick) {
                return Double.POSITIVE_INFINITY;
            }

            double ahead = Math.max(0, progress - startTickOffset);

            if (ahead == 0) {
                // Already at or behind the start offset: shown on the stage's first frame, the way keyframe events
                // behind the start fire immediately.
                return bodyStart;
            }

            return speed > 0 ? bodyStart + ahead / speed : Double.POSITIVE_INFINITY;
        }

        String missingLengthReason(AzSequenceEvent event) {
            return "the length of '" + name + "' or of an earlier stage's animation is unknown; add it to the "
                + "AzSequenceTiming (event " + event + ")";
        }
    }
}
