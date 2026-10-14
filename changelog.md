v3.1.21

### Additions
- N/A

### Fixes
- Fixed auto-glowing layers using incomplete full-bright lighting by switching to `LightTexture.FULL_BRIGHT`.
- Fixed glowmask rendering on block entities with Iris shaders by enabling the lightmap state for AzureLib's custom emissive render type.

### Changes
- N/A

### Developer Notes
- `AzAutoGlowingLayer#getPackedLight` now returns `LightTexture.FULL_BRIGHT` instead of `LightTexture.FULL_SKY`.
- `AzAbstractTexture` now adds `RenderStateShard.LIGHTMAP` to the `az_glowing_layer` composite render state; the existing overlay and transparency states remain unchanged.
