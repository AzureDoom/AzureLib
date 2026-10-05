v4.0.4

### Additions
- Added reverse animation playback. Animations can be reversed per stage (`withShouldReverse(true)` / `AzSequence#playReversed`) or per controller via the existing set reverse AzCommands.
  - Reversed animations play their keyframes, Molang `query.anim_time` and keyframe events (sounds, particles, custom instructions) backwards.
  - Transitions into a reversed animation now blend towards its last frame instead of its first.
  - Reversing a controller mid-animation now turns the animation around from its current pose instead of snapping to the mirrored frame.
- Added the `PING_PONG` play behavior (`AzPlayBehaviors.PING_PONG` / `AzSequence#pingPong`), which plays an animation forward then backward indefinitely with seamless turnarounds.
- Animation files can now define their own play behavior, so it no longer has to be set through AzCommands:
  - `"loop"` now accepts `true`, `"hold_on_last_frame"`, `"ping_pong"`, `"repeat_x_times"` and `"freeze_on_frame"`.
  - `"repeat_times"` sets the play count for `repeat_x_times`.
  - `"freeze_at"` sets the freeze point in seconds for `freeze_on_frame`.
  - These can all be set in Blockbench with AzureLib Animator 2.2.0+.
- Added `AzPlayBehaviors.AS_AUTHORED` and `AzSequence#authored` to explicitly play an animation with the behavior from its animation file.
- Molang expressions are now simplified further when animations load. Parts that only use constants (including `math.pi`) are calculated once instead of every frame, and no-op math like `x * 1` or `x - 0` is removed.
- Repeated numbers in animation files (keyframe values, rotations, easing arguments) now share one object per value instead of one per occurrence, reducing memory use for large animation sets.
- Re-added armor trim support for 26.3 with `AzArmorTrimLayer`, rebuilt on Minecraft's new paletted trim textures. Trims are colored by the trim material's palette at runtime, and decal patterns render like vanilla's.

### Fixes
- Fixed `REPEAT_X_TIMES` playing one extra time for counts above 1 (2 played 3 times). The repeat count is now the total number of plays.
- Fixed the `loop` field in animation files being parsed but never used.
- Fixed the controller reverse property and per-stage reverse settings being ignored.
- Fixed per-stage properties other than the play behavior being dropped when an animation was queued, which prevented stage-level reversing.
- Fixed Molang `query.anim_time` during transitions using the transition's progress instead of the time of the pose being blended to.
- Fixed animation files exported by older AzureLib Animator versions with `"loop": "once"` or `"loop": "hold"` not being recognized.
- Fixed `math.pi` evaluating to `0` in Molang expressions. It was registered as a function that didn't exist instead of a constant.
- Fixed `AzCommand#create` / `createRoot` ignoring their `transitionLength` parameter.

### Developer Notes
- **Behavior change:** stages queued without a play behavior (e.g. `AzAnimationSequenceBuilder#queue(name)`) now use the animation file's `loop` value. Files without a `loop` field still play once. Previously `loop` was ignored, so animations exported with `"loop": true` and queued without a behavior will now loop.
- **Behavior change:** `REPEAT_X_TIMES` / `setRepeatAmount` now means total plays. Values of 2 or more play one fewer time than before; 1 or less plays once.
- A play behavior or repeat count / freeze point set on the code side (stage or AzCommand) always takes priority over the animation file.
- New API:
  - `AzAnimationController`: `isPlayingReversed()`, `sampleTick()`, `reverseInPlace()`, `setReversing(boolean)`, `flipDirection()`, `effectiveRepeatXTimes()`, `effectiveFreezeTickOffset()`.
  - `AzAnimationControllerTimer#seek(double)`.
  - `AzKeyframeCallbackHandler`: `handle(animatable, tick, reversed)`, `resync(tick, reversed)`.
  - `AzQueuedAnimation#reverseOverride()`, `AzBakedAnimation#defaults()`, new `AzAnimationDefaults` record.
- Animation progress (`getAdjustedTick()`) always counts forward regardless of direction; use `AzAnimationController#sampleTick()` for the tick actually being sampled.
- The previous `AzQueuedAnimation` and `AzBakedAnimation` constructors are kept for compatibility.
- **Behavior change:** animations using `math.pi` were previously getting `0` and will now animate with the correct value.
- Constant pool: `Constant.of(double)` returns a shared, immutable constant per value (calling `set()` on one throws). `new Constant(...)` still creates a mutable one. `MolangParser#constantValue(double)` shares constant `MolangValue`s the same way, and `MolangParser#clearExpressionCache()` now also clears both pools.
- `MathBuilder#parse(String)` now returns an already simplified tree. Simplification never changes the value an expression returns.
- Added `MathBuilder#registerConstant(String, double)` for named values that are folded at parse time, unlike `Variable`s. `PI` and `E` are now registered as constants (and still as variables for compatibility).
- `Operation` is now a plain enum with a single `calculate` implementation, which `Operator` uses for evaluation. Added `Operation#fromSign(String)`. `Operation.OPERATORS` is now unmodifiable.
- **Behavior change:** `AzCommand#create(controller, animation)` and `create(controller, animation, behavior)` no longer reset the controller's speed, start offset, freeze point, repeat amount and reverse direction. Settings from the controller builder or an earlier command now carry over.
- **Behavior change:** `AzCommand#create(controller, animation)` now plays the animation with the loop mode from its animation file (`AzPlayBehaviors.AS_AUTHORED`) instead of always `PLAY_ONCE`. Animations exported with `"loop": true` (or any other loop mode) will now play that way; files without a `loop` field still play once. Pass `AzPlayBehaviors.PLAY_ONCE` explicitly to keep the old behavior.
- `AzArmorTrimLayer` (26.3):
  - Trim textures are no longer registered in the `armor_trims` atlas (removed by Minecraft in 26.3). Instead, each grayscale trim texture needs a `.png.mcmeta` with `{"palette": {"base_palette": "minecraft:trim_base"}}`, and must be the same size as the armor texture.
  - Texture ids are relative to `textures/` (e.g. `yourmod:trims/your_armor_trim`) and use `Identifier` instead of `ResourceLocation`.
  - **Breaking:** custom texture functions now return only the texture, without the material suffix (`_gold`, `_iron`, ...), since the color comes from the palette.
  - New `AzArmorTrimLayer(textureFn, paletteFn)` constructor to choose the palette per trim, e.g. `TrimMaterials.Palette.GOLD_DARKER.id()` for vanilla-style darker trims. `AzArmorTrimLayer.MATERIAL_PALETTE` is the default (the trim material's own palette).
  - See the updated Armor Trims docs page for setup and migration.