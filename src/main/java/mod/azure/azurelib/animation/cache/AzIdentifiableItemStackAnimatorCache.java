package mod.azure.azurelib.animation.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.item.ItemStack;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import mod.azure.azurelib.animation.impl.AzItemAnimator;
import mod.azure.azurelib.animation.impl.AzItemAnimatorView;

/**
 * Singleton cache of {@link AzItemAnimator}s keyed by an {@link ItemStack}'s id, as given by
 * {@link AzItemIdentity#resolve(ItemStack)}: the stack's persistent Az ID, or a transient per-instance id for a stack
 * that hasn't been given one yet. Animators are stored per {@link AzItemAnimatorView}, so a single stack can have a
 * separate first-person animator alongside its default one. <br>
 * Entries are strongly keyed and expire once an animator has gone unused for 300s. They can't be weakly keyed: the UUID
 * read from a stack's NBT is a new instance on every read and nothing else holds it, so weak keys let the GC discard
 * animators while the stack is still being rendered. <br>
 * Commands dispatched before a stack's animator exists are queued briefly and replayed once it is registered.
 */
public class AzIdentifiableItemStackAnimatorCache {

    private static final AzIdentifiableItemStackAnimatorCache INSTANCE = new AzIdentifiableItemStackAnimatorCache();

    private static final Map<AzItemAnimatorView, Cache<UUID, AzItemAnimator>> ANIMATORS_BY_VIEW =
        new EnumMap<>(AzItemAnimatorView.class);

    private static final Cache<UUID, Map<AzItemAnimatorView, List<Consumer<AzItemAnimator>>>> PENDING =
        CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build();

    static {
        for (AzItemAnimatorView view : AzItemAnimatorView.values()) {
            ANIMATORS_BY_VIEW.put(
                view,
                CacheBuilder.newBuilder().expireAfterAccess(300, TimeUnit.SECONDS).build()
            );
        }
    }

    public static AzIdentifiableItemStackAnimatorCache getInstance() {
        return INSTANCE;
    }

    private AzIdentifiableItemStackAnimatorCache() {}

    /**
     * Registers the {@link AzItemAnimatorView#DEFAULT default} animator for the given stack.
     */
    public void add(ItemStack itemStack, AzItemAnimator animator) {
        add(itemStack, AzItemAnimatorView.DEFAULT, animator);
    }

    /**
     * Registers an animator for the given stack and view, keyed by {@link AzItemIdentity#resolve(ItemStack)}. If one is
     * already registered it is kept, and the new one is ignored. Never writes to the stack.
     */
    public void add(@Nullable ItemStack itemStack, AzItemAnimatorView view, @Nullable AzItemAnimator animator) {
        if (itemStack == null || animator == null) {
            return;
        }

        add(AzItemIdentity.resolve(itemStack), view, animator);
    }

    /**
     * Registers an animator for {@code view} under {@code uuid}, replaying any commands that were queued for it.
     */
    public void add(@Nullable UUID uuid, AzItemAnimatorView view, @Nullable AzItemAnimator animator) {
        if (uuid == null || animator == null) {
            return;
        }

        if (ANIMATORS_BY_VIEW.get(view).asMap().putIfAbsent(uuid, animator) == null) {
            flushPending(uuid, view, animator);
        }
    }

    /**
     * @return the {@link AzItemAnimatorView#DEFAULT default} animator for the given stack id, or null.
     */
    public @Nullable AzItemAnimator getOrNull(@Nullable UUID uuid) {
        return getOrNull(uuid, AzItemAnimatorView.DEFAULT);
    }

    public @Nullable AzItemAnimator getOrNull(@Nullable UUID uuid, AzItemAnimatorView view) {
        return uuid == null ? null : ANIMATORS_BY_VIEW.get(view).getIfPresent(uuid);
    }

    /**
     * @return the animator for the given stack and view, or null if it has none yet.
     */
    public @Nullable AzItemAnimator getOrNull(@Nullable ItemStack itemStack, AzItemAnimatorView view) {
        return itemStack == null ? null : getOrNull(AzItemIdentity.resolve(itemStack), view);
    }

