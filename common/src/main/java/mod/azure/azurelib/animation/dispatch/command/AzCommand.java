package mod.azure.azurelib.animation.dispatch.command;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimatorAccessor;
import mod.azure.azurelib.animation.cache.AzIdentifiableItemStackAnimatorCache;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.AzPendingDispatches;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.animation.dispatch.command.sequence.AzSequence;
import mod.azure.azurelib.animation.impl.AzItemAnimatorView;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehavior;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import mod.azure.azurelib.network.packet.AzBlockEntityDispatchCommandPacket;
import mod.azure.azurelib.network.packet.AzEntityDispatchCommandPacket;
import mod.azure.azurelib.network.packet.AzItemStackDispatchCommandPacket;
import mod.azure.azurelib.platform.Services;
import mod.azure.azurelib.util.codec.AzListStreamCodec;

/**
 * Represents a command structure used to dispatch a sequence of actions in the animation system. This class primarily
 * serves as a container for a list of {@link AzAction} instances that define specific operations or behaviors to be
 * executed. <br>
 * The class provides support for building complex dispatch commands by leveraging the hierarchical builder system,
 * enabling customization of animation-related functionality.
 */
@SuppressWarnings({ "unused", "deprecation" })
public record AzCommand(List<AzAction> actions) {

    public static final AzListStreamCodec<AzAction> ACTION_LIST_CODEC =
        new AzListStreamCodec<>(AzAction::decode, (buf, action) -> action.encode(buf));

    public static final Function<FriendlyByteBuf, AzCommand> DECODER = buf -> {
        // Decode the list of actions using the AzListStreamCodec
        List<AzAction> actions = ACTION_LIST_CODEC.decode(buf);
        return new AzCommand(actions);
    };

    public static final BiConsumer<FriendlyByteBuf, AzCommand> ENCODER = (buf, command) -> {
        // Encode the list of actions using the AzListStreamCodec
        ACTION_LIST_CODEC.encode(buf, command.actions());
    };

    public static AzRootCommandBuilder rootBuilder() {
        return new AzRootCommandBuilder();
    }

    public static AzControllerCommandBuilder controllerBuilder() {
        return new AzControllerCommandBuilder();
    }

    public static AzCommand compose(Collection<AzCommand> commands) {
        if (commands.isEmpty()) {
            throw new IllegalArgumentException("Attempted to compose an empty collection of commands.");
        } else if (commands.size() == 1) {
            return commands.iterator().next();
        }

        return new AzCommand(
            commands.stream()
                .flatMap(command -> command.actions().stream())
                .toList()
        );
    }

    public static AzCommand compose(AzCommand first, AzCommand second, AzCommand... others) {
        var allCommands = new ArrayList<AzCommand>();

        allCommands.add(first);
        allCommands.add(second);
        Collections.addAll(allCommands, others);

        return compose(allCommands);
    }

    /**
     * Creates an animation command for a specific controller and animation, played the way its animation file says (the
     * loop mode set in Blockbench: play once, loop, hold, ping-pong, repeat or freeze). Animations whose file has no
     * loop mode play once. Only plays the animation: the controller's speed, transition length, offsets, repeat amount
     * and direction are left as they are.
     *
     * @param controllerName the name of the animation controller to target
     * @param animationName  the name of the animation to be played
     * @return an AzCommand instance encapsulating the animation command for the specified controller and animation
     */
    public static AzCommand create(String controllerName, String animationName) {
        return create(controllerName, animationName, AzPlayBehaviors.AS_AUTHORED);
    }

    /**
     * Creates an animation command for a specific controller and animation, with the ability to customize the play
     * behavior. Only plays the animation: the controller's speed, transition length, offsets, repeat amount and
     * direction are left as they are (for example as configured in its {@code AzAnimationControllerBuilder}).
     *
     * @param controllerName the name of the animation controller to target
     * @param animationName  the name of the animation to be played
     * @param playBehavior   the play behavior for the animation, defining how it should handle playback
     * @return an AzCommand instance that encapsulates the animation command for the specified controller and animation
     */
    public static AzCommand create(String controllerName, String animationName, AzPlayBehavior playBehavior) {
        return controllerBuilder()
            .playSequence(
                controllerName,
                sequenceBuilder -> sequenceBuilder.queue(
                    animationName,
                    props -> props.withPlayBehavior(playBehavior)
                )
            )
            .build();
    }

    /**
     * Creates a command that plays an {@link AzSequence} on a single controller.
     *
     * @param controllerName the name of the animation controller to target
     * @param sequence       the sequence to play
     * @return an AzCommand that plays the sequence
     */
    public static AzCommand create(String controllerName, AzSequence sequence) {
        return sequence.toCommand(controllerName);
    }

