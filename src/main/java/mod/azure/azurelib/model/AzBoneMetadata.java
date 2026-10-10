package mod.azure.azurelib.model;

import mod.azure.azurelib.loading.json.raw.Bone;

/**
 * AzBoneMetadata is a record class representing metadata about a 3D model bone. This metadata provides information such
 * as rendering preferences, inflation values, mirroring, hierarchy, and reset options for a bone in a 3D model's
 * structure.
 */
public final class AzBoneMetadata {

    private final Boolean dontRender;

    private final Double inflate;

    private final Boolean mirror;

    private final String name;

    private final AzBone parent;

    private final Boolean reset;

    public AzBoneMetadata(
        Boolean dontRender,
        Double inflate,
        Boolean mirror,
        String name,
        AzBone parent,
        Boolean reset
    ) {
        this.dontRender = dontRender;
        this.inflate = inflate;
        this.mirror = mirror;
        this.name = name;
        this.parent = parent;
        this.reset = reset;
    }

    public Boolean dontRender() {
        return this.dontRender;
    }

    public Double inflate() {
        return this.inflate;
    }

    public Boolean mirror() {
        return this.mirror;
    }

    public String name() {
        return this.name;
    }

    public AzBone parent() {
        return this.parent;
    }

    public Boolean reset() {
        return this.reset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzBoneMetadata))
            return false;
        AzBoneMetadata other = (AzBoneMetadata) o;
        return java.util.Objects.equals(this.dontRender, other.dontRender)
            && java.util.Objects.equals(this.inflate, other.inflate)
            && java.util.Objects.equals(this.mirror, other.mirror)
            && java.util.Objects.equals(this.name, other.name)
            && java.util.Objects.equals(this.parent, other.parent)
            && java.util.Objects.equals(this.reset, other.reset);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.dontRender);
        result = 31 * result + java.util.Objects.hashCode(this.inflate);
        result = 31 * result + java.util.Objects.hashCode(this.mirror);
        result = 31 * result + java.util.Objects.hashCode(this.name);
        result = 31 * result + java.util.Objects.hashCode(this.parent);
        result = 31 * result + java.util.Objects.hashCode(this.reset);
        return result;
    }

    @Override
    public String toString() {
        return "AzBoneMetadata[dontRender=" + this.dontRender + ", inflate=" + this.inflate + ", mirror=" + this.mirror
            + ", name=" + this.name + ", parent=" + this.parent + ", reset=" + this.reset + "]";
    }

    public AzBoneMetadata(Bone bone, AzBone parent) {
        this(bone.neverRender(), bone.inflate(), bone.mirror(), bone.name(), parent, bone.reset());
    }
}
