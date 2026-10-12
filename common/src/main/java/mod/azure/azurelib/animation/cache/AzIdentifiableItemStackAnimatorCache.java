package mod.azure.azurelib.animation.cache;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.impl.AzItemAnimator;
import mod.azure.azurelib.animation.impl.AzItemAnimatorView;

/**
 * The AzIdentifiableItemStackAnimatorCache class is a singleton utility for managing a cache of {@link ItemStack}
 * objects, each associated with a unique identifier (UUID). This class provides functionality to register and retrieve
 * item animators that apply to specific {@link ItemStack}s using their respective UUIDs. <br>
 * Animators are stored per {@link AzItemAnimatorView}, so a single stack can have a separate first-person animator
 * alongside its default one.
 */
public class AzIdentifiableItemStackAnimatorCache {

    private static final AzIdentifiableItemStackAnimatorCache INSTANCE = new AzIdentifiableItemStackAnimatorCache();

    private static final Map<AzItemAnimatorView, WeakHashMap<UUID, AzItemAnimator>> ANIMATORS_BY_VIEW;

    static {
        ANIMATORS_BY_VIEW = new EnumMap<>(AzItemAnimatorView.class);

        for (AzItemAnimatorView view : AzItemAnimatorView.values()) {
            ANIMATORS_BY_VIEW.put(view, new WeakHashMap<>());
        }
    }

    public static AzIdentifiableItemStackAnimatorCache getInstance() {
        return INSTANCE;
    }

    private AzIdentifiableItemStackAnimatorCache() {}

    /**
     * Registers the {@link AzItemAnimatorView#DEFAULT default} animator for the given stack.
     */
    public void add(ItemStack itemStack, AzItemAnimatorView view, AzItemAnimator animator) {
        var tag = itemStack.getOrCreateTag();

        if (itemStack.getOrCreateTag().contains(AzureLib.ITEM_UUID_TAG)) {
            ANIMATORS_BY_VIEW.get(view).putIfAbsent(tag.getUUID(AzureLib.ITEM_UUID_TAG), animator);
        }
    }

    public @Nullable AzItemAnimator getOrNull(UUID uuid) {
        return getOrNull(uuid, AzItemAnimatorView.DEFAULT);
    }

    public @Nullable AzItemAnimator getOrNull(UUID uuid, AzItemAnimatorView view) {
        return uuid == null ? null : ANIMATORS_BY_VIEW.get(view).get(uuid);
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

        for (WeakHashMap<UUID, AzItemAnimator> animators : ANIMATORS_BY_VIEW.values()) {
            AzItemAnimator animator = animators.get(uuid);

            if (animator != null) {
                action.accept(animator);
            }
        }
    }
}
