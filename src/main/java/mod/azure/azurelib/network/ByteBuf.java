package mod.azure.azurelib.network;

import java.util.Arrays;

/**
 * Minimal big-endian, growable byte buffer with independent reader and writer indices, implementing the subset of
 * Netty's {@code ByteBuf} API that {@link AzByteBuf} uses.
 */
public final class ByteBuf {

    private byte[] data;

    private int readerIndex;

    private int writerIndex;

    public ByteBuf() {
        this(64);
    }

    public ByteBuf(int initialCapacity) {
        this.data = new byte[Math.max(16, initialCapacity)];
    }

    /** Wraps existing bytes for reading. */
    public static ByteBuf wrap(byte[] bytes) {
        ByteBuf buf = new ByteBuf(0);
        buf.data = bytes;
        buf.writerIndex = bytes.length;
        return buf;
    }

    /** A copy of the written, not yet read bytes. */
    public byte[] toByteArray() {
        return Arrays.copyOfRange(this.data, this.readerIndex, this.writerIndex);
    }

    public int readableBytes() {
        return this.writerIndex - this.readerIndex;
    }

    private void ensureWritable(int bytes) {
        int needed = this.writerIndex + bytes;
        if (needed > this.data.length) {
            this.data = Arrays.copyOf(this.data, Math.max(needed, this.data.length * 2));
        }
    }

    private void ensureReadable(int bytes) {
        if (this.readerIndex + bytes > this.writerIndex) {
            throw new IndexOutOfBoundsException(
                "readerIndex(" + this.readerIndex + ") + length(" + bytes + ") exceeds writerIndex(" + this.writerIndex
                    + ")"
            );
        }
    }

    public byte readByte() {
        ensureReadable(1);
        return this.data[this.readerIndex++];
    }

    public short readUnsignedByte() {
        return (short) (readByte() & 0xFF);
    }

    public void writeByte(int value) {
        ensureWritable(1);
        this.data[this.writerIndex++] = (byte) value;
    }

    public boolean readBoolean() {
        return readByte() != 0;
    }

    public void writeBoolean(boolean value) {
        writeByte(value ? 1 : 0);
    }

    public short readShort() {
        return (short) ((readByte() & 0xFF) << 8 | readByte() & 0xFF);
    }

    public void writeShort(int value) {
        writeByte(value >>> 8);
        writeByte(value);
    }

    public int readInt() {
        return (readByte() & 0xFF) << 24 | (readByte() & 0xFF) << 16 | (readByte() & 0xFF) << 8 | readByte() & 0xFF;
    }

    public void writeInt(int value) {
        writeByte(value >>> 24);
        writeByte(value >>> 16);
        writeByte(value >>> 8);
        writeByte(value);
    }

    public long readLong() {
        return (long) readInt() << 32 | readInt() & 0xFFFFFFFFL;
    }

    public void writeLong(long value) {
        writeInt((int) (value >>> 32));
        writeInt((int) value);
    }

    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public void writeFloat(float value) {
        writeInt(Float.floatToIntBits(value));
    }

    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    public void writeDouble(double value) {
        writeLong(Double.doubleToLongBits(value));
    }

    public void readBytes(byte[] destination) {
        ensureReadable(destination.length);
        System.arraycopy(this.data, this.readerIndex, destination, 0, destination.length);
        this.readerIndex += destination.length;
    }

    public void writeBytes(byte[] source) {
        ensureWritable(source.length);
        System.arraycopy(source, 0, this.data, this.writerIndex, source.length);
        this.writerIndex += source.length;
    }
}
