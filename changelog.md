v3.2.6

### Additions
- N/A

### Fixes
- Fixed item and armor animations randomly snapping to their rest pose and restarting mid-animation. The item animator cache was keyed weakly, so live animators were being discarded on garbage collection.
- Fixed armor and item renderers always creating their animation context with a random UUID instead of the stack's Az ID.
- Fixed stacks duplicated in containers (creative middle-click clone, quick-craft drag distribution) sharing an Az ID, and therefore an animator, with the stack they were copied from.
- Fixed the item animator cache adding an empty tag to untagged stacks just by checking for an Az ID, which caused unnecessary client/server slot resyncs.
- Fixed a possible crash when reading a malformed Az ID tag.
- Fixed Better Combat attacks only registering when the swing touched an entity's vanilla bounding box. Attacks now use animated combat hitboxes for target search, collision, and range/angle checks.
- Fixed entities with a live hitbox pose but no combat hitboxes being impossible to hit with Better Combat. They now fall back to their vanilla bounding box.
- Fixed arm, leg and boot pieces of AzureLib armor on AzureLib entities being placed at the wrong position (for example, gauntlets rendering at the entity's feet), depending on the order the entity's bones rendered in.

### Changes
- The item animator cache now expires animators after 5 minutes without access, replacing weak references.
- Removed the Az ID comparison hooks from container slot sync. Vanilla stack matching already compares the Az ID, and the old checks were inverted and could never match.
- The container Az ID mixin now uses `@WrapOperation` instead of `@Redirect` for better compatibility with other mods.
- Better Combat compat is now split into three mixins (initial targets, collision filter, radial filter), and the initial target search no longer depends on remapped Minecraft method names inside Better Combat.

### Developer Notes
- Added `AzItemIdentity`, the single place stack identity is read and written: `peek` reads the persistent ID, `resolve` returns the ID animators are keyed by (persistent, or a temporary per-instance ID), `getOrCreate` assigns one, `clear` removes it, and `differsOnlyByIdentity` compares stacks while ignoring it.
- `AzItemIdentity.getOrCreate` is server-only. Calling it on the client gives that stack a different ID from the server's.
- On the server, `AzCommand.sendForItem` assigns the Az ID if the stack has none, so pass the stack that lives in the inventory rather than a copy.
- Stacks that never tick in an inventory (item frames, block entity slots, dropped items) have no Az ID until an animation command is sent for them, and render under a temporary ID until then. On dropped items, an ID assigned this way may never reach clients, because changing the stack doesn't resync the item entity, so don't rely on animating items lying on the ground.
- On 26.3 the `az_id` component is registered with `ignoreSwapAnimation`, so vanilla ignores it when deciding whether to play the held item's swap animation. 1.18.2, 1.20.1 and 1.21.1 do the same through Forge/NeoForge's `shouldCauseReequipAnimation` hook and, on Fabric, `ItemInHandRenderer.tick`.
- `AzIdentityRegistry.register` no longer writes to the item's default instance; registration only marks the item as identified.
- Added `getOrNull(ItemStack, AzItemAnimatorView)`, `add(UUID, AzItemAnimatorView, AzItemAnimator)`, `remove(UUID)`, `clear()` and `ANIMATOR_IDLE_SECONDS` to `AzIdentifiableItemStackAnimatorCache`. Call `clear()` on client disconnect to release cached animators.
- Item dispatch packets should be handled on the main thread, since queued commands run on whichever thread registers the animator.
- Splitting a stackable animated item with `ItemStack.split()` still leaves both halves with the same Az ID. This only affects animated items that can stack.
