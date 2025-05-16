package mod.azure.azurelib.network.packet;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;

import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.animation.AzAnimatorAccessor;
import mod.azure.azurelib.rewrite.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.rewrite.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.util.ClientUtils;

public class AzEntityDispatchCommandPacket extends AbstractPacket {

    private final int entityId;

    private final AzCommand dispatchCommand;

    public AzEntityDispatchCommandPacket(
        int entityId,
        AzCommand dispatchCommand
    ) {
        this.entityId = entityId;
        this.dispatchCommand = dispatchCommand;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeInt(this.entityId);
        AzCommand.ENCODER.accept(buf, this.dispatchCommand);
    }

    public static AzEntityDispatchCommandPacket receive(PacketBuffer buf) {
        int entityId = buf.readInt(); // Decode integer entity ID
        AzCommand dispatchCommand = AzCommand.DECODER.apply(buf); // Decode AzCommand
        return new AzEntityDispatchCommandPacket(entityId, dispatchCommand); // Create and return the packet instance
    }

    public void handle() {
        Entity entity = ClientUtils.getLevel().getEntity(this.entityId);

        if (entity == null) {
            return;
        }

        AzAnimator<Entity> animator = AzAnimatorAccessor.getOrNull(entity);

        if (animator != null) {
            dispatchCommand.actions().forEach(action -> action.handle(AzDispatchSide.SERVER, animator));
        }
    }
}
