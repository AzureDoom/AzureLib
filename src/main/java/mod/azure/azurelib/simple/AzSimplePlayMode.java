package mod.azure.azurelib.simple;

import mod.azure.azurelib.network.AzByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

public enum AzSimplePlayMode {

    PLAY,
    LOOP,
    STOP;

    private static final AzSimplePlayMode[] VALUES = values();

    public static final Function<AzByteBuf, AzSimplePlayMode> DECODER = buf -> {
        int id = buf.readUnsignedByte();
        return id < VALUES.length ? VALUES[id] : STOP;
    };

    public static final BiConsumer<AzByteBuf, AzSimplePlayMode> ENCODER = (buf, mode) -> buf.writeByte(
        (mode == null ? STOP : mode).ordinal()
    );

    public static AzSimplePlayMode decode(AzByteBuf buf) {
        return DECODER.apply(buf);
    }

    public static void encode(AzByteBuf buf, AzSimplePlayMode mode) {
        ENCODER.accept(buf, mode);
    }
}
