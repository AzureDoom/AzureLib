v1.0.3

### Additions
- N/A

### Fixes
- Fixed AzureLib items rendered in the hotbar or inventory having inverted lighting, and changing the lighting of every item rendered after them in the same slot row (blocks, spawn eggs).
- Fixed AzureLib entities rendering with dark or inverted shading when a bone is scaled non-uniformly by an animation (for example, a squash or stretch).
- Fixed bones scaled to 0 by an animation turning their child bones black.

### Changes
- N/A

### Developer Notes
- `AzItemGuiRenderUtil#renderInGui` no longer resets the item lights for the default GUI lighting, since the hotbar or container has already set them. Items using `useEntityGuiLighting()` now set and restore their lights under an identity model-view, so the light direction no longer depends on the item's GUI transform (RenderItem's Y flip and the display rotation).
- `AzBufferSource` now normalizes every vertex normal before uploading. 1.12.2 packs normals into signed bytes and does not normalize them on the GPU, so non-unit normals from a non-uniform scale overflowed and flipped. Normals that are zero-length or not finite fall back to `(0, 1, 0)`.
- `PoseStack#scale` no longer updates the normal matrix when any axis is 0, which previously filled the matrix with infinities for the bone and all of its children.
