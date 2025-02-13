package mod.azure.azurelib.rewrite.animation.dispatch;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * This enum represents the dispatch side for animation commands, which can either be client-side or server-side. It is
 * used as part of the AzureLib animation system for identifying where an animation command originates from or should be
 * executed. <br>
 * Each enum constant has an associated unique identifier for easy lookup and transmission across the network. This
 * mapping is also used within codecs for serialization and deserialization purposes.
 */
public enum AzDispatchSide implements StringRepresentable {

    CLIENT(0),
    SERVER(1);

    private static final Map<Integer, AzDispatchSide> ID_TO_ENUM_MAP = new Int2ObjectArrayMap<>();

    static {
        // Populate the map for quick lookup
        for (AzDispatchSide side : values()) {
            ID_TO_ENUM_MAP.put(side.id, side);
        }
    }

    private final int id;

    AzDispatchSide(int id) {
        this.id = id;
    }

    public void encode(@NotNull FriendlyByteBuf buf, @NotNull AzDispatchSide val) {
        buf.writeByte(val.id);
    }

    public AzDispatchSide decode(@NotNull FriendlyByteBuf buf) {
        int id = buf.readByte();
        AzDispatchSide side = ID_TO_ENUM_MAP.get(id);
        if (side == null) {
            throw new IllegalArgumentException("Invalid AzDispatchSide ID: " + id);
        }
        return side;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name();
    }
}
