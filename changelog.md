v3.2.10

## Performance — Rendering Pipeline Overhaul

This release is a focused performance pass targeting the client-side rendering pipeline. The goal was to bring per-entity CPU and GC cost for complex animated models in line with vanilla mob rendering. All changes are backwards-compatible — no model files, animator classes, or existing renderer configurations require modification.

### Fixes
- Fixed registry of easing types (Credit to Collinvh for fix)
- Fixed bedrock easing interpolation having the position incorrectly in the place of the first value. (Credit to Collinvh for fix)
- Fixed broken matrix translation in `RenderUtils.translateMatrix`. The previous implementation used `Matrix4f.add()` to apply a translation vector, which is mathematically incorrect and produced wrong world-space matrix results for bone tracking. Now uses `Matrix4f.translate()` as intended.
- Fixed triple `boneTextureOverrideProvider` call per bone. The provider lambda was invoked three times per bone per frame in `getOrRefreshRenderBuffer`. Now called once, result cached locally.
- Replaced per-cube `pushPose`/`popPose` with save/restore in `renderCubesOfBone`. The pose stack entry is now captured once per bone and restored after each cube transform, eliminating N matrix stack copies per multi-cube bone.
- Added hidden bone early-out to `renderRecursively`. Hidden bones now skip all matrix work (`pushPose`, translate, rotate, scale) entirely. Children are still iterated unless `isHidingChildren()` is also true, matching Bedrock's bone visibility spec.
- Hoisted flat-cube check out of the quad loop in `renderCube`. `fixInvertedFlatCube` is only relevant for 2D plane cubes (one dimension = 0). The check is now computed once per cube before the quad loop and skipped entirely for standard 3D cubes.
- Eliminated per-vertex redundant reads in `createVerticesOfQuad`. `getTextureOverride()`, `packedOverlay()`, `packedLight()`, and normal vector components are now all hoisted outside the vertex loop. UV scale factors for the texture override path are pre-computed as floats, replacing two integer divisions per vertex with a single multiply.
- Fixed `invertAndMultiplyMatrices` allocation. Previously allocated a `new Matrix4f` on every call (called twice per tracked bone per frame). Now uses a static `INVERT_SCRATCH` buffer; one allocation per call for the returned result only.
- Eliminated per-frame `Matrix4f` heap allocations in `renderRecursively`. The two `new Matrix4f(...)` calls in the bone matrix tracking path are replaced with reusable `scratchPoseState` and `scratchLocalMatrix` instance fields.
- Fixed triple `getAdjustedTick()` call. Was called once before the bone loop, once inside the loop per bone, and once for the callback. Now called once; `currentAdjustedTick` field reused throughout.
- Fixed severe per-frame allocation spam. The previous implementation called `CubicBezierCurve.getPoints(200)` on every bezier keyframe every frame, allocating an `ArrayList` and 201 `Vector2d` objects per call. Additionally `findClosestPoints` allocated two more `Vector2d` instances and a `Vector2d[]` return on every call, and `buildBezierCurve` allocated four `Vector2d` control points per call. For a model with multiple bezier-eased bones this produced 240+ allocations per frame contributing significantly to GC pressure.
- Fixed AzArmorRendererPipeline using head for the leftArm scaling.
- Fixed custom easing type lookup when easing names use mixed or lower camel case.

