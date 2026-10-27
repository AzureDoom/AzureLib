package mod.azure.azurelib.animation.cache;

import com.google.common.collect.MapMaker;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nullable;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.util.AzureLibUtil;

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
 * The id is stored as two longs at the top level of the stack's tag, {@code az_idMost} and {@code az_idLeast} (the same
 * layout 1.12.2's {@code NBTTagCompound#setUniqueId} writes). Every read and write goes through this class's private
 * helpers so the layout can't drift between methods.
 * <p>
 * Rendering never writes NBT. {@link #resolve(ItemStack)} returns the persistent id when the stack has one and
 * otherwise a transient id tied to that stack instance, so a stack without an id still keeps one stable animator for as
 * long as the same instance is rendered.
 */
public final class AzItemIdentity {

    private static final String MOST_KEY = AzureLib.ITEM_UUID_TAG + "Most";

    private static final String LEAST_KEY = AzureLib.ITEM_UUID_TAG + "Least";

    /** {@code NBTTagCompound#hasKey(String, int)} wildcard for any numeric tag type. */
    private static final int ANY_NUMERIC = 99;

    private static final UUID EMPTY_STACK_ID = new UUID(0L, 0L);

    /** Weak, identity-keyed: an entry lives exactly as long as its stack instance. */
    private static final ConcurrentMap<ItemStack, Resolved> RESOLVED = new MapMaker().weakKeys().makeMap();

    private AzItemIdentity() {}

    /**
     * @return true when the stack carries a persistent AzureLib id in its NBT.
     */
    public static boolean hasPersistentId(@Nullable ItemStack stack) {
        if (stack == null || AzureLibUtil.isEmpty(stack)) {
            return false;
        }

        return hasId(stack.getTagCompound());
    }

    /**
     * Reads the stack's persistent id without creating one.
     *
     * @return the persistent id, or null when the stack has none.
     */
    public static @Nullable UUID peek(@Nullable ItemStack stack) {
        return hasPersistentId(stack) ? readId(stack.getTagCompound()) : null;
    }

    /**
     * Returns the stack's persistent id, assigning one first if the stack has none and its item is registered in
     * {@link AzIdentityRegistry}. This writes NBT, so call it on the logical server with the stack that actually lives
     * in the inventory (not a copy): the normal slot sync then carries the id to clients.
     *
     * @return the persistent id, or null for empty stacks and items without a registered identity.
     */
    public static @Nullable UUID getOrCreate(@Nullable ItemStack stack) {
        UUID existing = peek(stack);

        if (existing != null) {
            return existing;
        }

        if (stack == null || AzureLibUtil.isEmpty(stack) || !AzIdentityRegistry.hasIdentity(stack.getItem())) {
            return null;
        }

        UUID id = UUID.randomUUID();
        writeId(AzureLibUtil.getOrCreateTag(stack), id);
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
        if (stack == null || AzureLibUtil.isEmpty(stack)) {
            return EMPTY_STACK_ID;
        }

        NBTTagCompound tag = stack.getTagCompound();
        Resolved cached = RESOLVED.get(stack);

        if (hasId(tag)) {
            long most = tag.getLong(MOST_KEY);
            long least = tag.getLong(LEAST_KEY);

            if (cached != null && cached.persistent && cached.matches(most, least)) {
                return cached.id;
            }

            UUID id = new UUID(most, least);
            RESOLVED.put(stack, new Resolved(id, true));
            return id;
        }

        if (cached != null && !cached.persistent) {
            return cached.id;
        }

        // No persistent id (never had one, or it was cleared): give this instance its own transient id. A stale
        // persistent entry is replaced rather than reused, so a cleared clone never shares the original's animator.
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Resolved fresh = new Resolved(new UUID(random.nextLong(), random.nextLong()), false);

        if (cached == null) {
            Resolved raced = RESOLVED.putIfAbsent(stack, fresh);
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

        NBTTagCompound tag = stack.getTagCompound();
        removeId(tag);

        if (tag.hasNoTags()) {
            stack.setTagCompound(null);
        }

        RESOLVED.remove(stack);
    }

    /**
     * @return true when the two stacks are the same item, damage and count and their NBT differs only in the AzureLib
     *         id (including one having an id and the other not).
     */
    public static boolean differsOnlyByIdentity(@Nullable ItemStack a, @Nullable ItemStack b) {
        if (a == null || b == null || AzureLibUtil.isEmpty(a) || AzureLibUtil.isEmpty(b)) {
            return false;
        }

        if (
            a.getItem() != b.getItem() || a.getItemDamage() != b.getItemDamage()
                || a.stackSize != b.stackSize
                || !AzIdentityRegistry.hasIdentity(a.getItem())
        ) {
            return false;
        }

        if (Objects.equals(a.getTagCompound(), b.getTagCompound())) {
            return false;
        }

        return Objects.equals(withoutIdentity(a.getTagCompound()), withoutIdentity(b.getTagCompound()));
    }

    private static @Nullable NBTTagCompound withoutIdentity(@Nullable NBTTagCompound tag) {
        if (tag == null) {
            return null;
        }

        NBTTagCompound copy = (NBTTagCompound) tag.copy();
        removeId(copy);
        return copy.hasNoTags() ? null : copy;
    }

    private static boolean hasId(@Nullable NBTTagCompound tag) {
        return tag != null && tag.hasKey(MOST_KEY, ANY_NUMERIC) && tag.hasKey(LEAST_KEY, ANY_NUMERIC);
    }

    private static UUID readId(NBTTagCompound tag) {
        return new UUID(tag.getLong(MOST_KEY), tag.getLong(LEAST_KEY));
    }

    private static void writeId(NBTTagCompound tag, UUID id) {
        tag.setLong(MOST_KEY, id.getMostSignificantBits());
        tag.setLong(LEAST_KEY, id.getLeastSignificantBits());
    }

    private static void removeId(NBTTagCompound tag) {
        tag.removeTag(MOST_KEY);
        tag.removeTag(LEAST_KEY);
    }

    private static final class Resolved {

        private final UUID id;

        private final boolean persistent;

        private Resolved(UUID id, boolean persistent) {
            this.id = id;
            this.persistent = persistent;
        }

        private boolean matches(long most, long least) {
            return id.getMostSignificantBits() == most && id.getLeastSignificantBits() == least;
        }
    }
}
