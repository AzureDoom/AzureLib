package mod.azure.azurelib.rewrite.animation.dispatch.command;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.network.AzureLibNetwork;
import mod.azure.azurelib.network.packet.*;
import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.animation.AzAnimatorAccessor;
import mod.azure.azurelib.rewrite.animation.cache.AzIdentityRegistry;
import mod.azure.azurelib.rewrite.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.rewrite.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.rewrite.animation.play_behavior.AzPlayBehavior;
import mod.azure.azurelib.rewrite.animation.play_behavior.AzPlayBehaviors;
import mod.azure.azurelib.rewrite.util.codec.AzListStreamCodec;

/**
 * Represents a command structure used to dispatch a sequence of actions in the animation system. This class primarily
 * serves as a container for a list of {@link AzAction} instances that define specific operations or behaviors to be
 * executed. <br>
 * The class provides support for building complex dispatch commands by leveraging the hierarchical builder system,
 * enabling customization of animation-related functionality.
 */
public class AzCommand {

    private List<AzAction> actions;

    public AzCommand(List<AzAction> actions) {
        this.actions = actions;
    }

    public List<AzAction> actions() {
        return actions;
    }

    public static final AzListStreamCodec<AzAction> ACTION_LIST_CODEC =
        new AzListStreamCodec<>(AzAction::decode, (buf, action) -> action.encode(buf));

    public static final Function<FriendlyByteBuf, AzCommand> DECODER = buf -> {
        // Decode the list of actions using the AzListStreamCodec
        List<AzAction> actions = ACTION_LIST_CODEC.decode(buf);
        return new AzCommand(actions);
    };

    public static final BiConsumer<FriendlyByteBuf, AzCommand> ENCODER = (buf, command) -> {
        ACTION_LIST_CODEC.encode(buf, command.actions());
    };

    public static AzRootCommandBuilder builder() {
        return new AzRootCommandBuilder();
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
                .collect(Collectors.toList())
        );
    }

    public static AzCommand compose(AzCommand first, AzCommand second, AzCommand... others) {
        ArrayList allCommands = new ArrayList<AzCommand>();

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
        return create(controllerName, animationName, AzPlayBehaviors.PLAY_ONCE, 0F, 1F);
    }

    /**
     * Creates an animation command for a specific controller and animation, using the default play behavior of
     * PLAY_ONCE and allowing a starting tick offset to be specified.
     *
     * @param controllerName  the name of the animation controller to target
     * @param animationName   the name of the animation to be played
     * @param startTickOffset the starting tick offset for the animation
     * @return an AzCommand instance encapsulating the animation command for the specified controller and animation
     */
    public static AzCommand create(String controllerName, String animationName, float startTickOffset) {
        return create(controllerName, animationName, AzPlayBehaviors.PLAY_ONCE, startTickOffset, 1F);
    }

    /**
     * Creates an AzCommand instance to run an animation with a specified speed for a given controller and animation,
     * using the default play behavior of PLAY_ONCE.
     *
     * @param controllerName the name of the animation controller to target
     * @param animationName  the name of the animation to be played
     * @param animationSpeed the speed at which the animation should play
     * @return an AzCommand instance configured for the specified controller, animation, and speed
     */
    public static AzCommand createSpeed(String controllerName, String animationName, float animationSpeed) {
        return create(controllerName, animationName, AzPlayBehaviors.PLAY_ONCE, 0F, 1F);
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
        return create(controllerName, animationName, playBehavior, 0F, 1F);
    }

    /**
     * Creates an animation command for a specific controller and animation, with the ability to customize the play
     * behavior and specify a starting tick offset.
     *
     * @param controllerName the name of the animation controller to target.
     * @param animationName  the name of the animation to be played
     */
    public static AzCommand create(
        String controllerName,
        String animationName,
        AzPlayBehavior playBehavior,
        float startTickOffset
    ) {
        return create(controllerName, animationName, playBehavior, startTickOffset, 1F);
    }

    /**
     * Creates an animation command for controlling the playback speed of a specific animation and controller with the
     * specified play behavior.
     *
     * @param controllerName the name of the animation controller to target
     * @param animationName  the name of the animation to be played
     * @param playBehavior   the play behavior for the animation, defining how it should handle playback
     * @param animationSpeed the speed at which the animation should play
     * @return an AzCommand instance configured for the specified controller, animation, play behavior, and speed
     */
    public static AzCommand createSpeed(
        String controllerName,
        String animationName,
        AzPlayBehavior playBehavior,
        float animationSpeed
    ) {
        return create(controllerName, animationName, playBehavior, 0F, animationSpeed);
    }

    /**
     * Creates an animation command for a specified controller and animation, with the ability to customize the play
     * behavior, starting tick offset, and animation speed.
     *
     * @param controllerName  the name of the animation controller to target
     * @param animationName   the name of the animation to be played
     * @param playBehavior    the play behavior for the animation, defining how it should handle playback
     * @param startTickOffset the start tick offset for the animation
     * @param animationSpeed  the speed at which the animation should play
     * @return an AzCommand instance configured for the specified controller, animation, play behavior, start tick
     *         offset, and speed
     */
    public static AzCommand create(
        String controllerName,
        String animationName,
        AzPlayBehavior playBehavior,
        float startTickOffset,
        float animationSpeed
    ) {
        return builder()
            .playSequence(
                controllerName,
                sequenceBuilder -> sequenceBuilder.queue(
                    animationName,
                    props -> props.withPlayBehavior(playBehavior)
                )
            )
            .setStartTickOffset(startTickOffset)
            .setSpeed(animationSpeed)
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
        if (entity.level.isClientSide()) {
            dispatchFromClient(entity);
        } else {
            int entityId = entity.getId();
            AzEntityDispatchCommandPacket packet = new AzEntityDispatchCommandPacket(entityId, this);
            AzureLibNetwork.sendToTrackingEntityAndSelf(packet, entity);
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
            BlockPos entityBlockPos = entity.getBlockPos();
            AzBlockEntityDispatchCommandPacket packet = new AzBlockEntityDispatchCommandPacket(entityBlockPos, this);
            AzureLibNetwork.sendToEntitiesTrackingChunk(packet, (ServerLevel) entity.getLevel(), entityBlockPos);
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
        if (entity.level.isClientSide()) {
            dispatchFromClient(entity);
        } else {
            if (!AzIdentityRegistry.hasIdentity(itemStack.getItem())) {
                return;
            }

            UUID uuid = itemStack.getTag().getUUID(AzureLib.ITEM_UUID_TAG);

            if (uuid == null) {
                AzureLib.LOGGER.warn(
                    "Could not find item stack UUID during dispatch. Did you forget to register an identity for the item? Item: {}, Item Stack: {}",
                    itemStack.getItem(),
                    itemStack
                );
                return;
            }

            AzItemStackDispatchCommandPacket packet = new AzItemStackDispatchCommandPacket(uuid, this);

            AzureLibNetwork.sendToTrackingEntityAndSelf(packet, entity);
        }
    }

    private <T> void dispatchFromClient(T animatable) {
        AzAnimator<T> animator = AzAnimatorAccessor.getOrNull(animatable);

        if (animator != null) {
            actions.forEach(action -> action.handle(AzDispatchSide.CLIENT, animator));
        }
    }
}
