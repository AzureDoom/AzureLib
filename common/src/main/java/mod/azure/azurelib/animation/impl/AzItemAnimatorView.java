package mod.azure.azurelib.animation.impl;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemDisplayContext;

import java.util.Optional;

/**
 * Identifies which of an {@link net.minecraft.world.item.ItemStack ItemStack}'s animators is being used. <br>
 * By default, every item stack has a single animator ({@link #DEFAULT}) that is shared by every
 * {@link ItemDisplayContext}. When a renderer opts in with
 * {@code AzItemRendererConfig.Builder#useSeparateFirstPersonAnimator()}, the first-person hand contexts get their own
 * animator ({@link #FIRST_PERSON}) with its own controllers, bone state and timer, so the first-person view and the
 * third-person view of the same stack can play different animations at the same time. <br>
 * <br>
 * The view being rendered is tracked per thread while an item renders, so anything that looks the stack's animator up
 * through {@link mod.azure.azurelib.animation.AzAnimatorAccessor AzAnimatorAccessor} during that render (render layers,
 * the model provider, custom code in the pipeline) gets the animator for the view being drawn. Outside of rendering the
 * current view is always {@link #DEFAULT}.
 */
public enum AzItemAnimatorView {

    /**
     * The stack's main animator. Used for third person, GUI, ground, item frames, heads and every other context, and
     * for first person too unless the renderer uses a separate first-person animator.
     */
    DEFAULT,

    /**
     * The first-person animator, used for {@link ItemDisplayContext#FIRST_PERSON_LEFT_HAND} and
     * {@link ItemDisplayContext#FIRST_PERSON_RIGHT_HAND} when the renderer uses a separate first-person animator.
     */
    FIRST_PERSON;

    private static final AzItemAnimatorView[] VALUES = values();

    private static final ThreadLocal<AzItemAnimatorView> CURRENT = ThreadLocal.withInitial(() -> DEFAULT);

    public static void encode(FriendlyByteBuf buf, Optional<AzItemAnimatorView> view) {
        buf.writeByte(view.map(Enum::ordinal).orElse(-1));
    }

    public static Optional<AzItemAnimatorView> decode(FriendlyByteBuf buf) {
        int id = buf.readByte();

        return id < 0 || id >= VALUES.length
            ? Optional.empty()
            : Optional.of(VALUES[id]);
    }

    /**
     * Maps a display context to a view, sending the first-person hand contexts to {@link #FIRST_PERSON} and everything
     * else to {@link #DEFAULT}.
     */
    public static AzItemAnimatorView fromDisplayContext(ItemDisplayContext context) {
        return context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
            || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ? FIRST_PERSON : DEFAULT;
    }

    /**
     * @return the view being rendered on this thread, or {@link #DEFAULT} when no item is rendering.
     */
    public static AzItemAnimatorView current() {
        return CURRENT.get();
    }

    /**
     * Makes {@code view} the current view on this thread and returns the previous one, which must be handed back to
     * {@link #restore(AzItemAnimatorView)} once rendering is done (use try/finally).
     */
    public static AzItemAnimatorView push(AzItemAnimatorView view) {
        var previous = CURRENT.get();
        CURRENT.set(view);
        return previous;
    }

    public static void restore(AzItemAnimatorView previous) {
        CURRENT.set(previous);
    }
}
