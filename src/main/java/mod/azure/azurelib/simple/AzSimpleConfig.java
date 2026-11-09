package mod.azure.azurelib.simple;

import mod.azure.azurelib.animation.AzAnimatorConfig;
import mod.azure.azurelib.animation.controller.AzBlendMode;
import mod.azure.azurelib.animation.controller.AzBoneMask;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehavior;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import mod.azure.azurelib.animation.pose.AzPoseBone;
import mod.azure.azurelib.animation.pose.AzPoseModifier;
import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.util.ResourceLocation;
import mod.azure.azurelib.util.math.Mth;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public final class AzSimpleConfig<T> {

    public static final int NO_PRIORITY = Integer.MIN_VALUE;

    public static final float DEFAULT_MOVEMENT_THRESHOLD = 0.15F;

    private static final double NON_LIVING_MOVEMENT_SQR = 1.0E-4;

    public enum Kind {

        ENTITY("entity"),
        ITEM("item"),
        BLOCK("block");

        private final String folder;

        Kind(String folder) {
            this.folder = folder;
        }

        public String folder() {
            return folder;
        }
    }

    public static final class State<T> {

        private final String animation;

        private final Predicate<T> condition;

        private final AzPlayBehavior behavior;

        private final int priority;

        public State(
            String animation,
            Predicate<T> condition,
            AzPlayBehavior behavior,
            int priority
        ) {
            this.animation = animation;
            this.condition = condition;
            this.behavior = behavior;
            this.priority = priority;
        }

        public String animation() {
            return animation;
        }

        public Predicate<T> condition() {
            return condition;
        }

        public AzPlayBehavior behavior() {
            return behavior;
        }

        public int priority() {
            return priority;
        }

        public boolean interruptsActions() {
            return priority != NO_PRIORITY;
        }
    }

    public static final class Action {

        private final String animation;

        private final boolean hold;

        private final int priority;

        private final AzSimpleInterruptPolicy interruptPolicy;

        private final String layer;

        private final int transitionLength;

        private final double speed;

        public Action(
            String animation,
            boolean hold,
            int priority,
            AzSimpleInterruptPolicy interruptPolicy,
            String layer,
            int transitionLength,
            double speed
        ) {
            this.animation = animation;
            this.hold = hold;
            this.priority = priority;
            this.interruptPolicy = interruptPolicy;
            this.layer = layer;
            this.transitionLength = transitionLength;
            this.speed = speed;
        }

        public String animation() {
            return animation;
        }

        public boolean hold() {
            return hold;
        }

        public int priority() {
            return priority;
        }

        public AzSimpleInterruptPolicy interruptPolicy() {
            return interruptPolicy;
        }

        public String layer() {
            return layer;
        }

        public int transitionLength() {
            return transitionLength;
        }

        public double speed() {
            return speed;
        }

        public static Action defaults(String animation) {
            return new Action(
                animation,
                false,
                0,
                AzSimpleInterruptPolicy.ALWAYS,
                null,
                -1,
                0
            );
        }
    }

    public static final class Layer<T> {

        private final String name;

        private final String animation;

        private final Predicate<T> condition;

        private final boolean automatic;

        private final AzBoneMask boneMask;

        private final AzBlendMode blendMode;

        private final float weight;

        private final int fadeTicks;

        private final int transitionLength;

        private final double speed;

        private final AzPlayBehavior behavior;

        public Layer(
            String name,
            String animation,
            Predicate<T> condition,
            boolean automatic,
            AzBoneMask boneMask,
            AzBlendMode blendMode,
            float weight,
            int fadeTicks,
            int transitionLength,
            double speed,
            AzPlayBehavior behavior
        ) {
            this.name = name;
            this.animation = animation;
            this.condition = condition;
            this.automatic = automatic;
            this.boneMask = boneMask;
            this.blendMode = blendMode;
            this.weight = weight;
            this.fadeTicks = fadeTicks;
            this.transitionLength = transitionLength;
            this.speed = speed;
            this.behavior = behavior;
        }

        public String name() {
            return name;
        }

        public String animation() {
            return animation;
        }

        public Predicate<T> condition() {
            return condition;
        }

        public boolean automatic() {
            return automatic;
        }

        public AzBoneMask boneMask() {
            return boneMask;
        }

        public AzBlendMode blendMode() {
            return blendMode;
        }

        public float weight() {
            return weight;
        }

        public int fadeTicks() {
            return fadeTicks;
        }

        public int transitionLength() {
            return transitionLength;
        }

        public double speed() {
            return speed;
        }

        public AzPlayBehavior behavior() {
            return behavior;
        }
    }

    public static final class PoseEntry<T> {

        private final String id;

        private final AzPoseModifier<T> modifier;

        private final int priority;

        public PoseEntry(
            String id,
            AzPoseModifier<T> modifier,
            int priority
        ) {
            this.id = id;
            this.modifier = modifier;
            this.priority = priority;
        }

        public String id() {
            return id;
        }

        public AzPoseModifier<T> modifier() {
            return modifier;
        }

        public int priority() {
            return priority;
        }
    }

    private final Kind kind;

    private final Function<T, ResourceLocation> modelLocation;

    private final Function<T, ResourceLocation> textureLocation;

    private final Function<T, ResourceLocation> animationLocation;

    private final List<State<T>> states;

    private final Map<String, Action> actions;

    private final List<Layer<T>> layers;

    private final List<PoseEntry<T>> poseModifiers;

    private final int transitionLength;

    private final double animationSpeed;

    private final AzAnimatorConfig animatorConfig;

    private final float shadowRadius;

    private final float scaleWidth;

    private final float scaleHeight;

    private final boolean newItemOffset;

    private final boolean separateFirstPersonAnimator;

    private AzSimpleConfig(Builder<T, ?> builder, List<State<T>> states) {
        this.kind = builder.kind;
        this.modelLocation = builder.modelLocation;
        this.textureLocation = builder.textureLocation;
        this.animationLocation = builder.animationLocation;
        this.states = Collections.unmodifiableList(new ArrayList<>(states));
        this.actions = Collections.unmodifiableMap(new LinkedHashMap<>(builder.actions));
        this.layers = Collections.unmodifiableList(new ArrayList<>(builder.layers));
        this.poseModifiers = Collections.unmodifiableList(new ArrayList<>(builder.poseModifiers));
        this.transitionLength = builder.transitionLength;
        this.animationSpeed = builder.animationSpeed;
        this.animatorConfig = builder.animatorConfig;
        this.shadowRadius = builder.shadowRadius;
        this.scaleWidth = builder.scaleWidth;
        this.scaleHeight = builder.scaleHeight;
        this.newItemOffset = builder.newItemOffset;
        this.separateFirstPersonAnimator = builder.separateFirstPersonAnimator;
    }

    @SuppressWarnings("unchecked")
    public static <T> AzSimpleConfig<T> narrow(AzSimpleConfig<? super T> config) {
        return (AzSimpleConfig<T>) config;
    }

    static ResourceLocation conventionPath(ResourceLocation id, Kind kind, String prefix, String suffix) {
        return new ResourceLocation(
            id.getResourceDomain(),
            prefix + "/" + kind.folder() + "/" + id.getResourcePath() + suffix
        );
    }

    public static boolean isMoving(Entity entity, float threshold) {
        if (entity instanceof LivingEntity living) {
            return Mc.limbSwingAmount(living) > threshold;
        }

        double dx = Mc.x(entity) - Mc.prevX(entity);
        double dz = Mc.z(entity) - Mc.prevZ(entity);

        return dx * dx + dz * dz > NON_LIVING_MOVEMENT_SQR;
    }

    public Kind kind() {
        return kind;
    }

    public ResourceLocation modelLocation(T animatable) {
        return modelLocation.apply(animatable);
    }

    public ResourceLocation textureLocation(T animatable) {
        return textureLocation.apply(animatable);
    }

    public ResourceLocation animationLocation(T animatable) {
        return animationLocation.apply(animatable);
    }

    public List<State<T>> states() {
        return states;
    }

    public Map<String, Action> actions() {
        return actions;
    }

    public Action action(String animation) {
        Action action = actions.get(animation);
        return action != null ? action : Action.defaults(animation);
    }

    public List<Layer<T>> layers() {
        return layers;
    }

    public List<PoseEntry<T>> poseModifiers() {
        return poseModifiers;
    }

    public int transitionLength() {
        return transitionLength;
    }

    public double animationSpeed() {
        return animationSpeed;
    }

    public AzAnimatorConfig animatorConfig() {
        return animatorConfig;
    }

    public float shadowRadius() {
        return shadowRadius;
    }

    public float scaleWidth() {
        return scaleWidth;
    }

    public float scaleHeight() {
        return scaleHeight;
    }

    public boolean newItemOffset() {
        return newItemOffset;
    }

    public boolean separateFirstPersonAnimator() {
        return separateFirstPersonAnimator;
    }

    public abstract static class Builder<T, B extends Builder<T, B>> {

        protected final Kind kind;

        private Function<T, ResourceLocation> modelLocation;

        private Function<T, ResourceLocation> textureLocation;

        private Function<T, ResourceLocation> animationLocation;

        protected String idleAnimation;

        protected final List<State<T>> customStates = new ArrayList<>();

        private final Map<String, Action> actions = new LinkedHashMap<>();

        private final List<Layer<T>> layers = new ArrayList<>();

        private final List<PoseEntry<T>> poseModifiers = new ArrayList<>();

        private int transitionLength = -1;

        private double animationSpeed = 1;

        private AzAnimatorConfig animatorConfig = AzAnimatorConfig.defaultConfig();

        protected float shadowRadius;

        protected float scaleWidth = 1;

        protected float scaleHeight = 1;

        protected boolean newItemOffset;

        protected boolean separateFirstPersonAnimator;

        protected Builder(Kind kind, ResourceLocation id) {
            Objects.requireNonNull(id, "id");
            this.kind = kind;

            ResourceLocation model = conventionPath(id, kind, "geo", ".geo.json");
            ResourceLocation texture = conventionPath(id, kind, "textures", ".png");
            ResourceLocation animations = conventionPath(id, kind, "animations", ".animation.json");

            this.modelLocation = $ -> model;
            this.textureLocation = $ -> texture;
            this.animationLocation = $ -> animations;
        }

        protected abstract B self();

        public abstract AzSimpleConfig<T> build();

        public B model(ResourceLocation model) {
            Objects.requireNonNull(model, "model");
            this.modelLocation = $ -> model;
            return self();
        }

        public B model(Function<? super T, ResourceLocation> model) {
            Objects.requireNonNull(model, "model");
            this.modelLocation = model::apply;
            return self();
        }

        public B texture(ResourceLocation texture) {
            Objects.requireNonNull(texture, "texture");
            this.textureLocation = $ -> texture;
            return self();
        }

        public B texture(Function<? super T, ResourceLocation> texture) {
            Objects.requireNonNull(texture, "texture");
            this.textureLocation = texture::apply;
            return self();
        }

        public B animations(ResourceLocation animations) {
            Objects.requireNonNull(animations, "animations");
            this.animationLocation = $ -> animations;
            return self();
        }

        public B animations(Function<? super T, ResourceLocation> animations) {
            Objects.requireNonNull(animations, "animations");
            this.animationLocation = animations::apply;
            return self();
        }

        public B idle(String animation) {
            this.idleAnimation = requireName(animation);
            return self();
        }

        public B when(Predicate<? super T> condition, String animation) {
            return when(condition, animation, AzPlayBehaviors.LOOP);
        }

        public B when(Predicate<? super T> condition, String animation, AzPlayBehavior behavior) {
            return addState(condition, animation, behavior, NO_PRIORITY);
        }

        public B when(Predicate<? super T> condition, String animation, AzPlayBehavior behavior, int priority) {
            if (priority == NO_PRIORITY) {
                throw new IllegalArgumentException("Priority " + NO_PRIORITY + " is reserved for 'no priority'");
            }

            return addState(condition, animation, behavior, priority);
        }

        protected B addState(Predicate<? super T> condition, String animation, AzPlayBehavior behavior, int priority) {
            Objects.requireNonNull(condition, "condition");
            Objects.requireNonNull(behavior, "behavior");
            Predicate<T> typed = condition::test;
            customStates.add(new State<>(requireName(animation), typed, behavior, priority));
            return self();
        }

        public B transition(int ticks) {
            this.transitionLength = Math.max(0, ticks);
            return self();
        }

        public B speed(double speed) {
            this.animationSpeed = speed;
            return self();
        }

        public B animation(String animation) {
            return animation(animation, $ -> {});
        }

        public B animation(String animation, Consumer<ActionBuilder> settings) {
            ActionBuilder builder = new ActionBuilder(requireName(animation));
            settings.accept(builder);
            actions.put(animation, builder.build());
            return self();
        }

        public LayerBuilder<T, B> layer(String name) {
            return new LayerBuilder<>(self(), requireName(name));
        }

        public B pose(String id, AzPoseModifier<? super T> modifier) {
            return pose(id, modifier, 0);
        }

        @SuppressWarnings("unchecked")
        public B pose(String id, AzPoseModifier<? super T> modifier, int priority) {
            Objects.requireNonNull(modifier, "modifier");
            AzPoseModifier<T> typed = (AzPoseModifier<T>) modifier;
            poseModifiers.add(new PoseEntry<>(requireName(id), typed, priority));
            return self();
        }

        public B boneTransform(String bone, AzSimpleBoneCallback<? super T> callback) {
            requireName(bone);
            Objects.requireNonNull(callback, "callback");
            AzPoseModifier<T> modifier = pose -> {
                AzPoseBone poseBone = pose.bone(bone);

                if (poseBone.exists()) {
                    callback.apply(pose.animatable(), poseBone, pose.partialTick());
                }
            };
            poseModifiers.add(new PoseEntry<>("az_simple_bone/" + bone + "/" + poseModifiers.size(), modifier, 0));
            return self();
        }

        public B animatorConfig(AzAnimatorConfig animatorConfig) {
            this.animatorConfig = Objects.requireNonNull(animatorConfig, "animatorConfig");
            return self();
        }

        protected AzSimpleConfig<T> build(List<State<T>> states) {
            validate();
            return new AzSimpleConfig<>(this, states);
        }

        protected List<State<T>> defaultStates() {
            ArrayList<State<T>> states = new ArrayList<>(customStates);

            if (idleAnimation != null) {
                states.add(new State<>(idleAnimation, $ -> true, AzPlayBehaviors.LOOP, NO_PRIORITY));
            }

            return states;
        }

        private void validate() {
            HashSet<String> names = new HashSet<>();
            names.add(AzSimple.BASE_CONTROLLER);

            for (Layer<T> layer : layers) {
                if (!names.add(layer.name())) {
                    throw new IllegalStateException(
                        "Duplicate or reserved layer name '" + layer.name() + "' (\"" + AzSimple.BASE_CONTROLLER
                            + "\" is used by the base controller)"
                    );
                }

                if (layer.automatic() && layer.animation() == null) {
                    throw new IllegalStateException(
                        "Layer '" + layer.name() + "' plays by itself but has no animation"
                    );
                }
            }

            for (Action action : actions.values()) {
                if (action.layer() != null && !names.contains(action.layer())) {
                    throw new IllegalStateException(
                        "Animation '" + action.animation() + "' targets unknown layer '" + action.layer() + "'"
                    );
                }
            }
        }

        void addLayer(Layer<T> layer) {
            layers.add(layer);
        }

        static String requireName(String name) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Name must not be empty");
            }

            return name;
        }
    }

    public static final class EntityBuilder<T extends Entity> extends Builder<T, EntityBuilder<T>> {

        private String walkAnimation;

        private String deathAnimation;

        private Predicate<T> moving;

        private float movementThreshold = DEFAULT_MOVEMENT_THRESHOLD;

        EntityBuilder(ResourceLocation id) {
            super(Kind.ENTITY, id);
        }

        @Override
        protected EntityBuilder<T> self() {
            return this;
        }

        public EntityBuilder<T> walk(String animation) {
            this.walkAnimation = requireName(animation);
            return this;
        }

        public EntityBuilder<T> death(String animation) {
            this.deathAnimation = requireName(animation);
            return this;
        }

        public EntityBuilder<T> moving(Predicate<? super T> moving) {
            Objects.requireNonNull(moving, "moving");
            this.moving = moving::test;
            return this;
        }

        public EntityBuilder<T> movementThreshold(float threshold) {
            this.movementThreshold = Math.max(0, threshold);
            return this;
        }

        public EntityBuilder<T> shadow(float radius) {
            this.shadowRadius = Math.max(0, radius);
            return this;
        }

        public EntityBuilder<T> scale(float scale) {
            return scale(scale, scale);
        }

        public EntityBuilder<T> scale(float width, float height) {
            this.scaleWidth = width;
            this.scaleHeight = height;
            return this;
        }

        @Override
        public AzSimpleConfig<T> build() {
            ArrayList<State<T>> states = new ArrayList<>();

            if (deathAnimation != null) {
                states.add(
                    new State<>(
                        deathAnimation,
                        entity -> !entity.isAlive(),
                        AzPlayBehaviors.HOLD_ON_LAST_FRAME,
                        Integer.MAX_VALUE
                    )
                );
            }

            states.addAll(customStates);

            if (walkAnimation != null) {
                float threshold = movementThreshold;
                Predicate<T> test = moving != null ? moving : entity -> isMoving(entity, threshold);
                states.add(new State<>(walkAnimation, test, AzPlayBehaviors.LOOP, NO_PRIORITY));
            }

            if (idleAnimation != null) {
                states.add(new State<>(idleAnimation, $ -> true, AzPlayBehaviors.LOOP, NO_PRIORITY));
            }

            return build(states);
        }
    }

    public static final class ItemBuilder extends Builder<ItemStack, ItemBuilder> {

        ItemBuilder(ResourceLocation id) {
            super(Kind.ITEM, id);
        }

        @Override
        protected ItemBuilder self() {
            return this;
        }

        public ItemBuilder newOffset(boolean newOffset) {
            this.newItemOffset = newOffset;
            return this;
        }

        public ItemBuilder separateFirstPersonAnimator() {
            this.separateFirstPersonAnimator = true;
            return this;
        }

        public ItemBuilder scale(float scale) {
            return scale(scale, scale);
        }

        public ItemBuilder scale(float width, float height) {
            this.scaleWidth = width;
            this.scaleHeight = height;
            return this;
        }

        @Override
        public AzSimpleConfig<ItemStack> build() {
            return build(defaultStates());
        }
    }

    public static final class BlockBuilder<T extends BlockEntity> extends Builder<T, BlockBuilder<T>> {

        BlockBuilder(ResourceLocation id) {
            super(Kind.BLOCK, id);
        }

        @Override
        protected BlockBuilder<T> self() {
            return this;
        }

        public BlockBuilder<T> scale(float scale) {
            return scale(scale, scale);
        }

        public BlockBuilder<T> scale(float width, float height) {
            this.scaleWidth = width;
            this.scaleHeight = height;
            return this;
        }

        @Override
        public AzSimpleConfig<T> build() {
            return build(defaultStates());
        }
    }

    public static final class ActionBuilder {

        private final String animation;

        private boolean hold;

        private int priority;

        private AzSimpleInterruptPolicy interruptPolicy = AzSimpleInterruptPolicy.ALWAYS;

        private String layer;

        private int transitionLength = -1;

        private double speed;

        ActionBuilder(String animation) {
            this.animation = animation;
        }

        public ActionBuilder hold() {
            this.hold = true;
            return this;
        }

        public ActionBuilder priority(int priority) {
            if (priority == NO_PRIORITY) {
                throw new IllegalArgumentException("Priority " + NO_PRIORITY + " is reserved");
            }

            this.priority = priority;
            return this;
        }

        public ActionBuilder interrupt(AzSimpleInterruptPolicy policy) {
            this.interruptPolicy = Objects.requireNonNull(policy, "policy");
            return this;
        }

        public ActionBuilder uninterruptible() {
            return interrupt(AzSimpleInterruptPolicy.NEVER);
        }

        public ActionBuilder layer(String layer) {
            this.layer = Builder.requireName(layer);
            return this;
        }

        public ActionBuilder transition(int ticks) {
            this.transitionLength = Math.max(0, ticks);
            return this;
        }

        public ActionBuilder speed(double speed) {
            this.speed = speed;
            return this;
        }

        Action build() {
            return new Action(animation, hold, priority, interruptPolicy, layer, transitionLength, speed);
        }
    }

    public static final class LayerBuilder<T, B extends Builder<T, B>> {

        private final B parent;

        private final String name;

        private String animation;

        private Predicate<T> condition;

        private boolean automatic;

        private AzBoneMask boneMask = AzBoneMask.ALL;

        private AzBlendMode blendMode = AzBlendMode.OVERRIDE;

        private float weight = 1;

        private int fadeTicks;

        private int transitionLength = -1;

        private double speed;

        private AzPlayBehavior behavior = AzPlayBehaviors.LOOP;

        LayerBuilder(B parent, String name) {
            this.parent = parent;
            this.name = name;
        }

        public LayerBuilder<T, B> animation(String animation) {
            this.animation = Builder.requireName(animation);
            return this;
        }

        public LayerBuilder<T, B> bones(String... bones) {
            this.boneMask = AzBoneMask.only(bones);
            return this;
        }

        public LayerBuilder<T, B> exceptBones(String... bones) {
            this.boneMask = AzBoneMask.except(bones);
            return this;
        }

        public LayerBuilder<T, B> weight(float weight) {
            this.weight = Mth.clamp(weight, 0, 1);
            return this;
        }

        public LayerBuilder<T, B> additive() {
            this.blendMode = AzBlendMode.ADDITIVE;
            return this;
        }

        public LayerBuilder<T, B> override() {
            this.blendMode = AzBlendMode.OVERRIDE;
            return this;
        }

        public LayerBuilder<T, B> fade(int ticks) {
            this.fadeTicks = Math.max(0, ticks);
            return this;
        }

        public LayerBuilder<T, B> when(Predicate<? super T> condition) {
            Objects.requireNonNull(condition, "condition");
            this.condition = condition::test;
            this.automatic = true;
            return this;
        }

        public LayerBuilder<T, B> always() {
            this.condition = null;
            this.automatic = true;
            return this;
        }

        public LayerBuilder<T, B> playBehavior(AzPlayBehavior behavior) {
            this.behavior = Objects.requireNonNull(behavior, "behavior");
            return this;
        }

        public LayerBuilder<T, B> transition(int ticks) {
            this.transitionLength = Math.max(0, ticks);
            return this;
        }

        public LayerBuilder<T, B> speed(double speed) {
            this.speed = speed;
            return this;
        }

        public B endLayer() {
            parent.addLayer(
                new Layer<>(
                    name,
                    animation,
                    condition,
                    automatic,
                    boneMask,
                    blendMode,
                    weight,
                    fadeTicks,
                    transitionLength,
                    speed,
                    behavior
                )
            );
            return parent;
        }
    }
}
