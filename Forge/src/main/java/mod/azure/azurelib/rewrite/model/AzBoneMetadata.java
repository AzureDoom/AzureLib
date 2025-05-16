package mod.azure.azurelib.rewrite.model;

import mod.azure.azurelib.loading.json.raw.Bone;

/**
 * AzBoneMetadata is a class representing metadata about a 3D model bone. This metadata provides information such as
 * rendering preferences, inflation values, mirroring, hierarchy, and reset options for a bone in a 3D model's
 * structure.
 */
public class AzBoneMetadata {

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

    public AzBoneMetadata(Bone bone, AzBone parent) {
        this(
            bone.neverRender(),
            bone.inflate(),
            bone.mirror(),
            bone.name(),
            parent,
            bone.reset()
        );
    }

    public Boolean getDontRender() {
        return dontRender;
    }

    public Double getInflate() {
        return inflate;
    }

    public Boolean getMirror() {
        return mirror;
    }

    public String getName() {
        return name;
    }

    public AzBone getParent() {
        return parent;
    }

    public Boolean getReset() {
        return reset;
    }

    @Override
    public String toString() {
        return "AzBoneMetadata{" +
            "dontRender=" + dontRender +
            ", inflate=" + inflate +
            ", mirror=" + mirror +
            ", name='" + name + '\'' +
            ", parent=" + parent +
            ", reset=" + reset +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzBoneMetadata))
            return false;

        AzBoneMetadata that = (AzBoneMetadata) o;

        if (dontRender != null ? !dontRender.equals(that.dontRender) : that.dontRender != null)
            return false;
        if (inflate != null ? !inflate.equals(that.inflate) : that.inflate != null)
            return false;
        if (!mirror.equals(that.mirror))
            return false;
        if (!name.equals(that.name))
            return false;
        if (parent != null ? !parent.equals(that.parent) : that.parent != null)
            return false;
        return reset != null ? reset.equals(that.reset) : that.reset == null;
    }

    @Override
    public int hashCode() {
        int result = dontRender != null ? dontRender.hashCode() : 0;
        result = 31 * result + (inflate != null ? inflate.hashCode() : 0);
        result = 31 * result + mirror.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        result = 31 * result + (reset != null ? reset.hashCode() : 0);
        return result;
    }
}
