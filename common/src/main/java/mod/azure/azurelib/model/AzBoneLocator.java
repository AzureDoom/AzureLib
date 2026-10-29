package mod.azure.azurelib.model;

/**
 * A locator authored on a bone in Blockbench, baked relative to that bone: its offset from the bone's pivot in
 * Blockbench pixels and its rotation in Blockbench degrees. Locators move with their bone, so they make natural
 * attachment points (see {@code AzBoneSocket#locator}).
 */
public final class AzBoneLocator {

    private final String name;

    private final float offsetX;

    private final float offsetY;

    private final float offsetZ;

    private final float rotationX;

    private final float rotationY;

    private final float rotationZ;

    public AzBoneLocator(
        String name,
        float offsetX,
        float offsetY,
        float offsetZ,
        float rotationX,
        float rotationY,
        float rotationZ
    ) {
        this.name = name;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.rotationZ = rotationZ;
    }

    public String name() {
        return name;
    }

    public float offsetX() {
        return offsetX;
    }

    public float offsetY() {
        return offsetY;
    }

    public float offsetZ() {
        return offsetZ;
    }

    public float rotationX() {
        return rotationX;
    }

    public float rotationY() {
        return rotationY;
    }

    public float rotationZ() {
        return rotationZ;
    }
}
