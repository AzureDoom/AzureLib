v4.0.8

### Additions
- Added the `math.min_angle(value)` Molang function, which wraps an angle in degrees into the range [-180, 180).
- Added `AzProfiler` stage hooks (`mod.azure.azurelib.profiling`) for external profiling tools. No cost when no listener is installed.

### Fixes
- Fixed `math.copy_sign`, `math.sign`, `math.inverse_lerp` and all 30 `math.ease_*` Molang functions failing to resolve. They were only registered under their bare names, so the `math.`-prefixed forms used by Blockbench and Bedrock animations parsed to 0.
- Fixed the entity and block entity geometry caches never hitting: their keys included each instance's position. Bone visibility is now part of the entity cache key.
- A missing glowmask no longer draws the whole model a second time with an empty texture.

### Performance
- Vertex recordings are sized from the previous frame, handed to submit without copying, and pooled per frame instead of allocated per animatable.
- The auto-glowing layer reuses the main model pass instead of re-rendering the model (when no bone overrides are in use), and only submits quads whose UVs cover glowing pixels in the glowmask, including animated glowmasks.
- The entity and block entity geometry caches switch themselves off per type when they aren't getting hits, and probe periodically.
- Zero-area faces (the edges of flat cubes) are no longer baked or rendered.
- Cube pivot rotations are baked once at load instead of being applied to the pose stack for every cube every frame.
- Entity and block entity renderers reuse their extraction pose stack instead of allocating one per render.
- Flat-cube normal fixes are precomputed per cube; bone, cube and layer loops no longer allocate iterators.
- Glowmask texture paths are cached instead of rebuilt every frame.
- Removed a per-quad texture size lookup for bones without a texture override.

### Developer Notes
- The fixed Molang functions are now registered only under their `math.` names, matching the existing classic functions. Expressions that called them without the prefix (e.g. `ease_in_quad(0, 1, t)`) must be updated to use `math.ease_in_quad(0, 1, t)`.
- `AzCube` has two new record components, `transform` and `normalFlips`. The six-argument constructor still works; code that deconstructs the record with a pattern needs updating.
- `AzModelRenderer#renderCube` no longer modifies the pose stack, and `renderCubesOfBone` no longer pushes and pops it per cube. Overrides of `renderCube` that change the pose stack must push and pop it themselves.
- `AzEntityGeometryCache.CacheKey` and `AzBlockEntityGeometryCache.CacheKey` no longer have position components.
- `AzBufferSource` recordings are only valid until the end of the frame; their arrays are reused after that.
- New: `AzBufferSource#mirror` (with an optional `QuadFilter`), `AzGlowCoverage`, and `AzAutoGlowingLayer#canReuseModelPass` / `#glowFilter` for subclasses that draw their glow differently.
