package mod.azure.azurelib.neoforge.network;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.common.AzureLibException;
import mod.azure.azurelib.common.internal.common.config.ConfigHolder;
import mod.azure.azurelib.common.internal.common.config.adapter.TypeAdapter;
import mod.azure.azurelib.common.internal.common.config.value.ConfigValue;
import mod.azure.azurelib.common.internal.common.network.AbstractPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.Map;

public class S2C_NeoSendConfigData extends AbstractPacket implements IPacket<S2C_NeoSendConfigData> {

    private final String config;

    public static final Marker MARKER = MarkerManager.getMarker("Network");

    S2C_NeoSendConfigData() {
        this.config = null;
    }

    public S2C_NeoSendConfigData(String config) {
        this.config = config;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.config);
        ConfigHolder.getConfig(this.config).ifPresent(data -> {
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
    }

    public static S2C_NeoSendConfigData receive(FriendlyByteBuf buf) {
        return new S2C_NeoSendConfigData();
    }

    @Override
    public void handle() {

    }

    @Override
    public ResourceLocation id() {
        return null;
    }

    @Override
    public S2C_NeoSendConfigData decode(FriendlyByteBuf buffer) {
        String config = buffer.readUtf();
        int i = buffer.readInt();
        ConfigHolder.getConfig(config).ifPresent(data -> {
            Map<String, ConfigValue<?>> serialized = data.getNetworkSerializedFields();
            for (int j = 0; j < i; j++) {
                String fieldId = buffer.readUtf();
                ConfigValue<?> value = serialized.get(fieldId);
                if (value == null) {
                    AzureLib.LOGGER.fatal(MARKER, "Received unknown config value " + fieldId);
                    throw new AzureLibException("Unknown config field: " + fieldId);
                }
                setValue(value, buffer);
            }
        });
        return new S2C_NeoSendConfigData(config);
    }

    @SuppressWarnings("unchecked")
    private <V> void setValue(ConfigValue<V> value, FriendlyByteBuf buffer) {
        TypeAdapter adapter = value.getAdapter();
        V v = (V) adapter.decodeFromBuffer(value, buffer);
        value.set(v);
    }
}
