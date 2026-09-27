v3.2.13

### Additions
- Added `AzSequence`, a fluent API for chaining animations into a single reusable sequence. Stages can be added with `play`, `loop`, `hold`, or `then(name, behavior)`, using either a chain style (`AzSequence.create().then(...)`) or a builder style (`AzSequence.builder()...build()`). Adding a stage after a looping or holding stage now throws an error instead of silently creating a stage that can never play.
- Added timed events to `AzSequence` via `event(name, tick)`, delivered by the new `AzSequencePlayer`. Events are counted in ticks from the start of the sequence and can be handled server-side, e.g. to deal damage on a specific frame of an attack.
- Added `AzCommand.create(controller, AzSequence)`, `AzCommand.createRoot(AzSequence)`, and `playSequence` overloads on the controller and root command builders that accept an `AzSequence`.
- Added controller layering, so several controllers can animate the same bone at once instead of the last one fully replacing the others. Controllers are layered in the order they are added, and each can be configured with:
    - a weight from 0 to 1 (`setWeight`), controlling how strongly its animation is applied;
    - a blend mode (`setBlendMode`): `AzBlendMode.OVERRIDE` blends toward its animation, while `AzBlendMode.ADDITIVE` adds its movement on top of the layers below, e.g. breathing or recoil over a walk cycle;
    - a bone mask (`setBoneMask`), limiting it to certain bones and their children with `AzBoneMask.only(...)` or `AzBoneMask.except(...)`, e.g. an upper-body aiming layer.
- Added `AzAnimationController.fadeWeight(weight, ticks)` to fade a layer in or out smoothly instead of switching instantly.
- Added `setWeight` and `fadeWeight` to `AzControllerCommandBuilder`, so the server can change a controller's weight on clients, e.g. `AzCommand.controllerBuilder().fadeWeight("aim", 1F, 5F).build()`.

### Performance
- Molang expressions are now cached when animations load, so identical keyframe expressions share one parsed tree instead of each being parsed separately. In benchmarks, animation loading is up to 31× faster and allocates about 97% less.
- Molang no longer allocates any memory per frame. Previously every animated entity created around 830 bytes of garbage each frame from re-binding its queries.
- Molang expressions are optimized once when they load: constant parts are pre-calculated, parentheses add no extra work, and single-statement expressions are evaluated directly. Animators also look up their queries once instead of by name every frame. In benchmarks, Molang costs about 34% less per frame with 1000 animated mobs.

### Fixes
- Fixed multi-stage animation sequences stopping after the first stage. `PLAY_ONCE` and `REPEAT_X_TIMES` now move on to the next queued animation instead of stopping the controller. Previously, later stages only played if the command was re-sent every tick.
- Fixed a one-frame flash of the previous animation's first frame when moving between stages of a sequence.
- Fixed re-sending a sequence that had already finished replaying only its last stage. It now restarts from the beginning.
- Fixed cancelling a controller mid-sequence starting the next queued stage instead of stopping.
- Fixed `AzControllerCommandBuilder.play()` sometimes using the wrong play behavior. Animation property `with*()` methods were modifying the shared `EMPTY`/`DEFAULT` instances, so settings leaked between unrelated commands and controllers.
- Fixed transition length not being applied when sent from the server.
- Fixed `REPEAT_X_TIMES` sharing a single repeat counter across every controller using it, which caused animations to repeat the wrong number of times when several played at once.
- Fixed Molang `==`, `<=` and `>=` never working. Any expression using them failed to parse and evaluated to 0.
- Fixed Molang operator precedence. Comparisons and `&&`/`||` bound tighter than arithmetic, so `2 * 3 > 5` was read as `2 * (3 > 5)`; `&&` and `||` had equal precedence; and chains mixing `^` with other operators could calculate in the wrong order. Precedence now follows Molang: `||`, `&&`, `== !=`, `< <= > >=`, `+ -`, `* / %`, `^`.
- Fixed Molang `-(...)` after an operator, e.g. `2 * -(q.anim_time)`, failing to parse and evaluating to 0.
- Fixed Molang assignments. Assigning a variable in the first statement of an expression (`v.x = 6`) failed, and later statements never read the assigned value back (`v.x = 6; return v.x + 1` returned 1). Variables assigned inside an expression also no longer leak into the global variable list.
- Fixed Molang `return` not ending the expression. Statements after a `return` still ran and replaced its result.
- Fixed `query.item_is_enchanted` returning 1 for items that are not enchanted.
- Fixed `query.item_current_durability` returning NaN for items that cannot take damage. It now returns 0.
- Fixed living-entity queries such as `query.health` and `query.limb_swing` reading values from a previously animated entity when used on a non-living entity. They now return 0.
- Fixed a Molang expression that failed to parse logging an error every frame for every keyframe using it. The error is now logged once when the animation loads.
- Fixed Molang short prefixes other than `q.` not being treated as aliases. `v.`, `t.` and `c.` now refer to the same variables as `variable.`, `temp.` and `context.`, so `v.speed` and `variable.speed` are one variable.
- Fixed `math.hermite_blend` only returning whole numbers. It rounded its input up and its result down, so it jumped straight from 0 to 1 instead of following the smooth curve 3t² − 2t³.
- Fixed glowmasks not working on animated textures when another mod that replaces animated textures, such as GeckoLib, is installed. The glow layer showed every frame of the texture squashed together, and the glowing parts stayed visible on the base texture.
- Fixed animated textures losing their glowmask after a resource reload (F3+T or changing resource packs).
- Fixed animated textures leaking memory every time they were loaded or reloaded.
- Fixed textures that gain an animation from a resource pack not animating until the game was restarted.

