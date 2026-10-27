v3.3.2

### Additions
- Sequence stages can now set their own animation speed, transition length, easing, start offset, freeze point and repeat count. These only apply while that stage plays and no longer stick to the controller afterwards.
- Added `AzSequence#freeze(name, tick)` for stages that freeze partway through.
- Added stage-anchored sequence events: `eventAt(name, keyframeTick)`, `eventOnStart(name)` and `eventOnEnd(name)`. They follow playback speed, transitions, start offsets, repeats and stage lengths instead of a fixed game-tick count.
- Added `AzSequenceTiming` and `AzSequenceTimeline` for working out when stages and events happen, plus `AzSequencePlayer#withTiming` and `AzAnimationController#sequenceTiming()` (client side, reads lengths from the animation file).
- Molang: assignments anywhere in an expression, multiple `;`-separated statements, `{}` blocks, `return`, `loop(count, body)`, `break` and `continue`.
- Molang: binary conditional (`a ? b`), null coalescing (`??`), short-circuiting `&&` / `||`, and `true` / `false`.
- Molang: `variable.*` / `v.*` values are now stored per animatable, so each entity, block entity or item keeps its own copy. Code can read and write them through `AzAnimationContext#molangScope()`.

### Fixes
- Fixed bone hitboxes (and other bone matrix consumers) ignoring the renderer's `setScale` when the entity had a render layer that re-renders the model. Hitboxes now scale with the model on versions without the scale attribute.
- Fixed animations getting stuck after a freeze point was set on a controller (e.g. through the full `AzCommand.create` overload). The freeze point now only applies to `FREEZE_ON_FRAME` animations, so later loops, ping-pongs and sequence stages play normally.
- Fixed a freeze point shorter than the transition length holding the transition open forever.
- Fixed changing direction during a transition skipping the blend and snapping to the new animation.
- Fixed `FREEZE_ON_FRAME` pausing on the first frame when the freeze point came from the animation file.
- Fixed `REPEAT_X_TIMES` playing one extra time and ignoring the animation file's authored `repeat_times`.
- Fixed the start tick offset being multiplied by animation speed and shortening transitions. Transitions now blend towards the pose playback actually starts on.
- Molang: `pi` and `e` now resolve to their constants instead of 0.
- Molang: fixed a possible race when animation files are parsed in parallel.

### Changes
- Reduced log spam: the "Replaying queued command(s)" message from pending animation dispatches now only appears in development environments.
- `REPEAT_X_TIMES` now plays exactly `repeatXTimes` times in total, matching its documentation. Animations tuned around the old behavior will repeat once less.
- Molang expressions are now compiled by a new parser. Anything it can't handle is retried with the old parser and logged as a warning ("Falling back to the legacy parser").
- Molang: `temp.*` / `t.*` values now reset on every evaluation instead of carrying over between entities.
- Molang: assigning to `query.*`, `context.*`, `math.*` or a constant is now rejected and logged, and the expression evaluates to 0.

### Developer Notes
- Sequence events added with `event(name, tick)` keep their existing meaning (game ticks after `play()`).
- Stage-anchored events need the animation lengths they depend on. On the server, pass them through `AzSequenceTiming.builder().length(name, ticks)`. Events whose time can't be worked out are skipped with a single warning instead of firing early.
- Global variables driven from code with `MolangParser#register`, `setValue` or `setMemoizedValue` still work: animations read them until an animatable assigns its own value.
- `AzQueuedAnimation` has a new `stageProperties` component; the existing constructors are kept.
