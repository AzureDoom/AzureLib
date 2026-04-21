/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.loading.object;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

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
public record GeometryTree(
    Map<String, BoneStructure> topLevelBones,
    ModelProperties properties
) {

    public static GeometryTree fromModel(Model model) {
        Map<String, BoneStructure> topLevelBones = new Object2ObjectOpenHashMap<>();
        MinecraftGeometry geometry = model.minecraftGeometry()[0];
        List<Bone> bones = new ObjectArrayList<>(geometry.bones());
        Map<String, Bone> allBonesByName = new Object2ObjectOpenHashMap<>();

        for (Bone bone : geometry.bones()) {
            allBonesByName.put(bone.name(), bone);
        }

        while (!bones.isEmpty()) {
            int remainingBeforePass = bones.size();

            for (int index = bones.size() - 1; index >= 0; index--) {
                Bone bone = bones.get(index);

                if (bone.parent() == null || bone.parent().isBlank()) {
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
