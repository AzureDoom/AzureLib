package mod.azure.azurelib.util.math;

import net.minecraft.tileentity.TileEntity;

/**
 * Immutable block position. Minecraft 1.7.10 passes block coordinates as three ints, so AzureLib carries its own small
 * position type for packets and tile entity keys.
 */
public final class BlockPos {

    private static final int NUM_X_BITS = 26;

    private static final int NUM_Z_BITS = NUM_X_BITS;

    private static final int NUM_Y_BITS = 64 - NUM_X_BITS - NUM_Z_BITS;

    private static final int Y_SHIFT = NUM_Z_BITS;

    private static final int X_SHIFT = Y_SHIFT + NUM_Y_BITS;

    private static final long X_MASK = (1L << NUM_X_BITS) - 1L;

    private static final long Y_MASK = (1L << NUM_Y_BITS) - 1L;

    private static final long Z_MASK = (1L << NUM_Z_BITS) - 1L;

    private final int x;

    private final int y;

    private final int z;

    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockPos(double x, double y, double z) {
        this(Mth.floor(x), Mth.floor(y), Mth.floor(z));
    }

    public static BlockPos of(TileEntity tileEntity) {
        return new BlockPos(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    /** Same packing as modern Minecraft's {@code BlockPos#asLong}. */
    public long toLong() {
        return ((long) this.x & X_MASK) << X_SHIFT | ((long) this.y & Y_MASK) << Y_SHIFT | (long) this.z & Z_MASK;
    }

    public static BlockPos fromLong(long serialized) {
        int i = (int) (serialized << 64 - X_SHIFT - NUM_X_BITS >> 64 - NUM_X_BITS);
        int j = (int) (serialized << 64 - Y_SHIFT - NUM_Y_BITS >> 64 - NUM_Y_BITS);
        int k = (int) (serialized << 64 - NUM_Z_BITS >> 64 - NUM_Z_BITS);
        return new BlockPos(i, j, k);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof BlockPos))
            return false;
        BlockPos other = (BlockPos) o;
        return this.x == other.x && this.y == other.y && this.z == other.z;
    }

    @Override
    public int hashCode() {
        return (this.y + this.z * 31) * 31 + this.x;
    }

    @Override
    public String toString() {
        return "BlockPos{x=" + this.x + ", y=" + this.y + ", z=" + this.z + "}";
    }
}
