package mod.azure.azurelib.animation.pose.ik;

import com.mojang.math.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import mod.azure.azurelib.animation.pose.AzBoneChain;
import mod.azure.azurelib.animation.pose.AzBoneConstraint;
import mod.azure.azurelib.animation.pose.AzPoseContext;
import mod.azure.azurelib.animation.pose.AzPoseModifier;
import mod.azure.azurelib.animation.pose.AzVectors;

/**
 * A pose modifier that turns a chain of bones (e.g. spine, neck, head) so the last one faces a target, spreading the
 * turn evenly along the chain. Unlike {@code AzPoseModifiers.headLook}, which follows the entity's own head rotation,
 * this aims at an arbitrary point such as another entity.
 *
 * <pre>{@code
 * modifiers.add(
 *     "stare",
 *     AzLookAt.<MyEntity>builder("neck", "head")
 *         .target(AzIkTargets.entityEyes(MyEntity::getTarget))
 *         .constraint(AzBoneConstraints.limit("head", -40, 40, -70, 70, 0, 0))
 *         .build()
 * );
 * }</pre>
 *
 * @param <T> the animatable type
 */
public final class AzLookAt<T> implements AzPoseModifier<T> {

    private final String[] bones;

    private final float forwardX, forwardY, forwardZ;

    private final AzIkTarget<T> target;

    private final List<AzBoneConstraint> constraints;

    private final Vector3f head = new Vector3f(), forward = new Vector3f(), aimPoint = new Vector3f(),
        targetPos = new Vector3f();

    private AzLookAt(Builder<T> builder) {
        this.bones = builder.bones;
        this.forwardX = builder.forwardX;
        this.forwardY = builder.forwardY;
        this.forwardZ = builder.forwardZ;
        this.target = Objects.requireNonNull(builder.target, "target");
        this.constraints = List.copyOf(builder.constraints);
    }

    public static <T> Builder<T> builder(String... bones) {
        return new Builder<>(bones);
    }

    @Override
    public void apply(AzPoseContext<T> pose) {
        AzBoneChain chain = AzIkChain.ChainState.of(pose, bones);

        if (chain == null) {
            return;
        }

        chain.reset();
        int last = chain.size() - 1;
        chain.joint(last, head);

        if (!target.resolve(pose, head, targetPos)) {
            return;
        }

        for (int i = 0; i <= last; i++) {
            chain.joint(last, head);
            chain.directionOnBone(last, forwardX, forwardY, forwardZ, forward);
            float distance = AzVectors.distance(head, targetPos);

            if (distance < 1e-4f) {
                return;
            }

            aimPoint.load(forward);
            aimPoint.mul(distance);
            aimPoint.add(head);
            chain.aim(i, aimPoint, targetPos, 1f / (last - i + 1));
            AzIkSolvers.applyConstraints(chain, i, constraints);
        }

        chain.apply();
    }

    public static final class Builder<T> {

        private final String[] bones;

        private float forwardX = 0, forwardY = 0, forwardZ = -1;

        private AzIkTarget<T> target;

        private final List<AzBoneConstraint> constraints = new ArrayList<>();

        private Builder(String[] bones) {
            if (bones.length == 0) {
                throw new IllegalArgumentException("A look-at needs at least one bone");
            }

            this.bones = bones.clone();
        }

        /**
         * The direction the last bone faces, in its own Blockbench axes. Defaults to {@code (0, 0, -1)}, the front of a
         * model facing north in Blockbench.
         */
        public Builder<T> forward(float x, float y, float z) {
            this.forwardX = x;
            this.forwardY = y;
            this.forwardZ = z;
            return this;
        }

        /** What to look at. Required. */
        public Builder<T> target(AzIkTarget<T> target) {
            this.target = Objects.requireNonNull(target, "target");
            return this;
        }

        /** Limits on how far each bone may turn. */
        public Builder<T> constraint(AzBoneConstraint... constraints) {
            this.constraints.addAll(List.of(constraints));
            return this;
        }

        public AzLookAt<T> build() {
            return new AzLookAt<>(this);
        }
    }
}
