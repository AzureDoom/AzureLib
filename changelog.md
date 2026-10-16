v1.0.7

### Additions
- Added `AzAnimator#onFirstAnimate(T)`, a hook that runs once on an animator's first `animate()` call, right before its controllers update. Commands dispatched here apply on that same frame, making it the reliable place to start spawn/initial animations.
- Added `AzAnimator#getOrCreateContext(K, T)`, which binds the animatable to the context when it is created.
- Added `AzPendingDispatches`, which queues animation commands sent to an entity or block entity before its animator exists and replays them once the animator is created.
- Added `AzIdentifiableItemStackAnimatorCache#dispatchOrQueue`, which does the same for item stacks per `AzItemAnimatorView`.

### Fixes
- Fixed animatables rendering in their bind pose (T-pose) for a frame when a transition finished, most noticeably on controllers with a transition length of 0. The transition state now hands off to the play state and applies keyframes in the same update.
- Fixed `AzAnimationTimer` adding the full absolute render time to `animTime` on its first tick, which made controllers treat transitions as instantly finished.
- Fixed animation commands being silently dropped when they arrived before the animatable had been rendered (entity, block entity, and item stack dispatch packets, and client-side dispatch).
- Fixed "Unable to create animation queue: animatable is null" when a command was dispatched before an animator's first `animate()` call, which left the controller with nothing to play.

### Changes
- `AzProvider` now binds the animatable to the animation context on creation and flushes pending dispatches after registering controllers, both when creating an animator and when rebuilding it on a model change.
- Item stack dispatches (server packet and `AzCommand#sendForItem` on the client) now go through `dispatchOrQueue` instead of skipping stacks with no animator yet.

### Developer Notes
- Use `onFirstAnimate` to start an entity's initial animation instead of keying off `tickCount == 0`. A freshly spawned entity is not guaranteed to render before its first client tick, especially on the first spawn after loading a world.
- Commands sent before an animatable's first render are now replayed when its animator is created rather than discarded, so server-sent spawn animations will now play where they previously did not.
- Queued item stack commands expire after 10 seconds. A command with no view is queued separately for each view that has no animator yet.
