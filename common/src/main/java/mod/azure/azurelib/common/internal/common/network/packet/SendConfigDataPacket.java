/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
package mod.azure.azurelib.common.internal.common.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.common.AzureLibException;
import mod.azure.azurelib.common.internal.common.config.ConfigHolderRegistry;
import mod.azure.azurelib.common.internal.common.config.adapter.TypeAdapter;
import mod.azure.azurelib.common.internal.common.config.value.ConfigValue;
import mod.azure.azurelib.common.internal.common.network.AbstractPacket;
import mod.azure.azurelib.common.platform.services.AzureLibNetwork;

public record SendConfigDataPacket(String config) implements AbstractPacket {

    public static final Marker MARKER = MarkerManager.getMarker("Network");

    public static final Type<SendConfigDataPacket> TYPE = new Type<>(
        AzureLibNetwork.CONFIG_PACKET_ID
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SendConfigDataPacket> CODEC = StreamCodec.of(
        (buf, packet) -> {
            buf.writeUtf(packet.config);
            ConfigHolderRegistry.getConfig(packet.config).ifPresent(data -> {
                Map<String, ConfigValue<?>> serialized = data.getNetworkSerializedFields();
                buf.writeInt(serialized.size());
                for (Map.Entry<String, ConfigValue<?>> entry : serialized.entrySet()) {
                    String id = entry.getKey();
                    ConfigValue<?> value = entry.getValue();
                    TypeAdapter adapter = value.getAdapter();
                    buf.writeUtf(id);
                    adapter.encodeToBuffer(value, buf);
                }
            });
        },
        buf -> {
            String config = buf.readUtf();
            int i = buf.readInt();
            ConfigHolderRegistry.getConfig(config).ifPresent(data -> {
                Map<String, ConfigValue<?>> serialized = data.getNetworkSerializedFields();
                for (int j = 0; j < i; j++) {
                    String fieldId = buf.readUtf();
                    ConfigValue<?> value = serialized.get(fieldId);
                    if (value == null) {
                        AzureLib.LOGGER.fatal(MARKER, "Received unknown config value " + fieldId);
                        throw new AzureLibException("Unknown config field: " + fieldId);
                    }
                    setValue(value, buf);
                }
            });
            return new SendConfigDataPacket(config);
        }
    );

    @SuppressWarnings("unchecked")
    private static <V> void setValue(ConfigValue<V> value, FriendlyByteBuf buffer) {
        TypeAdapter adapter = value.getAdapter();
        V v = (V) adapter.decodeFromBuffer(value, buffer);
        value.set(v);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void handle() {}
}
