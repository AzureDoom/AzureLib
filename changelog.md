v1.0.4

### Additions
- N/A

### Fixes
- Fixed auto-glowing layers using incomplete full-bright lighting by switching to `LightTexture.FULL_BRIGHT`.

### Changes
- N/A

### Developer Notes
- `AzAutoGlowingLayer#getPackedLight` now returns `LightTexture.FULL_BRIGHT` instead of `LightTexture.FULL_SKY`.
