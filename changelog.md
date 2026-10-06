v3.1.17

### Additions
- Animations are now cheaper to play, especially with many bones or long animations:
  - Each controller remembers which keyframe every bone was on last frame and continues from there, instead of searching the bone's keyframes (and allocating a new array to do it) every frame. This works the same playing forwards, reversed and in `PING_PONG`; bigger jumps like looping back to the start fall back to a binary search over keyframe times calculated once when the animation loads.
  - Sampling keyframes no longer creates any objects. Values are written straight into the bone's animation slots instead of a new `AzAnimationPoint` per axis.
  - Linear keyframes skip the easing system entirely.
  - Easing curves are built once and reused instead of being rebuilt for every keyframe every frame. Curves that don't take an argument (sine, quad, cubic, quart, quint, expo, circ) are built when AzureLib loads. Curves with an argument (back, elastic, bounce, step) are resolved once per keyframe when the animation loads, so playback doesn't look them up at all.
  - Easing math no longer boxes every value into a `Double`.
  - Keyframe event checks (sounds, particles, custom instructions), bone animation lookups and bone/keyframe hashing no longer allocate every frame.
- The auto glowing layer can now tint emissive textures. Pass a color, or a function of the render context for tints that change or pulse, straight to `AzAutoGlowingLayer`:
  - `.addRenderLayer(new AzAutoGlowingLayer<>(Color.RED))`
  - `.addRenderLayer(new AzAutoGlowingLayer<>(context -> isAngry(context.animatable()) ? 0xFFFF4040 : 0xFFFFFFFF))`
- Added a large set of Bedrock Molang queries. See the Supported Queries list in the docs for the full set and what each returns.
  - Entity state: `is_alive`, `is_moving`, `is_in_lava`, `head_is_in_water`, `is_fire_immune`, `is_invisible`, `is_silent`, `is_sneaking`, `is_sprinting`, `is_swimming`, `is_gliding`, `is_sleeping`, `is_riding`, `has_rider`, `is_leashed`, `is_tamed`, `is_sitting`, `has_gravity`, `has_collision`, `is_spectator`, `is_local_player`, `is_first_person`.
  - Movement and rotation: `vertical_speed`, `body_x_rotation`, `body_y_rotation`, `cardinal_facing_2d`, `model_scale`, `swim_amount`, `death_ticks`.
  - Items and equipment: `item_in_use_duration`, `item_remaining_use_duration`, `item_max_use_duration`, `has_head_gear`, `equipment_count`, `player_level`, `max_durability`, `remaining_durability`.
  - World: `day`, `time_stamp`, `moon_brightness`, `frame_alpha`, `client_max_render_distance`.
  - Bedrock names for existing queries: `head_x_rotation` (`head_pitch`), `head_y_rotation` (`head_yaw`), `blocking` (`is_blocking`) and `is_onfire` (`is_on_fire`).
- Added Molang query functions, which take arguments: `query.position(axis)`, `position_delta(axis)`, `movement_direction(axis)`, `is_item_equipped(hand)`, `has_armor_slot(slot)`, `armor_damage_slot(slot)`, `heightmap(x, z)`, `above_top_solid(x, z)`, `camera_distance_range_lerp(near, far)`, `all(...)`, `any(...)`, `approx_eq(...)` and `in_range(value, min, max)`.

### Fixes
- Fixed `easeInQuint` using the same curve as `easeInQuart`.
- Fixed transitions into a reversed animation blending towards the second-to-last keyframe instead of the last frame, causing a small pop when the transition ended. Transitions now blend to exactly the pose the animation starts on, easing included.
- Fixed custom easing types registered with uppercase letters in their name through `AzEasingTypeRegistry#register(String, Function)` never being found by animation files.
- Fixed `AzKeyframe#equals` treating two different keyframes as equal if their hash codes happened to match.
- Fixed `query.is_on_fire` always being 0. It was registered but never set.
- Fixed `query.moon_phase` returning a fraction that grew through the day instead of a whole number, so checks like `query.moon_phase == 4` almost never matched.
- Fixed bone texture overrides rendering with the model's main texture, and leaking into the bones rendered after them (wrong UVs or missing parts) unless every bone was given an override.
- Fixed the auto glowing layer leaving full-bright lighting on the render context, so any layer rendered after it was also full-bright.

