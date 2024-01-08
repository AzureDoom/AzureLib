package mod.azure.azurelib.common.internal.common.network.packet;

import mod.azure.azurelib.common.internal.common.core.animatable.GeoAnimatable;
import mod.azure.azurelib.common.internal.common.core.animation.AnimatableManager;
import mod.azure.azurelib.common.internal.common.network.AbstractPacket;
import mod.azure.azurelib.common.platform.services.AzureLibNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Packet for syncing user-definable animations that can be triggered from the
 * server
 */
public class AnimTriggerPacket extends AbstractPacket {
    private final String syncableId;
    private final long instanceId;
    private final String controllerName;
    private final String animName;

    public AnimTriggerPacket(String syncableId, long instanceId, @Nullable String controllerName, String animName) {
        this.syncableId = syncableId;
        this.instanceId = instanceId;
        this.controllerName = controllerName == null ? "" : controllerName;
        this.animName = animName;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.syncableId);
        buf.writeVarLong(this.instanceId);
        buf.writeUtf(this.controllerName);
        buf.writeUtf(this.animName);
    }

    @Override
    public ResourceLocation id() {
        return AzureLibNetwork.ANIM_TRIGGER_SYNC_PACKET_ID;
    }

    public static AnimTriggerPacket receive(FriendlyByteBuf buf) {
        return new AnimTriggerPacket(buf.readUtf(), buf.readVarLong(), buf.readUtf(), buf.readUtf());
    }

    @Override
    public void handle() {
        GeoAnimatable animatable = AzureLibNetwork.getSyncedAnimatable(syncableId);

        if (animatable != null) {
            AnimatableManager<?> manager = animatable.getAnimatableInstanceCache().getManagerForId(instanceId);

            manager.tryTriggerAnimation(controllerName, animName);
        }
    }
}
