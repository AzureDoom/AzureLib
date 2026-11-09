package mod.azure.azurelib.simple;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public enum AzSimplePlayMode {

    PLAY,
    LOOP,
    STOP;

    private static final AzSimplePlayMode[] VALUES = values();

    public static final StreamCodec<FriendlyByteBuf, AzSimplePlayMode> CODEC = StreamCodec.of(
        (buf, mode) -> buf.writeByte(mode.ordinal()),
        buf -> {
            int id = buf.readByte();
            return id >= 0 && id < VALUES.length ? VALUES[id] : STOP;
        }
    );
}
