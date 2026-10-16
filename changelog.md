v1.0.2

### Additions
- N/A

### Fixes
- N/A

### Changes
- Improved animation performance on models with many bones. The bone cache now walks bones in a fixed array and skips any bone that has fully eased back to its bind pose, so its cost scales with the animated bones instead of the whole model.
- Bones scaled to zero on every axis, along with everything under them, are no longer rendered, instead of being drawn as collapsed geometry.

### Developer Notes
- `AzCachedBoneUpdateUtil` has new overloads that take an `AzBoneSnapshot` directly and return whether the channel is at rest. The existing `Map` overloads still work and delegate to them.
- Added `AzBoneCache#invalidateBoneArrays()`. Call it if you replace or remove entries in `getBoneSnapshotsByName()` directly; changing a snapshot's values doesn't need it.
- While a bone is scaled to zero on all three axes, its descendants don't update their tracked matrices and per-bone render layers aren't called for that subtree.
