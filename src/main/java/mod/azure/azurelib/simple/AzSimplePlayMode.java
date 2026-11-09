package mod.azure.azurelib.simple;

import net.minecraft.network.PacketBuffer;

import java.util.function.BiConsumer;
import java.util.function.Function;

public enum AzSimplePlayMode {

    PLAY,
    LOOP,
    STOP;

    private static final AzSimplePlayMode[] VALUES = values();

    public static final Function<PacketBuffer, AzSimplePlayMode> DECODER = buf -> {
        int id = buf.readUnsignedByte();
        return id < VALUES.length ? VALUES[id] : STOP;
    };

    public static final BiConsumer<PacketBuffer, AzSimplePlayMode> ENCODER = (buf, mode) -> buf.writeByte(
        (mode == null ? STOP : mode).ordinal()
    );

    public static AzSimplePlayMode decode(PacketBuffer buf) {
        return DECODER.apply(buf);
    }

    public static void encode(PacketBuffer buf, AzSimplePlayMode mode) {
        ENCODER.accept(buf, mode);
    }
}
