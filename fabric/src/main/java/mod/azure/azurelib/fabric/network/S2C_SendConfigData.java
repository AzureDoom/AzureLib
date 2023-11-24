package mod.azure.azurelib.fabric.network;

import java.util.Map;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.common.AzureLibException;
import mod.azure.azurelib.common.internal.common.config.ConfigHolder;
import mod.azure.azurelib.common.internal.common.config.adapter.TypeAdapter;
import mod.azure.azurelib.common.internal.common.config.value.ConfigValue;
import mod.azure.azurelib.fabric.network.api.IClientPacket;
import mod.azure.azurelib.common.internal.common.network.api.IPacketDecoder;
import mod.azure.azurelib.common.internal.common.network.api.IPacketEncoder;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class S2C_SendConfigData implements IClientPacket<S2C_SendConfigData.ConfigData> {

    public static final ResourceLocation IDENTIFIER = AzureLib.modResource("s2c_send_config_data");

    private final ConfigData config;

    S2C_SendConfigData() {
        this.config = null;
    }

    public S2C_SendConfigData(String config) {
        this.config = new ConfigData(config);
    }

    @Override
    public ResourceLocation getPacketId() {
        return IDENTIFIER;
    }

    @Override
    public ConfigData getPacketData() {
        return config;
    }

    @Override
    public IPacketEncoder<ConfigData> getEncoder() {
        return (configData, buffer) -> {
            buffer.writeUtf(configData.configId);
            ConfigHolder.getConfig(configData.configId).ifPresent(data -> {
                Map<String, ConfigValue<?>> serialized = data.getNetworkSerializedFields();
                buffer.writeInt(serialized.size());
                for (Map.Entry<String, ConfigValue<?>> entry : serialized.entrySet()) {
                    String id = entry.getKey();
                    ConfigValue<?> value = entry.getValue();
                    TypeAdapter adapter = value.getAdapter();
                    buffer.writeUtf(id);
                    adapter.encodeToBuffer(value, buffer);
                }
            });
        };
    }

    @Override
    public IPacketDecoder<ConfigData> getDecoder() {
        return buffer -> {
            String config = buffer.readUtf();
            int i = buffer.readInt();
            ConfigHolder.getConfig(config).ifPresent(data -> {
                Map<String, ConfigValue<?>> serialized = data.getNetworkSerializedFields();
                for (int j = 0; j < i; j++) {
                    String fieldId = buffer.readUtf();
                    ConfigValue<?> value = serialized.get(fieldId);
                    if (value == null) {
                    	AzureLib.LOGGER.fatal(Networking.MARKER, "Received unknown config value " + fieldId);
                        throw new AzureLibException("Unknown config field: " + fieldId);
                    }
                    setValue(value, buffer);
                }
            });
            return new ConfigData(config);
        };
    }

    @Override
    public void handleClientsidePacket(Minecraft client, ClientPacketListener listener, ConfigData packetData, PacketSender dispatcher) {
    }

    private <V> void setValue(ConfigValue<V> value, FriendlyByteBuf buffer) {
        TypeAdapter adapter = value.getAdapter();
        V v = (V) adapter.decodeFromBuffer(value, buffer);
        value.set(v);
    }

    static final class ConfigData {

        private final String configId;

        public ConfigData(String configId) {
            this.configId = configId;
        }
    }
}
