package mod.azure.azurelib.common.animation.cache;

import com.google.common.collect.MapMaker;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;

import mod.azure.azurelib.AzureLib;

/**
 * The single place AzureLib reads, creates and clears the identity of an {@link ItemStack}.
 * <p>
 * Stacks are never given an id when they are constructed. A stack receives its persistent id (the
 * {@link AzureLib#AZ_ID} component) through {@link #getOrCreate(ItemStack)} on the logical server, either when it first
 * ticks in an entity's inventory or, for stacks that never do, the first time an animation command is dispatched for
 * it. Assigning it while the stack sits in an inventory means the id, and the slot sync that carries it, arrives before
 * the item is first used rather than in the middle of that use. Creative tabs, recipe outputs, JEI/EMI's ingredient
 * lists and every other freshly built stack keep an empty component patch.
 * <p>
 * Rendering never writes components. {@link #resolve(ItemStack)} returns the persistent id when the stack has one and
 * otherwise a transient id tied to that stack instance, so a stack without an id still keeps one stable animator for as
 * long as the same instance is rendered.
 */
public final class AzItemIdentity {

    private static final UUID EMPTY_STACK_ID = new UUID(0L, 0L);

    private static final ConcurrentMap<ItemStack, UUID> TRANSIENT = new MapMaker().weakKeys().makeMap();

    private AzItemIdentity() {}

    /**
     * @return true when the stack carries a persistent AzureLib id component.
     */
    public static boolean hasPersistentId(@Nullable ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.has(AzureLib.AZ_ID.get());
    }

    /**
     * Reads the stack's persistent id without creating one.
     *
     * @return the persistent id, or null when the stack has none.
     */
    public static @Nullable UUID peek(@Nullable ItemStack stack) {
        return stack == null || stack.isEmpty() ? null : stack.get(AzureLib.AZ_ID.get());
    }

    /**
     * Returns the stack's persistent id, assigning one first if the stack has none and its item is registered in
     * {@link AzIdentityRegistry}. <br>
     * <b>Logical server only.</b> This writes a component, so call it on the server with the stack that actually lives
     * in the inventory (not a copy): the normal slot sync then carries the id to clients. Calling it on the client
     * gives the client a different id from the server's.
     *
     * @return the persistent id, or null for empty stacks and items without a registered identity.
     */
    public static @Nullable UUID getOrCreate(@Nullable ItemStack stack) {
        var existing = peek(stack);

        if (existing != null) {
            return existing;
        }

        if (stack == null || stack.isEmpty() || !AzIdentityRegistry.hasIdentity(stack.getItem())) {
            return null;
        }

        var id = UUID.randomUUID();
        stack.set(AzureLib.AZ_ID.get(), id);
        TRANSIENT.remove(stack);
        return id;
    }

    /**
     * The id to key this stack's animators by. Never writes components and never returns null.
     * <p>
     * Persistent ids are returned as-is. Stacks without one get a transient id that is generated once per stack
     * instance and never persisted or sent to the server.
     */
    public static UUID resolve(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return EMPTY_STACK_ID;
        }

        var persistent = stack.get(AzureLib.AZ_ID.get());

        if (persistent != null) {
            return persistent;
        }

        var cached = TRANSIENT.get(stack);

        if (cached != null) {
            return cached;
        }

        var random = ThreadLocalRandom.current();
        var fresh = new UUID(random.nextLong(), random.nextLong());
        var raced = TRANSIENT.putIfAbsent(stack, fresh);
        return raced != null ? raced : fresh;
    }

    /**
     * Removes the stack's persistent id, so its component patch is equal to a freshly created stack's again. The stack
     * gets a new id the next time one is needed.
     */
    public static void clear(@Nullable ItemStack stack) {
        if (!hasPersistentId(stack)) {
            return;
        }

        stack.remove(AzureLib.AZ_ID.get());
        TRANSIENT.remove(stack);
    }

    /**
     * @return true when the two stacks are the same item and count and differ only in the AzureLib id (including one
     *         having an id and the other not).
     */
    public static boolean differsOnlyByIdentity(@Nullable ItemStack a, @Nullable ItemStack b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
            return false;
        }

        if (
            a.getItem() != b.getItem() || a.getCount() != b.getCount() || !AzIdentityRegistry.hasIdentity(a.getItem())
        ) {
            return false;
        }

        if (Objects.equals(peek(a), peek(b))) {
            return false;
        }

        var strippedA = a.copy();
        var strippedB = b.copy();
        clear(strippedA);
        clear(strippedB);
        return ItemStack.matches(strippedA, strippedB);
    }
}
