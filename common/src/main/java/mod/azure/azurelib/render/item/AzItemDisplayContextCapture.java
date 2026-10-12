package mod.azure.azurelib.render.item;

import net.minecraft.world.item.ItemDisplayContext;

/**
 * Holds the {@link ItemDisplayContext} of the item currently being submitted.
 * <p>
 * Since 26.x, special item renderers ({@link AzItemSpecialRenderer}) are no longer told which display context they are
 * rendering in. The context lives on the item's {@code ItemStackRenderState}, so a mixin records it here for the
 * duration of that render state's submit, where {@link AzItemRenderer#renderSpecial} can read it. Render thread only.
 * </p>
 */
public final class AzItemDisplayContextCapture {

    private static ItemDisplayContext current = ItemDisplayContext.NONE;

    private AzItemDisplayContextCapture() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return the display context of the item being submitted, or {@link ItemDisplayContext#NONE} if unknown
     */
    public static ItemDisplayContext current() {
        return current;
    }

    /**
     * Sets the current display context and returns the previous one, so nested item renders can restore it.
     */
    public static ItemDisplayContext set(ItemDisplayContext context) {
        var previous = current;
        current = context == null ? ItemDisplayContext.NONE : context;
        return previous;
    }
}
