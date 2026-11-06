package mod.azure.azurelib.model;

import mod.azure.azurelib.cache.object.GeoSkinnedMesh;

/**
 * A {@link GeoSkinnedMesh} resolved against the bones of one {@link AzBakedModel} instance.
 * <p>
 * Each chain runs from a top-level bone down to (and including) the bone it is for, so a bone's current model-space
 * transform is the product of its chain's local transforms. Chains are built from the model's own hierarchy rather than
 * {@link AzBone#getParent()}, which still points into the original model on deep copies.
 * </p>
 */
public final class AzSkinBinding {

    private final GeoSkinnedMesh mesh;

    private final AzBone[] ownerChain;

    private final AzBone[][] jointChains;

    /**
     * @param mesh        the shared mesh data
     * @param ownerChain  chain of the bone that owns and renders the mesh
     * @param jointChains chain of each joint, parallel to {@link GeoSkinnedMesh#joints()}; a joint missing from the
     *                    model falls back to the owner chain, so its vertices stay rigid with the owner
     */
    public AzSkinBinding(GeoSkinnedMesh mesh, AzBone[] ownerChain, AzBone[][] jointChains) {
        this.mesh = mesh;
        this.ownerChain = ownerChain;
        this.jointChains = jointChains;
    }

    public GeoSkinnedMesh mesh() {
        return mesh;
    }

    public AzBone[] ownerChain() {
        return ownerChain;
    }

    public AzBone[][] jointChains() {
        return jointChains;
    }
}
