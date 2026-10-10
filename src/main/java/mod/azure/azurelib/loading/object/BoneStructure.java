/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.loading.object;

import java.util.HashMap;
import java.util.Map;

import mod.azure.azurelib.loading.json.raw.Bone;

/**
 * Container class for holding a {@link Bone} structure. Used at startup in deserialization
 */
public final class BoneStructure {

    private final Bone self;

    private final Map<String, BoneStructure> children;

    public BoneStructure(Bone self, Map<String, BoneStructure> children) {
        this.self = self;
        this.children = children;
    }

    public Bone self() {
        return this.self;
    }

    public Map<String, BoneStructure> children() {
        return this.children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof BoneStructure))
            return false;
        BoneStructure other = (BoneStructure) o;
        return java.util.Objects.equals(this.self, other.self)
            && java.util.Objects.equals(this.children, other.children);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.self);
        result = 31 * result + java.util.Objects.hashCode(this.children);
        return result;
    }

    @Override
    public String toString() {
        return "BoneStructure[self=" + this.self + ", children=" + this.children + "]";
    }

    public BoneStructure(Bone self) {
        this(self, new HashMap<>());
    }
}
