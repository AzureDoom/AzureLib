package mod.azure.azurelib.render.item;

import net.minecraft.client.renderer.block.model.ItemCameraTransforms;

/**
 * Carries the camera transform type from Forge's {@code handleCameraTransforms} to AzureLib's {@code RenderItem} hook.
 * Only touched on the client thread.
 */
public final class AzItemRenderHooks {

    private static ItemCameraTransforms.TransformType transformType = ItemCameraTransforms.TransformType.NONE;

    private AzItemRenderHooks() {
        throw new UnsupportedOperationException();
    }

    public static void setTransformType(ItemCameraTransforms.TransformType type) {
        transformType = type == null ? ItemCameraTransforms.TransformType.NONE : type;
    }

    /**
     * Returns the most recently recorded transform type and resets it to {@code NONE}, so a render that bypasses
     * Forge's camera transform hook doesn't inherit a stale value.
     */
    public static ItemCameraTransforms.TransformType consumeTransformType() {
        ItemCameraTransforms.TransformType type = transformType;
        transformType = ItemCameraTransforms.TransformType.NONE;
        return type;
    }
}
