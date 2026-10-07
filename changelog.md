v3.1.17

### Additions
- Added the `math.min_angle(value)` Molang function, which wraps an angle in degrees into the range [-180, 180).
- Added `AzProfiler` stage hooks (`mod.azure.azurelib.profiling`) for external profiling tools. No cost when no listener is installed.

### Fixes
- Fixed `math.copy_sign`, `math.sign`, `math.inverse_lerp` and all 30 `math.ease_*` Molang functions failing to resolve. They were only registered under their bare names, so the `math.`-prefixed forms used by Blockbench and Bedrock animations parsed to 0.

### Performance
- The auto-glowing layer only draws quads whose UVs cover glowing pixels in the glowmask, including animated glowmasks, and skips the pass entirely when the glowmask has nothing visible.
- Zero-area faces (the edges of flat cubes) are no longer baked or rendered.
- Cube pivot rotations are baked once at load instead of being applied to the pose stack for every cube every frame.
- Flat-cube normal fixes are precomputed per cube; bone, cube and layer loops no longer allocate iterators.
- Glowmask texture paths are cached instead of rebuilt every frame.

### Developer Notes
- The fixed Molang functions are now registered only under their `math.` names, matching the existing classic functions. Expressions that called them without the prefix (e.g. `ease_in_quad(0, 1, t)`) must be updated to use `math.ease_in_quad(0, 1, t)`.
- `GeoCube` has two new record components, `transform` and `normalFlips`. The six-argument constructor still works; code that deconstructs the record with a pattern needs updating.
- `AzModelRenderer#renderCube` no longer modifies the pose stack. Overrides of `renderCube` that change the pose stack must push and pop it themselves.
- New: `AzQuadFilter`, `AzRendererPipelineContext#quadFilter` / `#setQuadFilter`, `AzGlowCoverage`, and `AzAutoGlowingLayer#glowFilter` for subclasses that draw their glow differently.
