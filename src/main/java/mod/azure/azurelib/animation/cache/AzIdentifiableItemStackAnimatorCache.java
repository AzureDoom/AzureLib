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
import mod.azure.azurelib.util.AzureLibUtil;

/**
 * The AzIdentifiableItemStackAnimatorCache class is a singleton utility for managing a cache of {@link ItemStack}
 * objects, each associated with a unique identifier (UUID). This class provides functionality to register and retrieve
 * item animators that apply to specific {@link ItemStack}s using their respective UUIDs. <br>
 * Animators are stored per {@link AzItemAnimatorView}, so a single stack can have a separate first-person animator
 * alongside its default one.
 * <p>
 * Ids come from {@link AzItemIdentity#resolve(ItemStack)}, so they are either the stack's persistent id or a transient
 * id for a stack that has never been dispatched to.
 * <p>
 * Entries are strongly keyed and expire once an animator has gone unused for {@link #ANIMATOR_IDLE_SECONDS}. They can't
 * be weakly keyed: an animator's context holds its stack, so anything keeping the id alive for the stack's lifetime
 * would keep the animator alive forever, and nothing keeping it alive gets the animator collected at the next GC while
 * the stack is still being rendered.
 */
@SuppressWarnings("UnstableApiUsage")
public class AzIdentifiableItemStackAnimatorCache {

    private static final AzIdentifiableItemStackAnimatorCache INSTANCE = new AzIdentifiableItemStackAnimatorCache();

    public static final long ANIMATOR_IDLE_SECONDS = 120;

    private static final Map<AzItemAnimatorView, Cache<UUID, AzItemAnimator>> ANIMATORS_BY_VIEW;

    private static final Cache<UUID, Map<AzItemAnimatorView, List<Consumer<AzItemAnimator>>>> PENDING =
        CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build();

    static {
        ANIMATORS_BY_VIEW = new EnumMap<>(AzItemAnimatorView.class);

        for (AzItemAnimatorView view : AzItemAnimatorView.values()) {
            Cache<UUID, AzItemAnimator> animators = CacheBuilder.newBuilder()
                .expireAfterAccess(ANIMATOR_IDLE_SECONDS, TimeUnit.SECONDS)
                .build();
            ANIMATORS_BY_VIEW.put(view, animators);
        }
    }

    public static AzIdentifiableItemStackAnimatorCache getInstance() {
        return INSTANCE;
    }

    private AzIdentifiableItemStackAnimatorCache() {}

    /**
     * Registers the animator for {@code view} of the given stack, keyed by {@link AzItemIdentity#resolve(ItemStack)}.
     * Never writes to the stack.
     */
    public void add(ItemStack itemStack, AzItemAnimatorView view, AzItemAnimator animator) {
        if (itemStack == null || AzureLibUtil.isEmpty(itemStack) || animator == null) {
            return;
        }

        add(AzItemIdentity.resolve(itemStack), view, animator);
    }

    /**
     * Registers the animator for {@code view} under {@code uuid}, replaying any commands that were queued for it.
     */
    public void add(UUID uuid, AzItemAnimatorView view, AzItemAnimator animator) {
        if (uuid == null || animator == null) {
            return;
        }

        if (ANIMATORS_BY_VIEW.get(view).asMap().putIfAbsent(uuid, animator) == null) {
            flushPending(uuid, view, animator);
        }
    }

    public @Nullable AzItemAnimator getOrNull(UUID uuid) {
        return getOrNull(uuid, AzItemAnimatorView.DEFAULT);
    }

    public @Nullable AzItemAnimator getOrNull(UUID uuid, AzItemAnimatorView view) {
        return uuid == null ? null : ANIMATORS_BY_VIEW.get(view).getIfPresent(uuid);
    }

    /**
     * Runs {@code action} on the animator for {@code view}, or on every existing animator of the stack when
     * {@code view} is null. Views that have no animator yet are skipped.
     */
    public void forEach(UUID uuid, @Nullable AzItemAnimatorView view, Consumer<AzItemAnimator> action) {
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

    public void dispatchOrQueue(UUID uuid, @Nullable AzItemAnimatorView view, Consumer<AzItemAnimator> action) {
        if (uuid == null) {
            return;
        }

        AzItemAnimatorView[] targets = view != null ? new AzItemAnimatorView[] { view } : AzItemAnimatorView.values();

        for (AzItemAnimatorView target : targets) {
            AzItemAnimator animator = getOrNull(uuid, target);

            if (animator != null) {
                action.accept(animator);
            } else {
                PENDING.asMap()
                    .computeIfAbsent(uuid, k -> new EnumMap<>(AzItemAnimatorView.class))
                    .computeIfAbsent(target, k -> new ArrayList<>())
                    .add(action);
            }
        }
    }

    private static void flushPending(UUID uuid, AzItemAnimatorView view, AzItemAnimator animator) {
        Map<AzItemAnimatorView, List<Consumer<AzItemAnimator>>> byView = PENDING.getIfPresent(uuid);

        if (byView == null) {
            return;
        }

        List<Consumer<AzItemAnimator>> queued = byView.remove(view);

        if (byView.isEmpty()) {
            PENDING.invalidate(uuid);
        }

        if (queued != null) {
            queued.forEach(action -> action.accept(animator));
        }
    }
}
