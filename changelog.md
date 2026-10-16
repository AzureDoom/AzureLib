v1.0.2

### Additions
- N/A

### Fixes
- Fixed textures without an animation-frame method repeatedly retrying a failed reflective lookup every frame. The failed lookup is now cached, so these textures no longer throw an exception on every render.

### Changes
- Improved animation performance on models with many bones. The bone cache now walks bones in a fixed array and skips any bone that has fully eased back to its bind pose, so its cost scales with the animated bones instead of the whole model.
- Bones scaled to zero on every axis, along with everything under them, are no longer rendered, instead of being drawn as collapsed geometry.
- Reduced rendering overhead in the buffer source. Vertices are now written once, already in their final draw layout, with lighting, hurt tint and glint colour applied up front, so drawing a batch is a single bulk copy instead of a second pass over every vertex. Normals are normalized once per quad instead of once per vertex, and glint batches are filled once for both passes.
- Bone rotation and scaling no longer allocate matrices. Each bone's rotation is now applied as one combined rotation instead of up to three separate ones, which removes nearly all of the garbage rendering used to create per frame.

### Developer Notes
- `AzCachedBoneUpdateUtil` has new overloads that take an `AzBoneSnapshot` directly and return whether the channel is at rest. The existing `Map` overloads still work and delegate to them.
- Added `AzBoneCache#invalidateBoneArrays()`. Call it if you replace or remove entries in `getBoneSnapshotsByName()` directly; changing a snapshot's values doesn't need it.
- While a bone is scaled to zero on all three axes, its descendants don't update their tracked matrices and per-bone render layers aren't called for that subtree.
- Added `PoseStack#rotateZYX(rz, ry, rx)`, which applies the same rotation as calling `mulPose` for Z, then Y, then X, in a single step. `RenderUtils.rotateMatrixAroundBone` and `rotateMatrixAroundCube` now use it.
- `PoseStack#scale(1, 1, 1)` is now a no-op.
- Added in-place `Matrix4f#multiplyWithScale` and `Matrix4f#multiplyRotation`, and `Matrix3f#mulScale` and `Matrix3f#mulRotation`. `Matrix4f#multiply(Quaternion)` and `Matrix3f#mul(Quaternion)` now work in place instead of allocating a temporary matrix.
- `AzBufferSource`'s public API is unchanged; custom `VertexConsumer` usage needs no updates.
