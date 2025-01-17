/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.internal.common.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import mod.azure.azurelib.common.api.client.helper.ClientUtils;
import mod.azure.azurelib.common.api.common.animatable.GeoEntity;
import mod.azure.azurelib.common.api.common.animatable.GeoReplacedEntity;
import mod.azure.azurelib.common.internal.client.util.RenderUtils;
import mod.azure.azurelib.common.internal.common.network.AbstractPacket;
import mod.azure.azurelib.common.platform.services.AzureLibNetwork;

/**
 * Packet for syncing user-definable animations that can be triggered from the server for
 * {@link net.minecraft.world.entity.Entity Entities}
 *
 * @deprecated
 */
public record EntityAnimTriggerPacket(
    int entityId,
    boolean isReplacedEntity,
    String controllerName,
    String animName
) implements AbstractPacket {

    public static final CustomPacketPayload.Type<EntityAnimTriggerPacket> TYPE = new Type<>(
        AzureLibNetwork.ENTITY_ANIM_TRIGGER_SYNC_PACKET_ID
    );

    public static final StreamCodec<FriendlyByteBuf, EntityAnimTriggerPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        EntityAnimTriggerPacket::entityId,
        ByteBufCodecs.BOOL,
        EntityAnimTriggerPacket::isReplacedEntity,
        ByteBufCodecs.STRING_UTF8,
        EntityAnimTriggerPacket::controllerName,
        ByteBufCodecs.STRING_UTF8,
        EntityAnimTriggerPacket::animName,
        EntityAnimTriggerPacket::new
    );

    public void handle() {
        Entity entity = ClientUtils.getLevel().getEntity(this.entityId);
        if (entity == null)
            return;
        if (!this.isReplacedEntity) {
            if (entity instanceof GeoEntity geoEntity)
                geoEntity.triggerAnim(this.controllerName.isEmpty() ? null : this.controllerName, this.animName);
            return;
        }
        if (RenderUtils.getReplacedAnimatable(entity.getType()) instanceof GeoReplacedEntity replacedEntity)
            replacedEntity.triggerAnim(
                entity,
                this.controllerName.isEmpty() ? null : this.controllerName,
                this.animName
            );
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
