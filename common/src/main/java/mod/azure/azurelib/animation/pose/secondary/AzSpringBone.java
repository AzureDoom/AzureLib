package mod.azure.azurelib.animation.pose.secondary;

import com.mojang.math.Vector3f;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import mod.azure.azurelib.animation.pose.*;

/**
 * Procedural secondary motion for tails, antennae, ears, hair and cloth-like bone chains. Each joint of the chain is a
 * particle pulled towards where the animation puts it relative to the joint before it, carrying momentum, damped, and
 * held at the chain's segment lengths; the bones are then turned to follow the particles. Because each joint follows
 * its parent, lag builds up towards the tip and the chain bends and whips rather than swinging as one piece.The
 * simulation runs in world axes for entities, so the chain swings and trails when the entity moves or turns, not only
 * when its animation does. For other animatables it runs in model space and reacts to the animation alone.
 *
 * <pre>{@code
 * modifiers.add(
 *     "tail",
 *     AzSpringBone.<MyEntity>chain("tail_1", "tail_2", "tail_3", "tail_4")
 *         .tip(0, 0, 6)
 *         .stiffness(0.15f)
 *         .damping(0.2f)
 *         .gravity(0.004f)
 *         .build()
 * ).priority(200);
 * }</pre>
 * <p>
 * Add {@link AzSpringCollider colliders} (and {@link Builder#ground ground}) to keep the chain from swinging through
 * the body or the floor.
 * </p>
 * <p>
 * The first chain bone is driven by the animation alone and anchors the chain; every following bone swings. Give the
 * last bone a {@link Builder#tip} so it swings too.
 * </p>
 *
 * @param <T> the animatable type
 */
public final class AzSpringBone<T> implements AzPoseModifier<T> {

    private final String[] bones;

    private final boolean hasTip;

    private final float tipX, tipY, tipZ;

    private final float stiffness;

    private final float damping;

    private final float gravity;

    private final float inertia;

    private final AzEntityModelSpace space;

    private final List<AzBoneConstraint> constraints;

    private final AzSpringCollider[] colliders;

    private final float radius;

    private final boolean ground;

    private final Vector3f scratch = new Vector3f(), scratch2 = new Vector3f();

    private AzSpringBone(Builder<T> builder) {
        this.bones = builder.bones;
        this.hasTip = builder.hasTip;
        this.tipX = builder.tipX;
        this.tipY = builder.tipY;
        this.tipZ = builder.tipZ;
        this.stiffness = builder.stiffness;
        this.damping = builder.damping;
        this.gravity = builder.gravity;
        this.inertia = builder.inertia;
        this.space = builder.space;
        this.constraints = List.copyOf(builder.constraints);
        this.colliders = builder.colliders.toArray(new AzSpringCollider[0]);
        this.radius = builder.radius;
        this.ground = builder.ground;
    }

    /** A spring chain over the given bones, root-most first; each must be a descendant of the one before it. */
    public static <T> Builder<T> chain(String... bones) {
        return new Builder<>(bones);
    }

    @Override
    public void apply(AzPoseContext<T> pose) {
        AzSpringBone.State state = pose.state(State::new);

        if (state.chain == null) {
            if (state.failed) {
                return;
            }

            state.chain = AzBoneChain.resolve(pose, bones);

            if (state.chain == null) {
                state.failed = true;
                return;
            }

            state.allocate(pointCount(state.chain));
            state.colliderChains = new AzBoneChain[colliders.length];
            state.colliders = new float[colliders.length * 7];

            for (int k = 0; k < colliders.length; k++) {
                // A collider on a bone this model doesn't have is skipped.
                state.colliderChains[k] = AzBoneChain.resolve(pose, colliders[k].bone());
            }
        }

        AzBoneChain chain = state.chain;
        chain.reset();

        int points = pointCount(chain);
        Entity entity = pose.animatable() instanceof Entity e ? e : null;
        float partialTick = pose.partialTick();

        // Where the animation puts each joint, in simulation space.
        for (int j = 0; j < points; j++) {
            chainPoint(chain, j, scratch);

            if (entity != null) {
                space.modelToEntity(entity, partialTick, scratch);
            }

            state.target[j * 3] = scratch.x;
            state.target[j * 3 + 1] = scratch.y;
            state.target[j * 3 + 2] = scratch.z;
        }

        double ex = 0, ey = 0, ez = 0;

        if (entity != null) {
            Vec3 position = entity.getPosition(partialTick);
            ex = position.x;
            ey = position.y;
            ez = position.z;
        }

        updateColliders(state, entity, partialTick);

        if (pose.isDiscontinuous() || !state.initialized) {
            System.arraycopy(state.target, 0, state.position, 0, points * 3);
            Arrays.fill(state.velocity, 0);
            collide(state, points);
            state.initialized = true;
        } else {
            // Particles keep their place in the world while the simulation frame follows the entity.
            float sx = (float) (ex - state.lastX) * inertia;
            float sy = (float) (ey - state.lastY) * inertia;
            float sz = (float) (ez - state.lastZ) * inertia;

            for (int j = 1; j < points; j++) {
                state.position[j * 3] -= sx;
                state.position[j * 3 + 1] -= sy;
                state.position[j * 3 + 2] -= sz;
            }

            simulate(state, points, (float) pose.deltaTicks());
        }

        state.lastX = ex;
        state.lastY = ey;
        state.lastZ = ez;

        // Turn each bone so the next joint lands on its particle.
        for (int i = 0; i < points - 1; i++) {
            chainPoint(chain, i + 1, scratch);
            scratch2.set(state.position[(i + 1) * 3], state.position[(i + 1) * 3 + 1], state.position[(i + 1) * 3 + 2]);

            if (entity != null) {
                space.entityToModel(entity, partialTick, scratch2);
            }

            chain.aim(i, scratch, scratch2, 1);

            for (int c = 0, n = constraints.size(); c < n; c++) {
                chain.constrain(i, constraints.get(c));
            }
        }

        chain.apply();
    }

