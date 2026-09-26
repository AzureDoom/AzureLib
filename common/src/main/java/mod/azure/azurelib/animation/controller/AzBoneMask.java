package mod.azure.azurelib.animation.controller;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import mod.azure.azurelib.model.AzBone;

/**
 * Limits which bones a controller animates. A named bone includes all of its children, so
 * {@code AzBoneMask.only("upper_body")} covers the arms and head under it. Bones outside the mask are skipped entirely:
 * their keyframes are not evaluated and the controller never writes to them.
 */
public final class AzBoneMask {

    /** Every bone; the default. */
    public static final AzBoneMask ALL = new AzBoneMask(Set.of(), false);

    private final Set<String> boneNames;

    /** True: only bones under a named bone. False: every bone except those under a named bone. */
    private final boolean onlyNamed;

    private AzBoneMask(Set<String> boneNames, boolean onlyNamed) {
        this.boneNames = boneNames;
        this.onlyNamed = onlyNamed;
    }

    /** Only the named bones and their children. */
    public static AzBoneMask only(String... boneNames) {
        return only(List.of(boneNames));
    }

    /** Only the named bones and their children. */
    public static AzBoneMask only(Collection<String> boneNames) {
        return new AzBoneMask(Set.copyOf(boneNames), true);
    }

    /** Every bone except the named bones and their children. */
    public static AzBoneMask except(String... boneNames) {
        return except(List.of(boneNames));
    }

    /** Every bone except the named bones and their children. */
    public static AzBoneMask except(Collection<String> boneNames) {
        return new AzBoneMask(Set.copyOf(boneNames), false);
    }

    public boolean includes(AzBone bone) {
        if (boneNames.isEmpty()) {
            return !onlyNamed;
        }

        boolean underNamedBone = false;

        for (var current = bone; current != null; current = current.getParent()) {
            if (boneNames.contains(current.getName())) {
                underNamedBone = true;
                break;
            }
        }

        return underNamedBone == onlyNamed;
    }
}
