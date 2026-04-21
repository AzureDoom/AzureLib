package mod.azure.azurelib.animation.dispatch.command;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimatorAccessor;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.action.AzAction;
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
     * Creates an animation command for a specific controller and animation, using the default play behavior of
     * PLAY_ONCE.
     *
     * @param controllerName the name of the animation controller to target
     * @param animationName  the name of the animation to be played
     * @return an AzCommand instance encapsulating the animation command for the specified controller and animation
     */
    public static AzCommand create(String controllerName, String animationName) {
        return create(controllerName, animationName, AzPlayBehaviors.PLAY_ONCE, 0F, 1F, 0F, 0F, 0F, false);
    }

    /**
     * Creates an animation command for a specific controller and animation, with the ability to customize the play
     * behavior. A default starting tick offset of 0 is used.
     *
     * @param controllerName the name of the animation controller to target
     * @param animationName  the name of the animation to be played
     * @param playBehavior   the play behavior for the animation, defining how it should handle playback
     * @return an AzCommand instance that encapsulates the animation command for the specified controller and animation
     */
    public static AzCommand create(String controllerName, String animationName, AzPlayBehavior playBehavior) {
        return create(controllerName, animationName, playBehavior, 0F, 1F, 0F, 0F, 0F, false);
    }

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
            .setRepeatAmount(controllerName, repeatXTimes)
            .setReverseAnimation(controllerName, isReversing)
            .build();
    }

    /**
     * Creates a root-level (all controllers) animation command with specified parameters for animation name, play
     * behavior, start tick offset, and animation speed.
     *
     * @param animationName   the name of the animation to be played
     * @param playBehavior    the play behavior for the animation, defining how it should handle playback
     * @param startTickOffset the starting tick offset for the animation
     * @param animationSpeed  the speed at which the animation should play
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
        if (entity.getLevel().isClientSide()) {
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
     * animation commands either from the client or the server side.
     *
     * @param entity    the {@link Entity} associated with the {@link ItemStack}.
     * @param itemStack the {@link ItemStack} on which the animation commands are dispatched.
     */
    public void sendForItem(Entity entity, ItemStack itemStack) {
        if (entity.level().isClientSide()) {
            dispatchFromClient(itemStack);
        } else {
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

            var packet = new AzItemStackDispatchCommandPacket(uuid, this);
            Services.NETWORK.sendToTrackingEntityAndSelf(packet, entity);
        }
    }

    private <T> void dispatchFromClient(T animatable) {
        var animator = AzAnimatorAccessor.getOrNull(animatable);

        if (animator != null) {
            actions.forEach(action -> action.handle(AzDispatchSide.CLIENT, animator));
        }
    }
}
