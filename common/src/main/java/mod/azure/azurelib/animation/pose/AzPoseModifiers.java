package mod.azure.azurelib.animation.pose;

import com.mojang.math.Vector3f;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Objects;

/**
 * Ready-made {@link AzPoseModifier}s for common procedural motion. Each one is a plain modifier, so it can be weighted,
 * masked, prioritized and faded like a handwritten one.
 */
public final class AzPoseModifiers {

    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

    private static final double TWO_PI = Math.PI * 2.0;

    private AzPoseModifiers() {}

    /**
     * Turns the given bones toward where a living entity is looking, on top of whatever the animation is doing. The
     * look angle is split evenly across the bones, so {@code headLook("neck", "head")} turns each by half.
     *
     * @param boneNames one or more bones, usually root-to-tip (e.g. neck then head)
     */
    public static <T extends LivingEntity> HeadLook<T> headLook(String... boneNames) {
        return new HeadLook<>(List.of(boneNames));
    }

    /**
     * Adds a sine-wave rotation, e.g. an idle sway for antennae, tails or hanging parts.
     *
     * @param boneName    the bone to sway
     * @param xRadians    peak rotation around X
     * @param yRadians    peak rotation around Y
     * @param zRadians    peak rotation around Z
     * @param periodTicks length of one full swing, in animation ticks
     * @param phase       offset into the cycle, in cycles (0.25 = a quarter period ahead)
     */
    public static <T> AzPoseModifier<T> sway(
        String boneName,
        float xRadians,
        float yRadians,
        float zRadians,
        double periodTicks,
        double phase
    ) {
        Objects.requireNonNull(boneName, "boneName");

        if (periodTicks <= 0) {
            throw new IllegalArgumentException("periodTicks must be positive");
        }

        return pose -> {
            float s = (float) Math.sin(TWO_PI * (pose.animTime() / periodTicks + phase));
            pose.bone(boneName).rotate(xRadians * s, yRadians * s, zRadians * s);
        };
    }

    /**
     * Makes {@code target} follow {@code source}'s rotation away from its bind pose, scaled by {@code factor}. Reads
     * the source's current pose, so it includes earlier modifiers (put it after a head-look to have eyes or a jaw
     * follow the head).
     */
    public static <T> AzPoseModifier<T> followRotation(String source, String target, float factor) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");

        return pose -> {
            AzPoseBone src = pose.bone(source);

            if (!src.exists()) {
                return;
            }

            Vector3f rot = src.currentRotation();
            Vector3f bind = src.bindRotation();
            pose.bone(target)
                .rotate((rot.x() - bind.x()) * factor, (rot.y() - bind.y()) * factor, (rot.z() - bind.z()) * factor);
        };
    }

    /**
     * Secondary motion: the bone's rotation chases its own current pose (animation plus earlier modifiers) through a
     * damped spring, so it lags, overshoots and settles instead of snapping when that pose changes. Good for tails,
     * ears, antennae and dangling parts. It reacts to how the bone itself is animated, not to the parent bone or the
     * entity moving through the world; to feed it extra motion, run a modifier such as {@link #sway} or
     * {@link #followRotation} on the same bone at a lower priority. Uses stiffness 0.35 and damping 0.45.
     */
    public static <T> AzPoseModifier<T> spring(String boneName) {
        return spring(boneName, 0.35f, 0.45f);
    }

    /**
     * Secondary motion with explicit spring constants, both per animation tick and clamped to [0, 1].
     *
     * @param stiffness how hard the bone is pulled toward its pose (higher: tighter, faster)
     * @param damping   how much velocity is lost (higher: less overshoot)
     */
    public static <T> AzPoseModifier<T> spring(String boneName, float stiffness, float damping) {
        Objects.requireNonNull(boneName, "boneName");
        float k = Mth.clamp(stiffness, 0, 1);
        float c = Mth.clamp(damping, 0, 1);

        return pose -> {
            AzPoseBone bone = pose.bone(boneName);

            if (!bone.isWritable()) {
                return;
            }

            Vector3f target = bone.currentRotation();
            SpringState state = pose.state(SpringState::new);

            if (pose.isDiscontinuous()) {
                state.snap(target.x(), target.y(), target.z());
            } else {
                state.step(target.x(), target.y(), target.z(), k, c, pose.deltaTicks());
            }

            bone.setRotation(state.x, state.y, state.z);
        };
    }

    public static final class HeadLook<T extends LivingEntity> implements AzPoseModifier<T> {

        private final String[] boneNames;

        private float maxYaw = 85;

        private float maxPitch = 90;

        private float yawSign = 1;

        private float pitchSign = 1;

        private HeadLook(List<String> boneNames) {
            if (boneNames.isEmpty()) {
                throw new IllegalArgumentException("headLook needs at least one bone");
            }

            this.boneNames = boneNames.toArray(new String[0]);
        }

        public HeadLook<T> limits(float maxYawDegrees, float maxPitchDegrees) {
            this.maxYaw = Math.abs(maxYawDegrees);
            this.maxPitch = Math.abs(maxPitchDegrees);
            return this;
        }

        public HeadLook<T> invertYaw() {
            this.yawSign = -yawSign;
            return this;
        }

        public HeadLook<T> invertPitch() {
            this.pitchSign = -pitchSign;
            return this;
        }

        @Override
        public void apply(AzPoseContext<T> pose) {
            T entity = pose.animatable();
            float pt = pose.partialTick();
            float bodyYaw = Mth.rotLerp(pt, entity.yBodyRotO, entity.yBodyRot);
            float headYaw = Mth.rotLerp(pt, entity.yHeadRotO, entity.yHeadRot);
            float yaw = Mth.clamp(Mth.wrapDegrees(headYaw - bodyYaw), -maxYaw, maxYaw);
            float pitch = Mth.clamp(Mth.lerp(pt, entity.xRotO, entity.getXRot()), -maxPitch, maxPitch);

            float share = 1f / boneNames.length;
            float x = pitch * pitchSign * share * DEG_TO_RAD;
            float y = yaw * yawSign * share * DEG_TO_RAD;

            for (String name : boneNames) {
                pose.bone(name).rotate(x, y, 0);
            }
        }
    }

    private static final class SpringState {

        private float x, y, z, vx, vy, vz;

        void snap(float tx, float ty, float tz) {
            x = tx;
            y = ty;
            z = tz;
            vx = vy = vz = 0;
        }

        void step(float tx, float ty, float tz, float k, float c, double deltaTicks) {
            // Fixed sub-steps of at most one tick keep the spring stable regardless of frame rate.
            float remaining = (float) deltaTicks;

            while (remaining > 1e-4f) {
                float dt = Math.min(remaining, 1f);
                vx = (vx + (tx - x) * k * dt) * (1 - c * dt);
                vy = (vy + (ty - y) * k * dt) * (1 - c * dt);
                vz = (vz + (tz - z) * k * dt) * (1 - c * dt);
                x += vx * dt;
                y += vy * dt;
                z += vz * dt;
                remaining -= dt;
            }
        }
    }
}