    private int pointCount(AzBoneChain chain) {
        return chain.size() + (hasTip ? 1 : 0);
    }

    private void chainPoint(AzBoneChain chain, int j, Vector3f dest) {
        if (j < chain.size()) {
            chain.joint(j, dest);
        } else {
            chain.pointOnBone(chain.size() - 1, tipX, tipY, tipZ, dest);
        }
    }

    private void simulate(State state, int points, float deltaTicks) {
        float[] p = state.position;
        float[] v = state.velocity;
        float[] t = state.target;

        // The root follows the animation exactly.
        p[0] = t[0];
        p[1] = t[1];
        p[2] = t[2];

        float remaining = deltaTicks;

        while (remaining > 1e-4f) {
            float dt = Math.min(remaining, 1f);
            float keep = Math.max(0, 1 - damping * dt);

            for (int j = 1; j < points; j++) {
                int o = j * 3;
                int q = o - 3;
                state.previous[o] = p[o];
                state.previous[o + 1] = p[o + 1];
                state.previous[o + 2] = p[o + 2];
                float restX = t[o] - t[q] + p[q];
                float restY = t[o + 1] - t[q + 1] + p[q + 1];
                float restZ = t[o + 2] - t[q + 2] + p[q + 2];
                v[o] = (v[o] + (restX - p[o]) * stiffness * dt) * keep;
                v[o + 1] = (v[o + 1] + (restY - p[o + 1]) * stiffness * dt - gravity * dt) * keep;
                v[o + 2] = (v[o + 2] + (restZ - p[o + 2]) * stiffness * dt) * keep;
                p[o] += v[o] * dt;
                p[o + 1] += v[o + 1] * dt;
                p[o + 2] += v[o + 2] * dt;
            }

            // Hold every segment at its animated length and push joints out of colliders. The two passes are
            // alternated so the chain ends up both the right length and outside the colliders.
            for (int pass = 0; pass < (colliders.length > 0 || ground ? 3 : 1); pass++) {
                for (int j = 1; j < points; j++) {
                    int o = j * 3;
                    int q = o - 3;
                    float rx = t[o] - t[q], ry = t[o + 1] - t[q + 1], rz = t[o + 2] - t[q + 2];
                    float rest = (float) Math.sqrt(rx * rx + ry * ry + rz * rz);
                    float dx = p[o] - p[q], dy = p[o + 1] - p[q + 1], dz = p[o + 2] - p[q + 2];
                    float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

                    if (length > 1e-6f) {
                        float k = rest / length;
                        p[o] = p[q] + dx * k;
                        p[o + 1] = p[q + 1] + dy * k;
                        p[o + 2] = p[q + 2] + dz * k;
                    }
                }

                collide(state, points);
            }

            // Derive velocity from the corrected motion, so contact slides and settles instead of bouncing.
            for (int j = 1; j < points; j++) {
                int o = j * 3;
                v[o] = (p[o] - state.previous[o]) / dt;
                v[o + 1] = (p[o + 1] - state.previous[o + 1]) / dt;
                v[o + 2] = (p[o + 2] - state.previous[o + 2]) / dt;
            }

            remaining -= dt;
        }
    }