### Changed
- Replaced nine `ArrayDeque<AzAnimationPoint>` queues with a flat object pool. Nine pre-allocated `AzAnimationPoint` instances are reused every frame via `set()`. A `boolean[9]` presence array replaces queue state. This eliminates the dominant source of per-frame heap allocation in the animation pipeline — previously ~180 record allocations per animated bone per frame.
- Removed `LinkedList` usage. Previously used for all nine queues before the `ArrayDeque` interim; now eliminated entirely.
- Converted from `record` to mutable `final class`. All record accessor names preserved for API compatibility. New `set(...)` method enables in-place reuse by the pool in `AzBoneAnimationQueue`.
- Converted from `record` to mutable `final class`. New `set(T, double)` method allows the single scratch instance on `AzAbstractKeyframeExecutor` to be truly reused end-to-end with zero allocation.
- Replaced linear keyframe scan with binary search. `getCurrentKeyframeLocation` previously walked all keyframes from the start (O(n)). Now builds a cumulative end-time array and binary searches it (O(log n)). Meaningful for animations with many keyframes per bone.
- Add overload method for per bone Texture/Render Type to support the entity as well.
- Replaced per-frame `DoubleSupplier` lambda allocation for `ANIM_TIME`. A stable `animTimeSupplier` field closes over `currentAdjustedTick`; `setMemoizedValue` receives the same object reference every frame.
- Replaced per-frame `DoubleSupplier` lambda allocation for `ANIM_TIME`, matching the fix in `AzKeyframeExecutor`.
- Replaced four per-frame `DoubleSupplier` lambda/method-reference allocations in `applyMolangQueries`. `LIFE_TIME`, `ACTOR_COUNT`, `TIME_OF_DAY`, and `MOON_PHASE` now use stable supplier fields that read mutable double fields set before each call.
- Updated to use pool-based `pollRotX/Y/Z`, `pollPosX/Y/Z`, `pollSclX/Y/Z` accessors from the new `AzBoneAnimationQueue` pool design.
- Replaced point-scan lookup with binary search on curve parameter `t`. Rather than sampling the curve at 200 evenly-spaced points and scanning for the nearest two, the curve is now evaluated via binary search on `t` (20 iterations), exploiting the monotonically increasing x-coordinate of well-formed animation curves. Converges to the same result with zero heap allocation.
- Added `translateMatrixInPlace(Matrix4f, Vector3f)`. Mutates the matrix directly, for call sites that already own the matrix and don't need the original preserved.
- Removed `getPoints(int)`. No longer needed now that `evaluateAtTime` handles all call sites.

### Added
- Added hidden bone early-out matching `AzModelRenderer`, before `pushPose` and all transform calls.
- Added scratch `AzKeyframeLocation` field. The single instance is reused across all `getAnimationPointAtTick` calls via `.set()`, eliminating one record allocation per axis per bone per frame.
- Added `prepareFrame()` method. Clears all pool presence flags before the keyframe executor writes new values each frame, preventing stale values from partially-animated prior frames bleeding through.
- Added `boneAnimationQueueCache.prepareFrame()` call before `stateMachine.update()` in the per-frame update order.
- Added reusable scratch fields. Four `Vector2d` fields replace the per-call control point allocations in `buildBezierCurve`, and two scratch `Vector2d` fields are passed into `CubicBezierCurve.evaluateAtTime` for the binary search iterations.
- Added `evaluateAtTime(double, Vector2d, Vector2d)`. Zero-allocation curve evaluation using binary search on `t`, accepting caller-provided scratch vectors. Replaces the allocation-heavy `getPoints(int)` approach.
- Configures bone hierarchy depth culling and animation tick-rate reduction, each with independent distance thresholds.
  - `AzLodConfig.DISABLED` — no LOD applied.
  - `AzLodConfig.DEFAULT` — automatically applied to all entity renderers unless overridden: bone LOD at 20 blocks (depth ≤ 3), animation LOD at 28 blocks (every 2 ticks).
- Per-entity runtime manager. Walks the bone tree and calls `setHidden(true)` on bones exceeding the depth budget. Tracks a `lodHidden` flag so LOD-hidden bones can be restored when the entity returns to close range without affecting model-authored hidden bones.
- Returns a `shouldAnimate` boolean to gate the full animation pipeline update, implementing tick-rate reduction for distant entities.
- Added `withLodConfig(AzLodConfig)` builder method. Defaults to `AzLodConfig.DEFAULT` when not specified.
- Wires `AzLodManager` into the render loop. LOD manager is instantiated per entity UUID and updated each frame before the pipeline runs. When `shouldAnimate` is false the model renders at its last computed pose, skipping all animation controller work.
- Added `lodHidden` field with `getLodHidden()` / `setLodHidden(boolean)` accessors. Distinguishes LOD-driven visibility from model-authored `dontRender` flags. Propagated through `deepCopy()`.