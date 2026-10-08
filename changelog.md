v3.1.18

### Additions
- Large groups of animated entities are now cheaper to render:
  - Bones no longer allocate a new pose every frame. Each bone used to create a new `PoseStack` pose, matrix and normal matrix every frame for every entity on screen; the pose is now saved and restored in place. This roughly halves render-thread allocation and makes garbage collection pauses about half as frequent.
  - Animations no longer look up every animated bone by name every frame. Each controller resolves its animation's bones once and reuses them until the animation or model changes.
  - Bone LOD no longer walks every bone of every nearby entity each frame when no bones are hidden.
  - Animated textures no longer use reflection, and no longer throw and catch an exception, on every render of every textured model.

### Fixes
- Fixed animation transitions stopping for every remaining bone when one bone had no snapshot or animation queue, instead of skipping just that bone.

### Developer Notes
- Added `AzModelRenderer#saveBonePose` and `restoreBonePose`, which replace `PoseStack#pushPose` / `popPose` around each bone. The entity and block entity model renderers use them; the item and armor model renderers no longer push their own pose per bone. Custom model renderers that override `renderRecursively` should do the same, since `pushPose` allocates on every call in this version.
- Added `AzBoneAnimationQueueCache#resolveQueues(AzBakedAnimation)`, which returns each bone animation's queue by index, cached per animation and model.
- `AnimatableTexture#setAndUpdate` handles AzureLib's own textures with a plain type check. For other texture classes it still calls `setAnimationFrame(int)` when they have one, but looks the method up once per class instead of on every call.
- Bone LOD now tracks which baked models currently have LOD-hidden bones, so un-hiding only runs when something was actually hidden.