    /** Puts each collider's ends and radius into simulation space for this frame. */
    private void updateColliders(State state, Entity entity, float partialTick) {
        // Simulation-space length of one Blockbench pixel, including the model's scale for entities.
        scratch.set(1 / 16f, 0, 0);

        if (entity != null) {
            space.modelToEntity(entity, partialTick, scratch);
        }

        float pixel = AzVectors.length(scratch);
        state.particleRadius = radius * pixel;
        float[] data = state.colliders;

        for (int k = 0; k < colliders.length; k++) {
            AzSpringCollider collider = colliders[k];
            AzBoneChain chain = state.colliderChains[k];
            int o = k * 7;

            if (chain == null) {
                data[o + 6] = -1;
                continue;
            }

            chain.reset();
            chain.pointOnBone(0, collider.startX(), collider.startY(), collider.startZ(), scratch);
            chain.pointOnBone(0, collider.endX(), collider.endY(), collider.endZ(), scratch2);

            if (entity != null) {
                space.modelToEntity(entity, partialTick, scratch);
                space.modelToEntity(entity, partialTick, scratch2);
            }

            data[o] = scratch.x;
            data[o + 1] = scratch.y;
            data[o + 2] = scratch.z;
            data[o + 3] = scratch2.x;
            data[o + 4] = scratch2.y;
            data[o + 5] = scratch2.z;
            data[o + 6] = collider.radius() * pixel;
        }
    }

    /**
     * Pushes the chain out of the colliders and above the ground. Each segment is tested against each collider's core,
     * so thin colliders can't slip between two joints; the segment's outer joint is moved so the segment clears it.
     */
    private void collide(State state, int points) {
        float[] p = state.position;
        float[] data = state.colliders;
        float particleRadius = state.particleRadius;

        for (int j = 1; j < points; j++) {
            int o = j * 3;
            int q = o - 3;

            for (int k = 0; k < colliders.length; k++) {
                int c = k * 7;
                float colliderRadius = data[c + 6];

                if (colliderRadius < 0) {
                    continue;
                }

                boolean capsule = colliders[k].shape() == AzSpringCollider.Shape.CAPSULE;
                float ax = data[c], ay = data[c + 1], az = data[c + 2];
                float bx = capsule ? data[c + 3] : ax, by = capsule ? data[c + 4] : ay, bz = capsule ? data[c + 5] : az;

                // Closest points between the segment (q -> o) and the collider's core (a -> b).
                closestSegmentParams(p[q], p[q + 1], p[q + 2], p[o], p[o + 1], p[o + 2], ax, ay, az, bx, by, bz);
                float along = segmentS;
                float core = segmentT;
                float chainX = p[q] + (p[o] - p[q]) * along;
                float chainY = p[q + 1] + (p[o + 1] - p[q + 1]) * along;
                float chainZ = p[q + 2] + (p[o + 2] - p[q + 2]) * along;
                float coreX = ax + (bx - ax) * core;
                float coreY = ay + (by - ay) * core;
                float coreZ = az + (bz - az) * core;

                float reach = colliderRadius + particleRadius;
                float dx = chainX - coreX, dy = chainY - coreY, dz = chainZ - coreZ;
                float distSq = dx * dx + dy * dy + dz * dz;

                if (distSq >= reach * reach) {
                    continue;
                }

                float dist = (float) Math.sqrt(distSq);

                if (dist < 1e-6f) {
                    dx = 0;
                    dy = 1;
                    dz = 0;
                } else {
                    dx /= dist;
                    dy /= dist;
                    dz /= dist;
                }

                // Moving the outer joint by d moves the contact point by along * d; the root end of the segment is
                // handled by the segment before it.
                float lift = (reach - dist) / Math.max(along, 0.25f);
                p[o] += dx * lift;
                p[o + 1] += dy * lift;
                p[o + 2] += dz * lift;
            }

            // The ground is the entity's feet (or the model's origin for other animatables).
            if (ground && p[o + 1] < particleRadius) {
                p[o + 1] = particleRadius;
            }
        }
    }

    // Results of closestSegmentParams, to avoid allocating.
    private float segmentS, segmentT;

