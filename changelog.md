v4.0.4

### Additions
- N/A

### Fixes
- Fixed auto-glowing layers using incomplete full-bright lighting by switching to `LightTexture.FULL_BRIGHT`.
- Fixed glowmask rendering on block entities with Iris shaders by introducing a custom lightmap-enabled render type.

### Changes
- N/A

### Developer Notes
- `AzAutoGlowingLayer#getPackedLight` now returns `LightTexture.FULL_BRIGHT` instead of `LightTexture.FULL_SKY`.
- `AzAbstractTexture` now uses a custom `az_glowing_layer` render type built with `RenderSetup` and `RenderPipelines.ENTITY_TRANSLUCENT`, enabling lightmap and overlay support.