    /**
     * Creates a command that plays an {@link AzSequence} on every controller.
     *
     * @param sequence the sequence to play
     * @return an AzCommand that plays the sequence on all controllers
     */
    public static AzCommand createRoot(AzSequence sequence) {
        return sequence.toRootCommand();
    }

    /**
     * Creates an animation command for a specific controller that plays the animation and sets every playback property
     * on the controller. These values stay on the controller for later commands too, though the freeze point only
     * affects animations played with {@link AzPlayBehaviors#FREEZE_ON_FRAME}. For settings that should only last for
     * one animation, set them on a sequence stage instead
     * ({@code AzSequence.Builder#then(name, behavior, props -> ...)}).
     *
     * @param controllerName   the name of the animation controller to target
     * @param animationName    the name of the animation to be played
     * @param playBehavior     the play behavior for the animation, defining how it should handle playback
     * @param startTickOffset  the starting tick offset for the animation
     * @param animationSpeed   the speed at which the animation should play
     * @param transitionLength the length, in ticks, of the transition into the animation
     * @param freezeTickOffset the tick at which FREEZE_ON_FRAME freezes the animation
     * @param repeatXTimes     the total number of plays for REPEAT_X_TIMES
     * @param isReversing      whether the animation plays in reverse
     * @return an AzCommand instance configured with the specified animation settings
     */
    public static AzCommand create(
        String controllerName,
        String animationName,
        AzPlayBehavior playBehavior,
        float startTickOffset,
        float animationSpeed,
        float transitionLength,
        float freezeTickOffset,
        float repeatXTimes,
        boolean isReversing
    ) {
        return controllerBuilder()
            .playSequence(
                controllerName,
                sequenceBuilder -> sequenceBuilder.queue(
                    animationName,
                    props -> props.withPlayBehavior(playBehavior)
                )
            )
            .setFreezeTickOffset(controllerName, freezeTickOffset)
            .setStartTickOffset(controllerName, startTickOffset)
            .setSpeed(controllerName, animationSpeed)
            .setTransitionSpeed(controllerName, transitionLength)
            .setRepeatAmount(controllerName, repeatXTimes)
            .setReverseAnimation(controllerName, isReversing)
            .build();
    }

    /**
     * Creates a root-level (all controllers) animation command that plays the animation and sets every playback
     * property on all controllers. These values stay on the controllers for later commands too.
     *
     * @param animationName    the name of the animation to be played
     * @param playBehavior     the play behavior for the animation, defining how it should handle playback
     * @param startTickOffset  the starting tick offset for the animation
     * @param animationSpeed   the speed at which the animation should play
     * @param transitionLength the length, in ticks, of the transition into the animation
     * @param freezeTickOffset the tick at which FREEZE_ON_FRAME freezes the animation
     * @param repeatXTimes     the total number of plays for REPEAT_X_TIMES
     * @param isReversing      whether the animation plays in reverse
     * @return an AzCommand instance configured with the specified animation settings
     */
    public static AzCommand createRoot(
        String animationName,
        AzPlayBehavior playBehavior,
        float startTickOffset,
        float animationSpeed,
        float transitionLength,
        float freezeTickOffset,
        float repeatXTimes,
        boolean isReversing
    ) {
        return rootBuilder()
            .playSequence(
                sequenceBuilder -> sequenceBuilder.queue(
                    animationName,
                    props -> props.withPlayBehavior(playBehavior)
                )
            )
            .setFreezeTickOffset(freezeTickOffset)
            .setStartTickOffset(startTickOffset)
            .setSpeed(animationSpeed)
            .setTransitionSpeed(transitionLength)
            .setRepeatAmount(repeatXTimes)
            .setReverseAnimation(isReversing)
            .build();
    }

    /**
     * Sends animation commands for the specified entity based on the configured dispatch origin. The method determines
     * whether the command should proceed, logs a warning if it cannot, and dispatches the animation commands either
     * from the client or the server side.
     *
     * @param entity the target {@link Entity} for which the animation commands are dispatched.
     */
    public void sendForEntity(Entity entity) {
        if (entity.level().isClientSide()) {
            dispatchFromClient(entity);
        } else {
            var entityId = entity.getId();
            var packet = new AzEntityDispatchCommandPacket(entityId, this);
            Services.NETWORK.sendToTrackingEntityAndSelf(packet, entity);
        }
    }

