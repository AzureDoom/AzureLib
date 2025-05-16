/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.animatable.GeoReplacedEntity;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.util.ClientUtils;
import mod.azure.azurelib.util.RenderUtils;

/**
 * Packet for syncing user-definable animations that can be triggered from the server for
 * {@link net.minecraft.world.entity.Entity Entities}
 */
@Deprecated()
public class EntityAnimTriggerPacket<D> {

    private final int entityId;

    private final boolean isReplacedEntity;

    private final String controllerName;

    private final String animName;

    public EntityAnimTriggerPacket(int entityId, @Nullable String controllerName, String animName) {
        this(entityId, false, controllerName, animName);
    }

    public EntityAnimTriggerPacket(
        int entityId,
        boolean isReplacedEntity,
        @Nullable String controllerName,
        String animName
    ) {
        this.entityId = entityId;
        this.isReplacedEntity = isReplacedEntity;
        this.controllerName = controllerName == null ? "" : controllerName;
        this.animName = animName;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.entityId);
        buffer.writeBoolean(this.isReplacedEntity);
        buffer.writeUtf(this.controllerName);
        buffer.writeUtf(this.animName);
    }

    public static <D> EntityAnimTriggerPacket<D> decode(FriendlyByteBuf buffer) {
        return new EntityAnimTriggerPacket<>(
            buffer.readVarInt(),
            buffer.readBoolean(),
            buffer.readUtf(),
            buffer.readUtf()
        );
    }

    public void receivePacket(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context handler = context.get();

        handler.enqueueWork(() -> {
            Entity entity = ClientUtils.getLevel().getEntity(this.entityId);

            if (entity == null)
                return;

            if (this.isReplacedEntity) {
                GeoAnimatable animatable = RenderUtils.getReplacedAnimatable(entity.getType());

                if (animatable instanceof GeoReplacedEntity replacedEntity)
                    replacedEntity.triggerAnim(
                        entity,
                        this.controllerName.isEmpty() ? null : this.controllerName,
                        this.animName
                    );
            } else if (entity instanceof GeoEntity geoEntity) {
                geoEntity.triggerAnim(this.controllerName.isEmpty() ? null : this.controllerName, this.animName);
            }
        });
        handler.setPacketHandled(true);
    }
}
