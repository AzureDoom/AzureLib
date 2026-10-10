package mod.azure.azurelib.model;

import com.google.common.collect.ImmutableList;

import java.util.*;
import javax.annotation.Nullable;

/**
 * Represents a baked 3D model consisting of hierarchical bone structures. This class is immutable and provides
 * read-only access to bones by name or as a list of top-level bones. Bones are uniquely identified by their names.
 */
public class AzBakedModel {

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