    /**
     * Runs {@code action} on the animator for {@code view}, or on every existing animator of the stack when
     * {@code view} is null. Views that have no animator yet are skipped.
     */
    public void forEach(@Nullable UUID uuid, @Nullable AzItemAnimatorView view, Consumer<AzItemAnimator> action) {
        if (uuid == null) {
            return;
        }

        if (view != null) {
            AzItemAnimator animator = getOrNull(uuid, view);

            if (animator != null) {
                action.accept(animator);
            }

            return;
        }

        for (Cache<UUID, AzItemAnimator> animators : ANIMATORS_BY_VIEW.values()) {
            AzItemAnimator animator = animators.getIfPresent(uuid);

            if (animator != null) {
                action.accept(animator);
            }
        }
    }

    /** True if any view has an animator registered under the given UUID. */
    public boolean contains(@Nullable UUID uuid) {
        if (uuid == null) {
            return false;
        }

        for (Cache<UUID, AzItemAnimator> animators : ANIMATORS_BY_VIEW.values()) {
            if (animators.getIfPresent(uuid) != null) {
                return true;
            }
        }

        return false;
    }

    /** Moves every animator registered under {@code from} to {@code to}, in every view. */
    public void rekey(@Nullable UUID from, @Nullable UUID to) {
        if (from == null || to == null || from.equals(to)) {
            return;
        }

        for (Cache<UUID, AzItemAnimator> animators : ANIMATORS_BY_VIEW.values()) {
            AzItemAnimator animator = animators.asMap().remove(from);

            if (animator != null) {
                animators.asMap().putIfAbsent(to, animator);
            }
        }
    }

    /**
     * Runs {@code action} on the stack's animator for {@code view} (or every view when null). For any targeted view
     * without an animator yet, the action is queued and replayed when that animator is registered, provided that
     * happens within 10 seconds.
     */
    public void dispatchOrQueue(
        @Nullable UUID uuid,
        @Nullable AzItemAnimatorView view,
        Consumer<AzItemAnimator> action
    ) {
        if (uuid == null) {
            return;
        }

        AzItemAnimatorView[] targets = view != null ? new AzItemAnimatorView[] { view } : AzItemAnimatorView.values();

        for (AzItemAnimatorView target : targets) {
            AzItemAnimator animator = getOrNull(uuid, target);

            if (animator != null) {
                action.accept(animator);
                continue;
            }

            synchronized (PENDING) {
                PENDING.asMap()
                    .computeIfAbsent(uuid, k -> new EnumMap<>(AzItemAnimatorView.class))
                    .computeIfAbsent(target, k -> new ArrayList<>())
                    .add(action);
            }
        }
    }

    /**
     * Drops every animator and queued command for the given stack id.
     */
    public void remove(@Nullable UUID uuid) {
        if (uuid == null) {
            return;
        }

        for (Cache<UUID, AzItemAnimator> animators : ANIMATORS_BY_VIEW.values()) {
            animators.invalidate(uuid);
        }

        synchronized (PENDING) {
            PENDING.invalidate(uuid);
        }
    }

    /**
     * Drops all animators and queued commands. Call on client disconnect / world unload.
     */
    public void clear() {
        for (Cache<UUID, AzItemAnimator> animators : ANIMATORS_BY_VIEW.values()) {
            animators.invalidateAll();
        }

        synchronized (PENDING) {
            PENDING.invalidateAll();
        }
    }

    private static void flushPending(UUID uuid, AzItemAnimatorView view, AzItemAnimator animator) {
        List<Consumer<AzItemAnimator>> queued;

        synchronized (PENDING) {
            Map<AzItemAnimatorView, List<Consumer<AzItemAnimator>>> byView = PENDING.getIfPresent(uuid);

            if (byView == null) {
                return;
            }

            queued = byView.remove(view);

            if (byView.isEmpty()) {
                PENDING.invalidate(uuid);
            }
        }

        if (queued != null) {
            queued.forEach(action -> action.accept(animator));
        }
    }
}
