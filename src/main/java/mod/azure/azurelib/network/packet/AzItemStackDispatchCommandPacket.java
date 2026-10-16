package mod.azure.azurelib.network.packet;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import mod.azure.azurelib.animation.cache.AzIdentifiableItemStackAnimatorCache;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.impl.AzItemAnimatorView;
import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.network.AzByteBuf;
import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.platform.services.AzureLibNetwork;
import mod.azure.azurelib.util.AzItemIds;
import mod.azure.azurelib.util.ResourceLocation;
import mod.azure.azurelib.util.client.ClientUtils;

/**
 * Plays an {@link AzCommand} on the animators of one item stack.
 * <p>
 * Stacks are identified by the UUID AzureLib keeps in StationAPI's stack NBT. Because the server assigns that UUID
 * lazily the first time a command is sent for a stack, the client may not have received the NBT yet when this packet
 * arrives. The packet therefore also carries the holder's entity id and the item id: if no animator is known under the
 * UUID, the client finds the matching stack in the holder's hand or armor, adopts the server's UUID for it (moving any
 * animator it already had), and then dispatches.
 */
public class AzItemStackDispatchCommandPacket extends AbstractPacket {

    private final UUID itemStackId;

    private final AzCommand dispatchCommand;

    private final Optional<AzItemAnimatorView> view;

    private final int holderEntityId;

    private final int itemId;

    public AzItemStackDispatchCommandPacket(
        UUID itemStackId,
        AzCommand dispatchCommand,
        Optional<AzItemAnimatorView> view,
        int holderEntityId,
        int itemId
    ) {
        this.itemStackId = itemStackId;
        this.dispatchCommand = dispatchCommand;
        this.view = view;
        this.holderEntityId = holderEntityId;
        this.itemId = itemId;
    }

    @Override
    public void encode(AzByteBuf buf) {
        buf.writeUniqueId(this.itemStackId);
        AzCommand.ENCODER.accept(buf, this.dispatchCommand);
        AzItemAnimatorView.encode(buf, this.view);
        buf.writeInt(this.holderEntityId);
        buf.writeInt(this.itemId);
    }

    @Override
    public ResourceLocation getPacketID() {
        return AzureLibNetwork.AZ_ITEM_STACK_DISPATCH_COMMAND_SYNC_PACKET_ID;
    }

    public static AzItemStackDispatchCommandPacket receive(AzByteBuf buf) {
        UUID itemStackId = buf.readUniqueId();
        AzCommand dispatchCommand = AzCommand.DECODER.apply(buf);
        Optional<AzItemAnimatorView> view = AzItemAnimatorView.decode(buf);
        int holderEntityId = buf.readInt();
        int itemId = buf.readInt();
        return new AzItemStackDispatchCommandPacket(itemStackId, dispatchCommand, view, holderEntityId, itemId);
    }

    @Override
    public void handle() {
        AzIdentifiableItemStackAnimatorCache cache = AzIdentifiableItemStackAnimatorCache.getInstance();

        if (!cache.contains(this.itemStackId)) {
            adoptServerId();
        }

        cache.dispatchOrQueue(
            this.itemStackId,
            this.view.orElse(null),
            animator -> this.dispatchCommand.actions().forEach(action -> action.handle(AzDispatchSide.SERVER, animator))
        );
    }

    private void adoptServerId() {
        World level = ClientUtils.getLevel();
        Entity holder = level == null ? null : Mc.entityById(level, this.holderEntityId);
        if (!(holder instanceof LivingEntity))
            return;

        ItemStack stack = findStack((LivingEntity) holder);
        if (stack == null)
            return;

        UUID previous = AzItemIds.getOrNull(stack);
        AzItemIds.set(stack, this.itemStackId);
        if (previous != null && !previous.equals(this.itemStackId)) {
            AzIdentifiableItemStackAnimatorCache.getInstance().rekey(previous, this.itemStackId);
        }
    }

    @Nullable
    private ItemStack findStack(LivingEntity holder) {
        ItemStack held = Mc.heldItem(holder);
        if (held != null && Mc.itemId(Mc.item(held)) == this.itemId)
            return held;
        for (int i = 0; i < 4; i++) {
            ItemStack armor = Mc.armor(holder, i);
            if (armor != null && Mc.itemId(Mc.item(armor)) == this.itemId)
                return armor;
        }
        return null;
    }
}
