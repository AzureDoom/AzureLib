v1.0.8

### Additions
- `AzItemIdentity`, the single utility for reading, creating and clearing an item stack's AzureLib id (`resolve`, `getOrCreate`, `peek`, `hasPersistentId`, `clear`).

### Fixes
- Item stacks are no longer given an `az_id` UUID when they are constructed. Creative tabs, recipe outputs and JEI's ingredient list now see NBT-free stacks, so AzureLib items match their JEI entries and recipes again, and crafted copies of a recipe output no longer share one id.
- Item and armor animators are no longer lost on garbage collection while the stack is still being rendered, which could restart idle animations or cut off triggered ones mid-play.
- Stacks without an id now keep one stable animator while rendered, instead of a new animator (and a new random id) every frame.
- `AzCommand#sendForItem` no longer throws a `NullPointerException` when the stack has no NBT.

### Changes
- Improved animation performance on models with many bones. The bone cache now walks bones in a fixed array and skips any bone that has fully eased back to its bind pose, so its cost scales with the animated bones instead of the whole model.
- Bones scaled to zero on every axis, along with everything under them, are no longer rendered, instead of being drawn as collapsed geometry.
- A stack gets its persistent id on the server when it first ticks in an entity's inventory, or on its first server-side dispatch (`AzCommand#sendForItem`) if it never does. Rendering never writes NBT; stacks without an id are keyed by a transient, per-instance id.
- Item animators now expire after 120 seconds without being rendered or dispatched to, instead of being weakly held.
- Creative "pick block" clones have their copied id removed rather than replaced, and get their own the next time one is assigned.
- Adding or swapping only the `az_id` on a held stack no longer plays the re-equip animation.
- Removed `ItemStackMixin_AzItemStackIdentityRegistry` and `ItemMixin_EnsureCraftHasID`; added `ItemStackMixin_AzAssignIdInInventory`.

### Developer Notes
- `AzCachedBoneUpdateUtil` has new overloads that take an `AzBoneSnapshot` directly and return whether the channel is at rest. The existing `Map` overloads still work and delegate to them.
- Added `AzBoneCache#invalidateBoneArrays()`. Call it if you replace or remove entries in `getBoneSnapshotsByName()` directly; changing a snapshot's values doesn't need it.
- While a bone is scaled to zero on all three axes, its descendants don't update their tracked matrices and per-bone render layers aren't called for that subtree.
- Only register items that use triggered animations in `AzIdentityRegistry`. Registered items get a unique `az_id` once they sit in an inventory, so stackable items registered there stop merging.
- When dispatching for a stack that never sits in an inventory (such as a mob's held item), call `sendForItem` on the server with the actual stack, not a copy: that stack is the one that receives the id.
- Code that read `az_id` directly from NBT should use `AzItemIdentity.peek` (maybe null until an id is assigned) or `AzItemIdentity.getOrCreate` on the server.
- Existing stacks that already carry an `az_id` keep it.
