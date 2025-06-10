package mod.azure.azurelib.rewrite.animation.dispatch.command;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.network.AzureLibNetwork;
import mod.azure.azurelib.network.packet.AzBlockEntityDispatchCommandPacket;
import mod.azure.azurelib.network.packet.AzEntityDispatchCommandPacket;
import mod.azure.azurelib.network.packet.AzItemStackDispatchCommandPacket;
import mod.azure.azurelib.rewrite.animation.AzAnimatorAccessor;
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

    public static AzCommand create(String controllerName, String animationName) {
        return create(controllerName, animationName, AzPlayBehaviors.PLAY_ONCE);
    }

    /**
     * Creates a dispatch command to play a specified animation on a given controller.
     *
     * @param controllerName the name of the animation controller on which the animation should be played
     * @param animationName  the name of the animation to be played on the specified controller
     * @param playBehavior   the play behavior for the animation to use
     * @return an instance of {@code AzCommand} representing the command to play the desired animation
     */
    public static AzCommand create(String controllerName, String animationName, AzPlayBehavior playBehavior) {
        return builder()
            .playSequence(
                controllerName,
                sequenceBuilder -> sequenceBuilder.queue(animationName, props -> props.withPlayBehavior(playBehavior))
            )
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
            var entityId = entity.getId();
            var packet = new AzEntityDispatchCommandPacket(entityId, this);
            AzureLibNetwork.send(packet, PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity));
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
            AzureLibNetwork.send(
                packet,
                PacketDistributor.TRACKING_CHUNK.with(() -> entity.getLevel().getChunkAt(entityBlockPos))
            );
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
            var uuid = itemStack.getTag().getUUID(AzureLib.ITEM_UUID_TAG);

            if (uuid == null) {
                AzureLib.LOGGER.warn(
                    "Could not find item stack UUID during dispatch. Did you forget to register an identity for the item? Item: {}, Item Stack: {}",
                    itemStack.getItem(),
                    itemStack
                );
                return;
            }

            var packet = new AzItemStackDispatchCommandPacket(uuid, this);
            AzureLibNetwork.send(packet, PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity));
        }
    }

    private <T> void dispatchFromClient(T animatable) {
        var animator = AzAnimatorAccessor.getOrNull(animatable);

        if (animator != null) {
            actions.forEach(action -> action.handle(AzDispatchSide.CLIENT, animator));
        }
    }
}
