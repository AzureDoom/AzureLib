package mod.azure.azurelib.model;

import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.loading.json.raw.Bone;

/**
 * AzBoneMetadata is a record class representing metadata about a 3D model bone. This metadata provides information such
 * as rendering preferences, inflation values, mirroring, hierarchy, and reset options for a bone in a 3D model's
 * structure.
 */
public record AzBoneMetadata(
    @Nullable Boolean dontRender,
    @Nullable Double inflate,
    Boolean mirror,
    String name,
    @Nullable AzBone parent,
    @Nullable Boolean reset
) {

    public AzBoneMetadata(Bone bone, AzBone parent) {
        this(bone.neverRender(), bone.inflate(), bone.mirror(), bone.name(), parent, bone.reset());
    }
}
