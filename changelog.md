v1.1.5

### Additions
- N/A

### Fixes
- Fixed item and armor animations randomly snapping to their rest pose and restarting mid-animation. The item animator cache was keyed weakly, so live animators were being discarded on garbage collection.
- Fixed all item stacks without an Az ID sharing a single animator.
- Fixed the client giving stacks its own Az ID when rendering them, which could disagree with the server's ID for the same stack. Rendering no longer writes to stacks, including stacks of items that aren't registered for identity.
- Fixed client-side animation commands being skipped for stacks that hadn't received an Az ID yet.
- Fixed arm, leg and boot pieces of AzureLib armor on AzureLib entities being placed at the wrong position (for example, gauntlets rendering at the entity's feet), depending on the order the entity's bones rendered in.

### Changes
- Az IDs are now assigned on the server when a stack first ticks in an inventory, or the first time an animation command is sent for it, instead of whenever a stack is created.
- Item animators now stay cached for 5 minutes without being rendered or dispatched to.

### Developer Notes
- Added `AzItemIdentity`, the single place stack identity is decided: `peek` reads the persistent ID, `resolve` returns the ID animators are keyed by (persistent, or a temporary per-instance ID), `getOrCreate` assigns one, and `adopt` applies a server-sent ID on the client. `AzItemIds` remains the low-level reader and writer of the StationAPI NBT.
- `AzItemIdentity.getOrCreate` is server-only. Calling it on the client gives that stack a different ID from the server's.
- On the server, `AzCommand.sendForItem` assigns the Az ID if the stack has none, so pass the stack that lives in the inventory rather than a copy.
- Worn armor doesn't tick in the inventory, so it gets its Az ID the first time an animation command is sent for it.
- When a dispatch arrives for a stack whose NBT hasn't synced yet, the client adopts the server's ID and moves the stack's existing animator to it, so the animation continues instead of restarting.
- Added `getOrNull(ItemStack, AzItemAnimatorView)`, `add(ItemStack, AzItemAnimator)`, `add(UUID, AzItemAnimatorView, AzItemAnimator)`, `remove(UUID)`, `clear()` and `ANIMATOR_IDLE_SECONDS` to `AzIdentifiableItemStackAnimatorCache`. Call `clear()` on disconnect or world unload to release cached animators.
