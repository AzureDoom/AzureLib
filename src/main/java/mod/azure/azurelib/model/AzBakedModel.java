package mod.azure.azurelib.model;

import com.google.common.collect.ImmutableList;

import java.util.*;
import javax.annotation.Nullable;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.cache.object.GeoSkinnedMesh;

/**
 * Represents a baked 3D model consisting of hierarchical bone structures. This class is immutable and provides
 * read-only access to bones by name or as a list of top-level bones. Bones are uniquely identified by their names.
 */
public class AzBakedModel {

    /** Meshes already reported, so per-entity model copies don't repeat the same warning. */
    private static final Set<GeoSkinnedMesh> WARNED_UNBOUND = Collections.newSetFromMap(
        Collections.synchronizedMap(new WeakHashMap<GeoSkinnedMesh, Boolean>())
    );

    private static final Set<GeoSkinnedMesh> WARNED_MISSING_JOINT = Collections.newSetFromMap(
        Collections.synchronizedMap(new WeakHashMap<GeoSkinnedMesh, Boolean>())
    );

    private static AzBakedModel defaultModel = new AzBakedModel(ImmutableList.of());

    private final Map<String, AzBone> bonesByName;

    private final List<AzBone> topLevelBones;

    private final UUID modelUUID;

    public AzBakedModel(List<AzBone> topLevelBones) {
        this(topLevelBones, UUID.randomUUID());
    }

    public AzBakedModel(List<AzBone> topLevelBones, UUID modelUUID) {
        this.topLevelBones = Collections.unmodifiableList(topLevelBones);
        this.bonesByName = Collections.unmodifiableMap(mapBonesByName(topLevelBones));
        this.modelUUID = modelUUID;
        bindSkinnedMeshes();
    }

    /**
     * Resolves every bone's skinned meshes against this model's own bones (deep copies included). Bind poses are not
     * computed here: they come from the geometry's rest pose when the model is baked (see
     * {@link mod.azure.azurelib.model.factory.AzBakedModelFactory#bindSkinnedMeshes}), so building or copying a model
     * never depends on the pose its bones are in.
     */
    private void bindSkinnedMeshes() {
        Map<AzBone, AzBone> parents = null;

        for (AzBone owner : bonesByName.values()) {
            List<GeoSkinnedMesh> meshes = owner.getSkinnedMeshes();

            if (meshes.isEmpty()) {
                owner.setSkinBindings(null);
                continue;
            }

            if (parents == null) {
                parents = mapParents();
            }

            AzBone[] ownerChain = chainOf(owner, parents);
            List<AzSkinBinding> bindings = new ArrayList<AzSkinBinding>(meshes.size());

            for (GeoSkinnedMesh mesh : meshes) {
                if (!mesh.isBound()) {
                    if (WARNED_UNBOUND.add(mesh)) {
                        AzureLib.LOGGER.error(
                            "Skinned mesh '{}' on bone '{}' has no bind pose and won't render. Custom model factories must call AzBakedModelFactory.bindSkinnedMeshes from constructGeoModel.",
                            mesh.name(),
                            owner.getName()
                        );
                    }

                    continue;
                }

                int jointCount = mesh.jointCount();
                AzBone[][] jointChains = new AzBone[jointCount][];

                for (int i = 0; i < jointCount; i++) {
                    AzBone joint = bonesByName.get(mesh.joint(i));

                    if (joint == null && WARNED_MISSING_JOINT.add(mesh)) {
                        AzureLib.LOGGER.warn(
                            "Skinned mesh '{}' on bone '{}' references missing joint '{}'; its vertices will follow '{}'",
                            mesh.name(),
                            owner.getName(),
                            mesh.joint(i),
                            owner.getName()
                        );
                    }

                    jointChains[i] = joint != null ? chainOf(joint, parents) : ownerChain;
                }

                bindings.add(new AzSkinBinding(mesh, ownerChain, jointChains));
            }

            owner.setSkinBindings(bindings.toArray(new AzSkinBinding[0]));
        }
    }

    private Map<AzBone, AzBone> mapParents() {
        Map<AzBone, AzBone> parents = new IdentityHashMap<AzBone, AzBone>();
        ArrayDeque<AzBone> queue = new ArrayDeque<AzBone>(topLevelBones);

        while (!queue.isEmpty()) {
            AzBone bone = queue.poll();

            for (AzBone child : bone.getChildBones()) {
                parents.put(child, bone);
                queue.add(child);
            }
        }

        return parents;
    }

    private static AzBone[] chainOf(AzBone bone, Map<AzBone, AzBone> parents) {
        ArrayDeque<AzBone> chain = new ArrayDeque<AzBone>();

        for (AzBone current = bone; current != null; current = parents.get(current)) {
            chain.addFirst(current);
        }

        return chain.toArray(new AzBone[0]);
    }

    private Map<String, AzBone> mapBonesByName(List<AzBone> bones) {
        HashMap<String, AzBone> bonesByName = new HashMap<String, AzBone>();
        ArrayDeque<AzBone> nodesToMap = new ArrayDeque<>(bones);

        while (!nodesToMap.isEmpty()) {
            AzBone currentBone = nodesToMap.poll();
            nodesToMap.addAll(currentBone.getChildBones());
            currentBone.saveInitialSnapshot();
            bonesByName.put(currentBone.getName(), currentBone);
        }

        return bonesByName;
    }

    public AzBakedModel deepCopy() {
        List<AzBone> copied = new ArrayList<>(this.topLevelBones.size());
        for (AzBone bone : this.topLevelBones) {
            copied.add(bone.deepCopy()); // each child deepCopy() calls saveInitialSnapshot()
        }
        return new AzBakedModel(copied, this.modelUUID); // this will rebuild bonesByName internally
    }

    public @Nullable AzBone getBoneOrNull(String name) {
        return bonesByName.get(name);
    }

    public Optional<AzBone> getBone(String name) {
        return Optional.ofNullable(getBoneOrNull(name));
    }

    public Map<String, AzBone> getBonesByName() {
        return bonesByName;
    }

    public List<AzBone> getTopLevelBones() {
        return topLevelBones;
    }

    public UUID getModelUUID() {
        return modelUUID;
    }

    public static AzBakedModel getDefault() {
        return defaultModel;
    }

    public static void setDefault(AzBakedModel model) {
        defaultModel = model != null ? model : new AzBakedModel(ImmutableList.of());
    }
}
