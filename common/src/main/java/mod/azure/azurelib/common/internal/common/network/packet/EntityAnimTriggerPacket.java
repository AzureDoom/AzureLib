package mod.azure.azurelib.common.internal.common.network.packet;

import mod.azure.azurelib.common.api.common.animatable.GeoEntity;
import mod.azure.azurelib.common.api.common.animatable.GeoReplacedEntity;
import mod.azure.azurelib.common.internal.client.util.RenderUtils;
import mod.azure.azurelib.common.internal.common.core.animatable.GeoAnimatable;
import mod.azure.azurelib.common.internal.common.network.AbstractPacket;
import mod.azure.azurelib.common.platform.services.AzureLibNetwork;
import mod.azure.azurelib.common.api.client.helper.ClientUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Packet for syncing user-definable animations that can be triggered from the
 * server for {@link net.minecraft.world.entity.Entity Entities}
 */
public class EntityAnimTriggerPacket extends AbstractPacket {
    private final int entityId;
    private final boolean isReplacedEntity;

    private final String controllerName;
    private final String animName;

    public EntityAnimTriggerPacket(int entityId, @Nullable String controllerName, String animName) {
        this(entityId, false, controllerName, animName);
    }

    public EntityAnimTriggerPacket(int entityId, boolean isReplacedEntity, @Nullable String controllerName,
                                   String animName) {
        this.entityId = entityId;
        this.isReplacedEntity = isReplacedEntity;
        this.controllerName = controllerName == null ? "" : controllerName;
        this.animName = animName;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entityId);
        buf.writeBoolean(this.isReplacedEntity);

        buf.writeUtf(this.controllerName);
        buf.writeUtf(this.animName);
    }

    @Override
    public ResourceLocation id() {
        return AzureLibNetwork.ENTITY_ANIM_TRIGGER_SYNC_PACKET_ID;
    }

    public static EntityAnimTriggerPacket receive(FriendlyByteBuf buf) {
        return new EntityAnimTriggerPacket(buf.readVarInt(), buf.readBoolean(), buf.readUtf(), buf.readUtf());
    }

    public void handle() {
        Entity entity = ClientUtils.getLevel().getEntity(entityId);
        if (entity == null)
            return;

        if (!isReplacedEntity) {
            if (entity instanceof GeoEntity geoEntity) {
                geoEntity.triggerAnim(controllerName.isEmpty() ? null : controllerName, animName);
            }
            return;
        }

        GeoAnimatable animatable = RenderUtils.getReplacedAnimatable(entity.getType());
        if (animatable instanceof GeoReplacedEntity replacedEntity)
            replacedEntity.triggerAnim(entity, controllerName.isEmpty() ? null : controllerName, animName);
    }
}
