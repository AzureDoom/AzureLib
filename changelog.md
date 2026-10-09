v3.2.19

### Additions
- Animation LOD now works. Past `animLodDistance`, entities update their animations once every `animLodTickInterval` ticks and hold their pose in between, instead of animating every frame.

### Fixes
- Fixed animation LOD having no effect. The LOD decision was computed but ignored, so distant entities still animated every frame.
- Fixed models and animations that could briefly go missing during a resource reload (F3+T or changing resource packs in-world). The caches were filled in place on a background thread while the game was still rendering from them; they are now loaded separately and swapped in once the reload finishes.
- Fixed models and animations from removed resource packs staying loaded until restart.
- Fixed one geo model that failed to bake stopping every remaining model from loading. The broken model is now logged and skipped.
- Fixed a crash when an animation file `includes` a file that is missing or failed to load. The animation is now logged as missing instead.
- Fixed bone LOD being able to hide bones on the shared model template before an entity had its own copy, which newly spawned entities could then inherit.
- Fixed per-entity LOD state never being released after entities unloaded.

### Changes
- Changed LOD to be DISABLED by default when a render doesn't call `withLodConfig`.

### Developer Notes
- `AzResourceCache#loadResources` now returns `CompletableFuture<Map<ResourceLocation, T>>` and no longer takes a `BiConsumer`. `AzBakedAnimationCache#loadAnimations` and `AzBakedModelCache#loadModels` return the loaded map; publish it with the new `apply(Map)`. Namespaces in `EXCLUDED_NAMESPACES` are now skipped before loading instead of after.
- Added `AzEntityRenderer#shouldAnimateThisFrame()` and the overridable `updateLod(...)`. Custom entity model renderers that override `render` should check `shouldAnimateThisFrame()` before calling `handleAnimation`, or animation LOD won't apply to them.
- LOD is opt-in: renderers that never call `withLodConfig` don't use bone or animation LOD. Pass `AzLodConfig.DEFAULT` for the built-in preset (one animation update every 2 ticks past 48 blocks; bones deeper than depth 3 hidden past 40 blocks) or build your own with `AzLodConfig.builder()`.
