package mod.azure.azurelib.network;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import mod.azure.azurelib.util.math.BlockPos;

/**
 * Packet buffer used by AzureLib's packets and action codecs. Minecraft 1.7.10's {@code AzByteBuf} lacks most of the
 * helpers AzureLib uses (UUIDs, block positions, var-int strings without checked exceptions), so this small wrapper
 * provides them with the same names as later versions.
 */
public final class AzByteBuf {

    private final ByteBuf buf;

    public AzByteBuf(ByteBuf buf) {
        this.buf = buf;
    }

    public ByteBuf unwrap() {
        return this.buf;
    }

    public byte readByte() {
        return this.buf.readByte();
    }

    public short readUnsignedByte() {
        return this.buf.readUnsignedByte();
    }

    public AzByteBuf writeByte(int value) {
        this.buf.writeByte(value);
        return this;
    }

    public boolean readBoolean() {
        return this.buf.readBoolean();
    }

    public AzByteBuf writeBoolean(boolean value) {
        this.buf.writeBoolean(value);
        return this;
    }

    public short readShort() {
        return this.buf.readShort();
    }

    public AzByteBuf writeShort(int value) {
        this.buf.writeShort(value);
        return this;
    }

    public int readInt() {
        return this.buf.readInt();
    }

    public AzByteBuf writeInt(int value) {
        this.buf.writeInt(value);
        return this;
    }

    public long readLong() {
        return this.buf.readLong();
    }

    public AzByteBuf writeLong(long value) {
        this.buf.writeLong(value);
        return this;
    }

    public float readFloat() {
        return this.buf.readFloat();
    }

    public AzByteBuf writeFloat(float value) {
        this.buf.writeFloat(value);
        return this;
    }

    public double readDouble() {
        return this.buf.readDouble();
    }

    public AzByteBuf writeDouble(double value) {
        this.buf.writeDouble(value);
        return this;
    }

    public int readVarInt() {
        int value = 0;
        int size = 0;
        byte b;

        do {
            b = this.buf.readByte();
            value |= (b & 0x7F) << size++ * 7;

            if (size > 5) {
                throw new IllegalStateException("VarInt too big");
            }
        } while ((b & 0x80) == 0x80);

        return value;
    }

    public AzByteBuf writeVarInt(int value) {
        while ((value & -128) != 0) {
            this.buf.writeByte(value & 0x7F | 0x80);
            value >>>= 7;
        }

        this.buf.writeByte(value);
        return this;
    }

    public String readString(int maxLength) {
        int length = readVarInt();

        if (length > maxLength * 4 || length < 0) {
            throw new IllegalStateException("Invalid string length " + length);
        }

        byte[] bytes = new byte[length];
        this.buf.readBytes(bytes);
        String value = new String(bytes, StandardCharsets.UTF_8);

        if (value.length() > maxLength) {
            throw new IllegalStateException("String longer than " + maxLength);
        }

        return value;
    }

    public AzByteBuf writeString(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt(bytes.length);
        this.buf.writeBytes(bytes);
        return this;
    }

    public UUID readUniqueId() {
        return new UUID(this.buf.readLong(), this.buf.readLong());
    }

    public AzByteBuf writeUniqueId(UUID uuid) {
        this.buf.writeLong(uuid.getMostSignificantBits());
        this.buf.writeLong(uuid.getLeastSignificantBits());
        return this;
    }

    public BlockPos readBlockPos() {
        return BlockPos.fromLong(this.buf.readLong());
    }

    public AzByteBuf writeBlockPos(BlockPos pos) {
        this.buf.writeLong(pos.toLong());
        return this;
    }

    public <T extends Enum<T>> T readEnumValue(Class<T> enumClass) {
        return enumClass.getEnumConstants()[readVarInt()];
    }

    public AzByteBuf writeEnumValue(Enum<?> value) {
        return writeVarInt(value.ordinal());
    }
}