### Developer Notes
- **Behavior change:** animations using `easeInQuint` now ease in more sharply, matching `easeOutQuint`, `easeInOutQuint` and the standard quint curve.
- Added `AzKeyframeChannel`, an immutable, baked view of one axis of keyframes with keyframe start/end times and easing curves resolved once. `locate(tick, hint)` does a cached lookup starting from a previous index, `search(tick)` does a binary search, and `transformer(index)` returns a keyframe's baked easing curve. Get them from `AzKeyframeStack#xChannel()`, `yChannel()` and `zChannel()`.
- `AzKeyframeStack` is now a final class instead of a record so it can hold its channels. Its constructors, accessors, `from`, `getLastKeyframeTime()`, `equals` and `hashCode` work the same as before. Channels are snapshots of the keyframe lists: a stack built from lists bakes them right away, and a stack made with the no-argument constructor bakes them the first time they're used, so its lists can be filled before then. Changing a list after its channel is baked has no effect on playback.
- `AzKeyframeExecutor` and `AzKeyframeTransitioner` no longer use `AzAbstractKeyframeExecutor#getAnimationPointAtTick` or `getCurrentKeyframeLocation`. Both are kept (deprecated, and now allocation-free) for compatibility, but overriding them no longer changes how animations are sampled. The new protected helpers are `prepareKeyframeCursors`, `resetKeyframeCursors`, `locateKeyframe`, `writeChannel` and `sampleValue`.
- Easing types registered with `AzEasingTypeRegistry#register(String, Function)` now cache the curves they build, once per argument value, so the function must return the same curve for the same argument. All built-in easings already do.
- Custom easing code should call `Double2DoubleFunction#get(double)` instead of `apply(Double)`, which boxes both the input and the result.
- Added `AzBoneAnimationQueue#addNextRotation`, `addNextPosition` and `addNextScale` overloads that take the target values as plain doubles. The `AzAnimationPoint` versions are kept.
- `AzAutoGlowingLayer` has new constructors taking a `Color` or a `ToIntFunction<AzRendererPipelineContext<K, T>>`, and a protected `getGlowColor(context)` hook for subclasses. The tint is an ARGB color multiplied with the renderer's own color, so the default (no tint) renders exactly as before. Tints work best on white or grey `_glowing` textures. The glow layer now also restores the context's render color after rendering, so a tint never leaks into later layers.
- **Behavior change:** `query.moon_phase` now returns a whole number from 0 to 7. Animations that relied on the old fractional value will step once per day instead of changing smoothly.
- Molang query functions can be registered with `MolangParser.INSTANCE.registerFunction(name, FunctionClass)`. Names are normalized, so animations can call them with `q.` or `query.`. Query functions that read game state should extend `ContextQueryFunction`, which reads the current `MolangQueryContext` and is never folded to a constant when animations load.
- New `MolangQueryContext` (in `core`, with no Minecraft dependencies) supplies the game state for query functions. AzureLib's implementation, `AzMolangQueryContext`, is re-bound by each animator before it evaluates, and exposes the current entity and partial tick for custom query functions.
- New `MolangParser#alias(alias, target)` makes two names share one variable.
- Function calls in Molang are now looked up with normalized names, so any registered function can be called with the short `q.`, `v.`, `t.` or `c.` prefix.
- Added `AzBoneCache#wasRotationAnimatedThisFrame`, `wasPositionAnimatedThisFrame` and `wasScaleAnimatedThisFrame`. Call them in `setCustomAnimations` to tell whether a controller moved a bone this frame, so code can add to the animated value when there is one and start from the default pose when there isn't. Adding to a bone's current value without this check makes un-animated bones drift a little further every frame.
- Added `AzEasingTypeRegistry#registerStateless(String, Double2DoubleFunction)` for easing types whose curve doesn't take an argument. The curve is built once and shared by every keyframe. The built-in argument-free easings now use it.
- Added `AzEasingType#resolveTransformer(AzKeyframe)`, which lets an easing type hand over a keyframe's curve when the animation loads. It returns `null` by default, which keeps resolving the curve at playback, so existing custom easing types are unaffected. Built-in easings only bake curves whose argument is a fixed value.
- Added `AzAnimationPoint#bakedTransformer()` and a `set` overload that carries it, plus `Constant#isFixed()`, which is `true` for shared constants from `Constant.of` that can't change.
