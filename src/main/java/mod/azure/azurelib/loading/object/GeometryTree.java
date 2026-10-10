/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.loading.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.loading.json.raw.Bone;
import mod.azure.azurelib.loading.json.raw.MinecraftGeometry;
import mod.azure.azurelib.loading.json.raw.Model;
import mod.azure.azurelib.loading.json.raw.ModelProperties;

/**
 * Container class for a {@link Bone} structure, used at startup during deserialization
 */
public final class GeometryTree {

    private final Map<String, BoneStructure> topLevelBones;

    private final ModelProperties properties;

    public GeometryTree(Map<String, BoneStructure> topLevelBones, ModelProperties properties) {
        this.topLevelBones = topLevelBones;
        this.properties = properties;
    }

    public Map<String, BoneStructure> topLevelBones() {
        return this.topLevelBones;
    }

    public ModelProperties properties() {
        return this.properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GeometryTree))
            return false;
        GeometryTree other = (GeometryTree) o;
        return java.util.Objects.equals(this.topLevelBones, other.topLevelBones)
            && java.util.Objects.equals(this.properties, other.properties);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.topLevelBones);
        result = 31 * result + java.util.Objects.hashCode(this.properties);
        return result;
    }

    @Override
    public String toString() {
        return "GeometryTree[topLevelBones=" + this.topLevelBones + ", properties=" + this.properties + "]";
    }

    public static GeometryTree fromModel(Model model) {
        Map<String, BoneStructure> topLevelBones = new HashMap<>();
        MinecraftGeometry geometry = model.minecraftGeometry()[0];
        List<Bone> bones = new ArrayList<>(java.util.Arrays.asList(geometry.bones()));
        Map<String, Bone> allBonesByName = new HashMap<>();

        for (Bone bone : geometry.bones()) {
            allBonesByName.put(bone.name(), bone);
        }

        while (!bones.isEmpty()) {
            int remainingBeforePass = bones.size();

            for (int index = bones.size() - 1; index >= 0; index--) {
                Bone bone = bones.get(index);

                if (bone.parent() == null || bone.parent().trim().isEmpty()) {
                    topLevelBones.put(bone.name(), new BoneStructure(bone));
                    bones.remove(index);
                    continue;
                }

                BoneStructure structure = findBoneStructureInTree(topLevelBones, bone.parent());

                if (structure != null) {
                    structure.children().put(bone.name(), new BoneStructure(bone));
                    bones.remove(index);
                }
            }

            if (bones.size() == remainingBeforePass) {
                boolean salvagedAny = false;

                for (int index = bones.size() - 1; index >= 0; index--) {
                    Bone bone = bones.get(index);
                    String parentName = bone.parent();

                    if (!allBonesByName.containsKey(parentName)) {
                        AzureLib.LOGGER.error(
                            "Invalid model bone hierarchy: bone '{}' references missing parent '{}'. Treating as top-level bone.",
                            bone.name(),
                            parentName
                        );

                        topLevelBones.put(bone.name(), new BoneStructure(bone));
                        bones.remove(index);
                        salvagedAny = true;
                    }
                }

                if (!salvagedAny) {
                    for (Bone bone : bones) {
                        AzureLib.LOGGER.error(
                            "Invalid model bone hierarchy: unable to resolve bone '{}' with parent '{}'. Treating as top-level bone.",
                            bone.name(),
                            bone.parent()
                        );

                        topLevelBones.put(bone.name(), new BoneStructure(bone));
                    }

                    bones.clear();
                }
            }
        }

        return new GeometryTree(topLevelBones, geometry.modelProperties());
    }

    private static BoneStructure findBoneStructureInTree(Map<String, BoneStructure> bones, String boneName) {
        for (BoneStructure entry : bones.values()) {
            if (boneName.equals(entry.self().name()))
                return entry;

            BoneStructure subStructure = findBoneStructureInTree(entry.children(), boneName);

            if (subStructure != null)
                return subStructure;
        }

        return null;
    }
}
