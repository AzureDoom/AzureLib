v3.2.12

### Fixes
- Fixed armor and item renderers sometimes failing to register when multiple mods registered them at the same time during parallel client setup on NeoForge. This caused armor to randomly render with vanilla/missing textures (e.g. purple armor), with different pieces affected between launches. `AzArmorRendererRegistry` and `AzItemRendererRegistry` are now thread-safe.
- Fixed `AzIdentityRegistry` having the same issue, which could cause trigger animations to silently stop working for some items. It is now thread-safe.

### Developer Notes
- On NeoForge, renderer and identity registration should be wrapped in `event.enqueueWork(...)` inside `FMLClientSetupEvent` / `FMLCommonSetupEvent`. The docs have been updated to reflect this. The registries are now safe either way, but `enqueueWork` also protects your mod on older AzureLib versions.