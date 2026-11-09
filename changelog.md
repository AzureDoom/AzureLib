v1.1.9

### Additions
- Added the Simple API (`AzSimple`), an optional beginner-friendly way to animate entities, items and block entities with one renderer class and no animator, controller or dispatcher. Model, texture and animation paths are found by name, idle/walk/death and custom `when` states play automatically, and `AzSimple.play`, `loop` and `stop` trigger animations from server or client code. Triggered animations support priorities, interrupt policies and holding the last frame, and extra layers support bone masks, additive blending, weights, fades and conditions.
- Added animation-authored pose modifier weights. An animation’s pose_weights sets or keyframes the pose layer weight of IK, look-at, spring and bone-limit modifiers for the animatable playing it, with fade in/out in ticks, and fades them back to their default weight when it stops. Requires AzureLib Animator 2.4.0.

### Fixes
- N/A

### Changes
- N/A

### Developer Notes
- Authored pose weights are applied by each AzAnimationController right after its state machine updates. Fixed weights use AzPoseLayer#fadeWeight once when the animation starts, so code can still change them; weight tracks call setWeight every frame while they play.
