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
import org.jetbrains.annotations.NotNull;

import mod.azure.azurelib.common.internal.common.network.AbstractPacket;
import mod.azure.azurelib.common.platform.services.AzureLibNetwork;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animation.AnimatableManager;

/**
 * Packet for syncing user-definable animations that can be triggered from the server
 *
 * @deprecated
 */
public record AnimTriggerPacket(
    String syncableId,
    long instanceId,
    String controllerName,
    String animName
) implements AbstractPacket {

    public static final CustomPacketPayload.Type<AnimTriggerPacket> TYPE = new Type<>(
        AzureLibNetwork.ANIM_TRIGGER_SYNC_PACKET_ID
    );

    public static final StreamCodec<FriendlyByteBuf, AnimTriggerPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        AnimTriggerPacket::syncableId,
        ByteBufCodecs.VAR_LONG,
        AnimTriggerPacket::instanceId,
        ByteBufCodecs.STRING_UTF8,
        AnimTriggerPacket::controllerName,
        ByteBufCodecs.STRING_UTF8,
        AnimTriggerPacket::animName,
        AnimTriggerPacket::new
    );

    @Override
    public void handle() {
        GeoAnimatable animatable = AzureLibNetwork.getSyncedAnimatable(syncableId);

        if (animatable != null) {
            AnimatableManager<?> manager = animatable.getAnimatableInstanceCache().getManagerForId(instanceId);

            manager.tryTriggerAnimation(controllerName, animName);
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
