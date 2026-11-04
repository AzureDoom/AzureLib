package mod.azure.azurelib.animation.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.impl.AzItemAnimator;
import mod.azure.azurelib.animation.impl.AzItemAnimatorView;

/**
 * Singleton cache of {@link AzItemAnimator}s keyed by an {@link ItemStack}'s Az ID ({@link AzureLib#ITEM_UUID_TAG}).
 * Animators are stored per {@link AzItemAnimatorView}, so a single stack can have a separate first-person animator
 * alongside its default one. <br>
 * Entries expire after a period without access rather than being weakly keyed: the UUID instance read from a stack's
 * tag is freshly allocated on every read and not retained elsewhere, so weak keys would let the GC discard live
 * animators mid-animation. <br>
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
        for (var view : AzItemAnimatorView.values()) {
            ANIMATORS_BY_VIEW.put(
                view,
                CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build()
            );
        }
    }

    public static AzIdentifiableItemStackAnimatorCache getInstance() {
        return INSTANCE;
    }

    private AzIdentifiableItemStackAnimatorCache() {}

    /**
     * Reads the stack's Az ID without creating or modifying its tag.
     *
     * @return the stack's Az ID, or null if it has none.
     */
    public static @Nullable UUID getAzId(ItemStack itemStack) {
        var tag = itemStack.getTag();
        return tag != null && tag.hasUUID(AzureLib.ITEM_UUID_TAG) ? tag.getUUID(AzureLib.ITEM_UUID_TAG) : null;
    }

    /**
     * Registers the {@link AzItemAnimatorView#DEFAULT default} animator for the given stack.
     */
    public void add(ItemStack itemStack, AzItemAnimator animator) {
        add(itemStack, AzItemAnimatorView.DEFAULT, animator);
    }

    /**
     * Registers an animator for the given stack and view. If one is already registered it is kept, and the new one is
     * ignored. Any commands queued for this stack and view are replayed on the newly registered animator.
     */
    public void add(ItemStack itemStack, AzItemAnimatorView view, AzItemAnimator animator) {
        var uuid = getAzId(itemStack);

        if (uuid == null) {
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
     * @return the animator for the given stack and view, or null if the stack has no Az ID or no animator yet.
     */
    public @Nullable AzItemAnimator getOrNull(ItemStack itemStack, AzItemAnimatorView view) {
        return getOrNull(getAzId(itemStack), view);
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
            var animator = getOrNull(uuid, view);

            if (animator != null) {
                action.accept(animator);
            }

            return;
        }

        for (var animators : ANIMATORS_BY_VIEW.values()) {
            var animator = animators.getIfPresent(uuid);

            if (animator != null) {
                action.accept(animator);
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

        var targets = view != null ? new AzItemAnimatorView[] { view } : AzItemAnimatorView.values();

        for (var target : targets) {
            var animator = getOrNull(uuid, target);

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

        for (var animators : ANIMATORS_BY_VIEW.values()) {
            animators.invalidate(uuid);
        }

        synchronized (PENDING) {
            PENDING.invalidate(uuid);
        }
    }

    /**
     * Drops all animators and queued commands. Call on client disconnect / level unload.
     */
    public void clear() {
        for (var animators : ANIMATORS_BY_VIEW.values()) {
            animators.invalidateAll();
        }

        synchronized (PENDING) {
            PENDING.invalidateAll();
        }
    }

    private static void flushPending(UUID uuid, AzItemAnimatorView view, AzItemAnimator animator) {
        List<Consumer<AzItemAnimator>> queued;

        synchronized (PENDING) {
            var byView = PENDING.getIfPresent(uuid);

            if (byView == null) {
                return;
            }

            queued = byView.remove(view);

            if (byView.isEmpty()) {
                PENDING.invalidate(uuid);
            }
        }

        // Run outside the lock so an action that dispatches again can't deadlock or re-enter mid-mutation.
        if (queued != null) {
            queued.forEach(action -> action.accept(animator));
        }
    }
}
