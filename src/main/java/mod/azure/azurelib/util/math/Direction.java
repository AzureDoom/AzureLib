package mod.azure.azurelib.util.math;

/**
 * The six axis directions. AzureLib uses this for cube faces and block facing; it mirrors the parts of modern
 * Minecraft's {@code Direction} that AzureLib needs, which 1.7.10 does not have.
 */
public enum Direction {

    DOWN(0, -1, 0),
    UP(0, 1, 0),
    NORTH(0, 0, -1),
    SOUTH(0, 0, 1),
    WEST(-1, 0, 0),
    EAST(1, 0, 0);

    private final int stepX;

    private final int stepY;

    private final int stepZ;

    Direction(int stepX, int stepY, int stepZ) {
        this.stepX = stepX;
        this.stepY = stepY;
        this.stepZ = stepZ;
    }

    public int getStepX() {
        return this.stepX;
    }

    public int getStepY() {
        return this.stepY;
    }

    public int getStepZ() {
        return this.stepZ;
    }

    public Vector3f step() {
        return new Vector3f(this.stepX, this.stepY, this.stepZ);
    }

    /**
     * Same ordering as 1.7.10's {@code ForgeDirection} / side indices (0 down, 1 up, 2 north, 3 south, 4 west, 5 east).
     */
    public static Direction byIndex(int index) {
        Direction[] values = values();
        return index >= 0 && index < values.length ? values[index] : NORTH;
    }

    /** The horizontal direction an entity with the given yaw is facing. */
    public static Direction fromYaw(double yaw) {
        switch (Mth.floor(yaw / 90.0D + 0.5D) & 3) {
            case 0:
                return SOUTH;
            case 1:
                return WEST;
            case 2:
                return NORTH;
            default:
                return EAST;
        }
    }

    /** 1.12.2-style horizontal index: south 0, west 1, north 2, east 3; -1 for up and down. */
    public int getHorizontalIndex() {
        switch (this) {
            case SOUTH:
                return 0;
            case WEST:
                return 1;
            case NORTH:
                return 2;
            case EAST:
                return 3;
            default:
                return -1;
        }
    }
}