    /**
     * Sends animation commands for the specified block entity based on the configured dispatch origin. The method
     * determines whether the command should proceed, logs a warning if it cannot, and dispatches the animation commands
     * either from the client or the server side.
     *
     * @param entity the target {@link BlockEntity} for which the animation commands are dispatched.
     */
    public void sendForBlockEntity(BlockEntity entity) {
        if (entity.getLevel() != null && entity.getLevel().isClientSide()) {
            dispatchFromClient(entity);
        } else {
            var entityBlockPos = entity.getBlockPos();
            var packet = new AzBlockEntityDispatchCommandPacket(entityBlockPos, this);
            Services.NETWORK.sendToEntitiesTrackingChunk(packet, (ServerLevel) entity.getLevel(), entityBlockPos);
        }
    }

    /**
     * Sends animation commands for the specified item based on the configured dispatch origin. The method determines
     * whether the command can proceed, assigns a unique identifier to the item if required, and dispatches the
     * animation commands either from the client or the server side. <br>
     * The command runs on every animator the stack has. If the item's renderer uses a separate first-person animator
     * ({@code AzItemRendererConfig.Builder#useSeparateFirstPersonAnimator()}), both the first-person and third-person
     * views play it; use {@link #sendForItem(Entity, ItemStack, AzItemAnimatorView)} to target only one of them.
     *
     * @param entity    the {@link Entity} associated with the {@link ItemStack}.
     * @param itemStack the {@link ItemStack} on which the animation commands are dispatched.
     */
    public void sendForItem(Entity entity, ItemStack itemStack) {
        sendForItem(entity, itemStack, null);
    }

    /**
     * Sends animation commands for the specified item, targeting only the animator for the given view. <br>
     * {@link AzItemAnimatorView#FIRST_PERSON} only has an effect when the item's renderer was built with
     * {@code useSeparateFirstPersonAnimator()}; {@link AzItemAnimatorView#DEFAULT} is the animator used for third
     * person and every other non-first-person context.
     *
     * @param entity    the {@link Entity} associated with the {@link ItemStack}.
     * @param itemStack the {@link ItemStack} on which the animation commands are dispatched.
     * @param view      the animator to target, or null to target every animator the stack has.
     */
    public void sendForItem(Entity entity, ItemStack itemStack, @Nullable AzItemAnimatorView view) {
        if (itemStack.getTag() == null) {
            return;
        }
        if (!itemStack.getTag().contains(AzureLib.ITEM_UUID_TAG)) {
            AzureLib.LOGGER.warn(
                AzureLib.MAIN_MARKER,
                "Missing '{}' UUID tag on ItemStack (item={}). "
                    + "Cannot dispatch animation commands. Ensure this is an AzureLib-animated item and that its UUID is assigned.",
                AzureLib.ITEM_UUID_TAG,
                itemStack.getItem().builtInRegistryHolder().key().location()
            );
            return;
        }

        var uuid = itemStack.getTag().getUUID(AzureLib.ITEM_UUID_TAG);

        if (entity.level().isClientSide()) {
            AzIdentifiableItemStackAnimatorCache.getInstance()
                .dispatchOrQueue(
                    uuid,
                    view,
                    animator -> actions.forEach(action -> action.handle(AzDispatchSide.CLIENT, animator))
                );
        } else {
            var packet = new AzItemStackDispatchCommandPacket(uuid, this, Optional.ofNullable(view));
            Services.NETWORK.sendToTrackingEntityAndSelf(packet, entity);
        }
    }

    /**
     * Shorthand for {@link #sendForItem(Entity, ItemStack, AzItemAnimatorView)} with
     * {@link AzItemAnimatorView#FIRST_PERSON}.
     */
    public void sendForItemFirstPerson(Entity entity, ItemStack itemStack) {
        sendForItem(entity, itemStack, AzItemAnimatorView.FIRST_PERSON);
    }

    /**
     * Shorthand for {@link #sendForItem(Entity, ItemStack, AzItemAnimatorView)} with
     * {@link AzItemAnimatorView#DEFAULT}, the animator used for third person (and every other non-first-person
     * context).
     */
    public void sendForItemThirdPerson(Entity entity, ItemStack itemStack) {
        sendForItem(entity, itemStack, AzItemAnimatorView.DEFAULT);
    }

    private <T> void dispatchFromClient(T animatable) {
        var animator = AzAnimatorAccessor.getOrNull(animatable);

        if (animator != null) {
            actions.forEach(action -> action.handle(AzDispatchSide.CLIENT, animator));
            animator.forEachLinkedAnimator(
                linked -> actions.forEach(action -> action.handle(AzDispatchSide.CLIENT, linked))
            );
        } else {
            AzPendingDispatches.enqueue(animatable, an -> actions.forEach(a -> a.handle(AzDispatchSide.CLIENT, an)));
        }
    }
}
