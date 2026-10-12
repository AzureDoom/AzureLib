v1.0.3

### Additions
- Added separate first-person animators for items. Item renderers built with `useSeparateFirstPersonAnimator()` give the first-person hands their own animator, so first person and third person can play different animations on the same item stack at the same time (for example, an aimed fire in first person while other players see a hip fire).
- Added `AzCommand#sendForItemFirstPerson`, `AzCommand#sendForItemThirdPerson` and `AzCommand#sendForItem(Entity, ItemStack, AzItemAnimatorView)` to play a command on only one view.

### Fixes
- N/A

### Changes
- `AzCommand#sendForItem(Entity, ItemStack)` now plays on every animator the stack has. For renderers that don't use a separate first-person animator this is the same single animator as before.

### Developer Notes
- Added `AzItemAnimatorView` (`DEFAULT`, `FIRST_PERSON`). While an item renders, the view for its display context is the current view on the render thread, so `AzAnimatorAccessor` lookups on an `ItemStack` during rendering (render layers, `AzProvider`, custom pipeline code) return that view's animator. Outside of rendering they return the `DEFAULT` animator.
- Added `AzItemRendererConfig.Builder#setAnimatorViewProvider(Function<AzItemDisplayContext, AzItemAnimatorView>)` for custom context-to-view mappings. `AzItemRendererConfig#animatorView(AzItemDisplayContext)` returns the view used for a context.
- `AzIdentifiableItemStackAnimatorCache` now stores animators per view. The existing `add(ItemStack, AzItemAnimator)` and `getOrNull(UUID)` still use `DEFAULT`; new overloads take an `AzItemAnimatorView`, and `forEach(UUID, AzItemAnimatorView, Consumer)` runs on one view or, with `null`, every view.
- `ClientUtils#getCurrentAnimationController` and `#getCurrentAnimationTick` read the `DEFAULT` animator when called outside of rendering. To read the first-person animator, use `AzIdentifiableItemStackAnimatorCache.getInstance().getOrNull(uuid, AzItemAnimatorView.FIRST_PERSON)`.
- `AzItemStackDispatchCommandPacket` has a new optional view field, which changes its network format. Clients and servers must run matching AzureLib versions.