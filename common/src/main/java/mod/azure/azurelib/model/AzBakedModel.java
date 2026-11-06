package mod.azure.azurelib.model;

import org.jetbrains.annotations.Nullable;

import java.util.*;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.cache.object.GeoSkinnedMesh;

/**
 * Represents a baked 3D model consisting of hierarchical bone structures. This class is immutable and provides
 * read-only access to bones by name or as a list of top-level bones. Bones are uniquely identified by their names.
 */
public class AzBakedModel {

    private static final Set<GeoSkinnedMesh> WARNED_UNBOUND = Collections.newSetFromMap(
        Collections.synchronizedMap(new WeakHashMap<>())
    );

    private static final Set<GeoSkinnedMesh> WARNED_MISSING_JOINT = Collections.newSetFromMap(
        Collections.synchronizedMap(new WeakHashMap<>())
    );

    private static AzBakedModel defaultModel = new AzBakedModel(List.of());

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
            ArrayList<AzSkinBinding> bindings = new ArrayList<>(meshes.size());

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

            owner.setSkinBindings(bindings.toArray(AzSkinBinding[]::new));
        }
    }

    private Map<AzBone, AzBone> mapParents() {
        IdentityHashMap<AzBone, AzBone> parents = new IdentityHashMap<>();
        ArrayDeque<AzBone> queue = new ArrayDeque<>(topLevelBones);

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
        ArrayDeque<AzBone> chain = new ArrayDeque<>();

        for (AzBone current = bone; current != null; current = parents.get(current)) {
            chain.addFirst(current);
        }

        return chain.toArray(AzBone[]::new);
    }

    private Map<String, AzBone> mapBonesByName(List<AzBone> bones) {
        var bonesByName = new HashMap<String, AzBone>();
        var nodesToMap = new ArrayDeque<>(bones);

        while (!nodesToMap.isEmpty()) {
            var currentBone = nodesToMap.poll();
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
        defaultModel = model != null ? model : new AzBakedModel(List.of());
    }
}
