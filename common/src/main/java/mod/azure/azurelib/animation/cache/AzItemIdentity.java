package mod.azure.azurelib.animation.cache;

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
 * {@link AzureLib#ITEM_UUID_TAG} UUID in its NBT) through {@link #getOrCreate(ItemStack)} on the logical server, either
 * when it first ticks in an entity's inventory or, for stacks that never do, the first time an animation command is
 * dispatched for it. Assigning it while the stack sits in an inventory means the id, and the slot sync that carries it,
 * arrives before the item is first used rather than in the middle of that use. Creative tabs, recipe outputs, JEI's
 * ingredient list and every other freshly built stack stay NBT-free.
 * <p>
 * Rendering never writes NBT. {@link #resolve(ItemStack)} returns the persistent id when the stack has one and
 * otherwise a transient id tied to that stack instance, so a stack without an id still keeps one stable animator for as
 * long as the same instance is rendered.
 * <p>
 * Since 1.16 a UUID is stored as a single four-int array tag rather than the {@code Most}/{@code Least} long pair used
 * on 1.12.2, so the id is always read and removed through the {@code ITEM_UUID_TAG} key itself.
 */
public final class AzItemIdentity {

    private static final UUID EMPTY_STACK_ID = new UUID(0L, 0L);

    private static final ConcurrentMap<ItemStack, Resolved> RESOLVED = new MapMaker().weakKeys().makeMap();

    private AzItemIdentity() {}

    /**
     * @return true when the stack carries a persistent AzureLib id in its NBT.
     */
    public static boolean hasPersistentId(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        var tag = stack.getTag();
        return tag != null && tag.hasUUID(AzureLib.ITEM_UUID_TAG);
    }

    /**
     * Reads the stack's persistent id without creating one.
     *
     * @return the persistent id, or null when the stack has none.
     */
    public static @Nullable UUID peek(@Nullable ItemStack stack) {
        return hasPersistentId(stack) ? stack.getTag().getUUID(AzureLib.ITEM_UUID_TAG) : null;
    }

    /**
     * Returns the stack's persistent id, assigning one first if the stack has none and its item is registered in
     * {@link AzIdentityRegistry}. <br>
     * <b>Logical server only.</b> This writes NBT, so call it on the server with the stack that actually lives in the
     * inventory (not a copy): the normal slot sync then carries the id to clients. Calling it on the client gives the
     * client a different id from the server's.
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
        stack.getOrCreateTag().putUUID(AzureLib.ITEM_UUID_TAG, id);
        RESOLVED.remove(stack);
        return id;
    }

    /**
     * The id to key this stack's animators by. Never writes NBT and never returns null.
     * <p>
     * Persistent ids are returned as-is (cached per stack instance, so steady-state rendering doesn't allocate). Stacks
     * without one get a transient id that is generated once per stack instance and never persisted or sent to the
     * server.
     */
    public static UUID resolve(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return EMPTY_STACK_ID;
        }

        var tag = stack.getTag();
        var cached = RESOLVED.get(stack);

        if (tag != null && tag.hasUUID(AzureLib.ITEM_UUID_TAG)) {
            int[] bits = tag.getIntArray(AzureLib.ITEM_UUID_TAG);

            if (cached != null && cached.persistent && cached.matches(bits)) {
                return cached.id;
            }

            var id = fromInts(bits);
            RESOLVED.put(stack, new Resolved(id, true));
            return id;
        }

        if (cached != null && !cached.persistent) {
            return cached.id;
        }

        var random = ThreadLocalRandom.current();
        var fresh = new Resolved(new UUID(random.nextLong(), random.nextLong()), false);

        if (cached == null) {
            var raced = RESOLVED.putIfAbsent(stack, fresh);
            return raced != null && !raced.persistent ? raced.id : fresh.id;
        }

        RESOLVED.put(stack, fresh);
        return fresh.id;
    }

    /**
     * Removes the stack's persistent id, dropping the tag entirely when nothing else is left in it so the stack is
     * equal to a freshly created one again. The stack gets a new id the next time one is needed.
     */
    public static void clear(@Nullable ItemStack stack) {
        if (!hasPersistentId(stack)) {
            return;
        }

        var tag = stack.getTag();
        tag.remove(AzureLib.ITEM_UUID_TAG);

        if (tag.isEmpty()) {
            stack.setTag(null);
        }

        RESOLVED.remove(stack);
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

    private static UUID fromInts(int[] bits) {
        return new UUID(
            (long) bits[0] << 32 | bits[1] & 0xFFFFFFFFL,
            (long) bits[2] << 32 | bits[3] & 0xFFFFFFFFL
        );
    }

    private static final class Resolved {

        private final UUID id;

        private final boolean persistent;

        private Resolved(UUID id, boolean persistent) {
            this.id = id;
            this.persistent = persistent;
        }

        private boolean matches(int[] bits) {
            long most = id.getMostSignificantBits();
            long least = id.getLeastSignificantBits();
            return (int) (most >> 32) == bits[0] && (int) most == bits[1]
                && (int) (least >> 32) == bits[2] && (int) least == bits[3];
        }
    }
}