    /** Closest points between segments p0-p1 and a-b; sets segmentS and segmentT, the parameters along each. */
    private void closestSegmentParams(
        float p0x,
        float p0y,
        float p0z,
        float p1x,
        float p1y,
        float p1z,
        float ax,
        float ay,
        float az,
        float bx,
        float by,
        float bz
    ) {
        float d1x = p1x - p0x, d1y = p1y - p0y, d1z = p1z - p0z;
        float d2x = bx - ax, d2y = by - ay, d2z = bz - az;
        float rx = p0x - ax, ry = p0y - ay, rz = p0z - az;
        float aa = d1x * d1x + d1y * d1y + d1z * d1z;
        float ee = d2x * d2x + d2y * d2y + d2z * d2z;
        float ff = d2x * rx + d2y * ry + d2z * rz;
        float s, t;

        if (aa <= 1e-12f && ee <= 1e-12f) {
            s = 0;
            t = 0;
        } else if (aa <= 1e-12f) {
            s = 0;
            t = clamp01(ff / ee);
        } else {
            float c = d1x * rx + d1y * ry + d1z * rz;

            if (ee <= 1e-12f) {
                s = clamp01(-c / aa);
                t = 0;
            } else {
                float bb = d1x * d2x + d1y * d2y + d1z * d2z;
                float denom = aa * ee - bb * bb;
                s = denom > 1e-12f ? clamp01((bb * ff - c * ee) / denom) : 0;
                t = (bb * s + ff) / ee;

                if (t < 0) {
                    t = 0;
                    s = clamp01(-c / aa);
                } else if (t > 1) {
                    t = 1;
                    s = clamp01((bb - c) / aa);
                }
            }
        }

        segmentS = s;
        segmentT = t;
    }

    private static float clamp01(float value) {
        return Mth.clamp(value, 0, 1);
    }

    private static final class State {

        private AzBoneChain chain;

        private boolean failed;

        private boolean initialized;

        private float[] position, previous, velocity, target;

        private double lastX, lastY, lastZ;

        private AzBoneChain[] colliderChains;

        private float[] colliders;

        private float particleRadius;

        void allocate(int points) {
            position = new float[points * 3];
            previous = new float[points * 3];
            velocity = new float[points * 3];
            target = new float[points * 3];
        }
    }

    public static final class Builder<T> {

        private final String[] bones;

        private boolean hasTip;

        private float tipX, tipY, tipZ;

        private float stiffness = 0.2f;

        private float damping = 0.25f;

        private float gravity;

        private float inertia = 1;

        private AzEntityModelSpace space = AzEntityModelSpace.DEFAULT;

        private final List<AzBoneConstraint> constraints = new ArrayList<>();

        private final List<AzSpringCollider> colliders = new ArrayList<>();

        private float radius;

        private boolean ground;

        private Builder(String[] bones) {
            if (bones.length == 0) {
                throw new IllegalArgumentException("A spring chain needs at least one bone");
            }

            this.bones = bones.clone();
        }

        /**
         * The end of the last bone, in Blockbench pixels from its pivot, so the last bone swings as well. Without a tip
         * the last bone keeps its animated rotation relative to its parent.
         */
        public Builder<T> tip(float x, float y, float z) {
            this.hasTip = true;
            this.tipX = x;
            this.tipY = y;
            this.tipZ = z;
            return this;
        }

        public Builder<T> stiffness(float stiffness) {
            this.stiffness = clamp01(stiffness);
            return this;
        }

        public Builder<T> damping(float damping) {
            this.damping = clamp01(damping);
            return this;
        }

        public Builder<T> gravity(float gravity) {
            this.gravity = gravity;
            return this;
        }

        /**
         * How much the entity moving through the world swings the chain, from 0 (only animation and turning) to 1
         * (fully). 1 by default.
         */
        public Builder<T> inertia(float inertia) {
            this.inertia = clamp01(inertia);
            return this;
        }

        /** For renderers that scale the model; see {@link AzEntityModelSpace#scaled}. */
        public Builder<T> space(AzEntityModelSpace space) {
            this.space = Objects.requireNonNull(space, "space");
            return this;
        }

        /** Limits on how far each swinging bone may turn, e.g. to keep a tail from folding into the body. */
        public Builder<T> constraint(AzBoneConstraint... constraints) {
            this.constraints.addAll(List.of(constraints));
            return this;
        }

        /**
         * Shapes the chain can't pass through, such as capsules along the legs. Colliders follow their bones, and one
         * collider can be shared by several chains.
         */
        public Builder<T> collider(AzSpringCollider... colliders) {
            this.colliders.addAll(List.of(colliders));
            return this;
        }

        /** How thick the chain is for collisions, in Blockbench pixels. 0 by default. */
        public Builder<T> radius(float pixels) {
            this.radius = Math.max(0, pixels);
            return this;
        }

        /**
         * Keeps the chain above the ground: the entity's feet, or the model's origin for items and block entities. Off
         * by default.
         */
        public Builder<T> ground(boolean ground) {
            this.ground = ground;
            return this;
        }

        public AzSpringBone<T> build() {
            return new AzSpringBone<>(this);
        }

        private static float clamp01(float value) {
            return Mth.clamp(value, 0, 1);
        }
    }
}
