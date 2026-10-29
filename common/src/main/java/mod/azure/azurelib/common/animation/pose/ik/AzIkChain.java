package mod.azure.azurelib.common.animation.pose.ik;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import mod.azure.azurelib.common.animation.pose.AzBoneChain;
import mod.azure.azurelib.common.animation.pose.AzBoneConstraint;
import mod.azure.azurelib.common.animation.pose.AzPoseContext;
import mod.azure.azurelib.common.animation.pose.AzPoseModifier;

/**
 * A pose modifier that solves an IK chain towards a target every frame, after the authored animation. The solved
 * rotations are blended in by the modifier's weight, so IK can be faded in and out per animatable.
 *
 * <pre>{@code
 * modifiers.add(
 *     "left_leg_ik",
 *     AzIkChain.<MyEntity>builder("left_thigh", "left_shin", "left_foot")
 *         .solver(AzIkSolvers.twoBone())
 *         .target(AzIkTargets.ground(0.5f, 0.75f))
 *         .constraint(AzBoneConstraints.hingeX("left_shin", -150, 0))
 *         .build()
 * ).priority(50);
 * }</pre>
 *
 * @param <T> the animatable type
 */
public final class AzIkChain<T> implements AzPoseModifier<T> {

    private final String[] bones;

    private final AzIkEffector effector;

    private final AzIkSolver solver;

    private final AzIkTarget<T> target;

    private final List<AzBoneConstraint> constraints;

    private final Vector3f effectorPos = new Vector3f();

    private final Vector3f targetPos = new Vector3f();

    private AzIkChain(Builder<T> builder) {
        this.bones = builder.bones;
        this.effector = builder.effector;
        this.solver = builder.solver;
        this.target = Objects.requireNonNull(builder.target, "target");
        this.constraints = List.copyOf(builder.constraints);
    }

    public static <T> Builder<T> builder(String... bones) {
        return new Builder<>(bones);
    }

    @Override
    public void apply(AzPoseContext<T> pose) {
        AzBoneChain chain = ChainState.of(pose, bones);

        if (chain == null) {
            return;
        }

        chain.reset();
        effector.position(chain, effectorPos);

        if (!target.resolve(pose, effectorPos, targetPos)) {
            return;
        }

        solver.solve(chain, effector, targetPos, constraints);
        chain.apply();
    }

    public static final class Builder<T> {

        private final String[] bones;

        private AzIkEffector effector = AzIkEffector.PIVOT;

        private AzIkSolver solver;

        private AzIkTarget<T> target;

        private final List<AzBoneConstraint> constraints = new ArrayList<>();

        private Builder(String[] bones) {
            if (bones.length == 0) {
                throw new IllegalArgumentException("An IK chain needs at least one bone");
            }

            this.bones = bones.clone();
            this.solver = bones.length == 2 || bones.length == 3 ? AzIkSolvers.twoBone() : AzIkSolvers.ccd(10, 0.1f);
        }

        /**
         * The point that should reach the target, in Blockbench pixels from the last chain bone's pivot. Defaults to
         * the pivot itself.
         */
        public Builder<T> effector(float x, float y, float z) {
            this.effector = new AzIkEffector(x, y, z);
            return this;
        }

        /** The solver; defaults to {@link AzIkSolvers#twoBone()} for 2 or 3 bones, otherwise CCD. */
        public Builder<T> solver(AzIkSolver solver) {
            this.solver = Objects.requireNonNull(solver, "solver");
            return this;
        }

        /** Where the effector should go each frame. Required. */
        public Builder<T> target(AzIkTarget<T> target) {
            this.target = Objects.requireNonNull(target, "target");
            return this;
        }

        /** Constraints the solver keeps chain bones within. */
        public Builder<T> constraint(AzBoneConstraint... constraints) {
            this.constraints.addAll(List.of(constraints));
            return this;
        }

        public AzIkChain<T> build() {
            return new AzIkChain<>(this);
        }
    }

    static final class ChainState {

        private AzBoneChain chain;

        private boolean failed;

        static AzBoneChain of(AzPoseContext<?> pose, String[] bones) {
            AzIkChain.ChainState state = pose.state(ChainState::new);

            if (state.chain == null && !state.failed) {
                state.chain = AzBoneChain.resolve(pose, bones);
                state.failed = state.chain == null;
            }

            return state.chain;
        }
    }
}
