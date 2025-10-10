package mod.azure.azurelib.animation.controller;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

import mod.azure.azurelib.animation.primitive.AzQueuedAnimation;
import mod.azure.azurelib.model.AzBoneSnapshot;

/**
 * A cache system for managing and storing {@link AzBoneSnapshot} objects related to specific animations and their
 * corresponding bone animations. This class is designed to optimize the retrieval and utilization of bone snapshots
 * during animation interpolation (lerping). <br/>
 * <br/>
 * The cache works by filtering and storing relevant {@code AzBoneSnapshot} instances for a given
 * {@link AzQueuedAnimation}, based on the associated bone animations defined within the animation. It ensures that only
 * the required snapshots are cached, reducing redundancy and improving performance during animation processing.
 */
public class AzBoneSnapshotCache {

    private final Map<String, AzBoneSnapshot> boneSnapshots;

    public AzBoneSnapshotCache() {
        this.boneSnapshots = new Object2ObjectOpenHashMap<>();
    }

    /**
     * Cache the relevant {@link AzBoneSnapshot AzBoneSnapshots} for the current {@link AzQueuedAnimation} for animation
     * lerping
     *
     * @param animation The {@code QueuedAnimation} to filter {@code BoneSnapshots} for
     * @param snapshots The master snapshot collection to pull filter from
     */
    public void put(AzQueuedAnimation animation, Collection<AzBoneSnapshot> snapshots) {
        if (animation.animation().boneAnimations() == null) {
            return;
        }

        for (var snapshot : snapshots) {
            for (var boneAnimation : animation.animation().boneAnimations()) {
                if (boneAnimation.boneName().equals(snapshot.getBone().getName())) {
                    boneSnapshots.put(boneAnimation.boneName(), AzBoneSnapshot.copy(snapshot));
                    break;
                }
            }
        }
    }

    /**
     * Retrieves an {@link AzBoneSnapshot} from the cache based on the provided bone name. If no snapshot is found for
     * the specified name, {@code null} is returned.
     *
     * @param name The name of the bone whose snapshot is to be retrieved.
     * @return The {@link AzBoneSnapshot} associated with the given name, or {@code null} if not found.
     */
    public @Nullable AzBoneSnapshot getOrNull(String name) {
        return boneSnapshots.get(name);
    }
}