### Developer Notes
- `AzAnimationProperties` and `AzAnimationStageProperties` are now immutable. Their `with*()` methods always return a new instance, so make sure you use the returned value; calling `with*()` without assigning the result no longer has any effect.
- Custom `AzPlayBehavior`s that end an animation should call `AzPlayBehaviors.advanceOrStop(context)` instead of `context.stateMachine().stop()`, so that the remaining stages of a sequence still play.
- `AzSequence` event ticks are not scaled by animation speed, and the controller's transition length is added before each stage on the client. Account for both when lining events up with specific keyframes.
- The Molang fixes change what some existing animations evaluate to, mainly those using comparisons, `&&`/`||`, `^`, assignments or `return`. Expressions that only use `+ - * / %` evaluate exactly as before. If an animation was tuned around the old behavior, re-check it.
- Parsed Molang trees are now shared between every keyframe with the same expression. Don't modify a `MolangValue` returned by the parser.
- To bind a query every frame, prefer a `MolangVariableRef` over `MolangParser.setValue`/`setMemoizedValue` by name. It resolves the variable once and follows `MolangParser.register` if the variable is replaced.
- `temp.`/`t.` Molang variables are now always scoped to their expression.
- Controllers with the default settings (weight 1, `OVERRIDE`, no bone mask) behave exactly as before layering was added, so existing animators need no changes.
- `setCustomAnimations` runs after all controllers have been layered, so values set there still replace the animated pose. Add to a bone's current value to layer code-driven movement on top instead.
- Weight fades follow the animator's clock: they run smoothly between game ticks, pause with the game, and are not affected by animation speed. A controller's weight is kept when a new animation or sequence is played on it.
- Layering adds a new network action (`controller/set_weight`), so clients and servers need to be on the same AzureLib version.
- `MolangParser.register` stores variables under the expanded name, so registering `v.foo` and reading `variable.foo` (or the reverse) now reach the same variable. The registered object's `getName()` is left as you passed it.
- New API: `MolangParser.parseCached`, `parseUncached`, `clearExpressionCache` and `expressionCacheSize`; `LazyVariable.setMemoized`; `MolangVariableRef`; `IValue.simplify()` (a default method, so existing implementations are unaffected); and `MolangCompoundValue.compact()`. `MolangParser.parseOneLine(String, MolangCompoundValue)` is deprecated, and the unused `HermiteBlend.random` field has been removed.
- New layering API: `AzBlendMode`, `AzBoneMask`, `AzWeightFade` and `AzControllerSetWeightAction`; `setWeight`, `setBlendMode` and `setBoneMask` on `AzAnimationControllerBuilder`; `weight`, `setWeight`, `fadeWeight`, `isFadingWeight`, `blendMode`, `setBlendMode`, `boneMask` and `setBoneMask` on `AzAnimationController`; and `AzBoneCache.currentFrame()`. `AzBoneAnimationUpdateUtil` and `AzBoneAnimationQueueCache.update` gained overloads that take a weight and blend mode; the existing signatures still work and apply at full weight.
- When a texture has an AzureLib glowmask, AzureLib now uses its own animated texture for that texture path, replacing one registered by another mod. A GeckoLib renderer sharing that exact texture path will no longer animate it, so give each library's renderers their own textures.