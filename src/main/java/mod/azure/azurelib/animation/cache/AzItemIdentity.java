package mod.azure.azurelib.animation.cache;

import com.google.common.collect.MapMaker;
import net.minecraft.item.ItemStack;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nullable;

import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.util.AzItemIds;

public final class AzItemIdentity {

    private static final ConcurrentMap<ItemStack, Resolved> RESOLVED = new MapMaker().weakKeys().makeMap();

    private AzItemIdentity() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return true when the stack carries a persistent AzureLib id.
     */
    public static boolean hasPersistentId(@Nullable ItemStack stack) {
        return AzItemIds.has(stack);
    }

    /**
     * Reads the stack's persistent id without creating one.
     *
     * @return the persistent id, or null when the stack has none.
     */
    @Nullable
    public static UUID peek(@Nullable ItemStack stack) {
        return AzItemIds.getOrNull(stack);
    }

    /**
     * Returns the stack's persistent id, assigning one first if the stack has none and its item is registered in
     * {@link AzIdentityRegistry}. <br>
     * <b>Logical server only.</b> This writes NBT, so call it on the server with the stack that actually lives in the
     * inventory (not a copy): StationAPI then carries the id to clients. Calling it on the client gives the client a
     * different id from the server's.
     *
     * @return the persistent id, or null for null stacks and items without a registered identity.
     */
    @Nullable
    public static UUID getOrCreate(@Nullable ItemStack stack) {
        if (stack == null) {
            return null;
        }

        UUID existing = AzItemIds.getOrNull(stack);

        if (existing != null) {
            return existing;
        }

        if (!AzIdentityRegistry.hasIdentity(Mc.item(stack))) {
            return null;
        }

        UUID id = UUID.randomUUID();
        AzItemIds.set(stack, id);
        RESOLVED.remove(stack);
        return id;
    }

    /**
     * The id to key this stack's animators by. Never writes NBT and never returns null for a non-null stack.
     * <p>
     * Persistent ids are returned as-is (cached per stack instance, so steady-state rendering doesn't allocate). Stacks
     * without one get a transient id that is generated once per stack instance and never persisted or sent to the
     * server.
     */
    public static UUID resolve(ItemStack stack) {
        Resolved cached = RESOLVED.get(stack);

        if (AzItemIds.has(stack)) {
            UUID id = AzItemIds.get(Mc.stackNbt(stack));

            if (cached != null && cached.persistent && cached.id.equals(id)) {
                return cached.id;
            }

            RESOLVED.put(stack, new Resolved(id, true));
            return id;
        }

        if (cached != null && !cached.persistent) {
            return cached.id;
        }

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
     * Client side: gives the stack the id the server dispatched to, for stacks whose NBT hasn't been synced (other
     * players' equipment may arrive without it). Unlike {@link #getOrCreate}, the id comes from the server, so client
     * and server still agree.
     *
     * @return the id the stack was resolved to before adopting, so the caller can move its animators across.
     */
    public static UUID adopt(ItemStack stack, UUID serverId) {
        UUID previous = resolve(stack);
        AzItemIds.set(stack, serverId);
        RESOLVED.put(stack, new Resolved(serverId, true));
        return previous;
    }

    private static final class Resolved {

        private final UUID id;

        private final boolean persistent;

        private Resolved(UUID id, boolean persistent) {
            this.id = id;
            this.persistent = persistent;
        }
    }
}
