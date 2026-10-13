v1.0.5

### Additions
- N/A

### Fixes
- Fixed AzureLib items rendered in the hotbar or inventory having inverted lighting, and changing the lighting of every item rendered after them in the same slot row (blocks, spawn eggs).
- Fixed AzureLib entities rendering with dark or inverted shading when a bone is scaled non-uniformly by an animation (for example, a squash or stretch).
- Fixed bones scaled to 0 by an animation turning their child bones black.
- Fixed items rendered in the hotbar or inventory after an AzureLib item turning darker on 1.7.10, because the AzureLib item's render was switching off the GUI item lighting.

### Changes
- N/A

### Developer Notes
- `AzItemGuiRenderUtil#renderInGui` no longer resets the item lights for the default GUI lighting, since the hotbar or container has already set them. Items using `useEntityGuiLighting()` now set and restore their lights under an identity model-view, so the light direction no longer depends on the item's GUI transform (RenderItem's Y flip and the display rotation).
- `AzBufferSource` now normalizes every vertex normal before uploading. 1.12.2 packs normals into signed bytes and does not normalize them on the GPU, so non-unit normals from a non-uniform scale overflowed and flipped. Normals that are zero-length or not finite fall back to `(0, 1, 0)`.
- `PoseStack#scale` no longer updates the normal matrix when any axis is 0, which previously filled the matrix with infinities for the bone and all of its children.
- On 1.7.10, `AzItemRendererAdapter` now saves the GL lighting state with `glPushAttrib(GL_ENABLE_BIT | GL_LIGHTING_BIT)` before an `INVENTORY` render and restores it afterward. Previously it ended with `RenderHelper.disableStandardItemLighting()`, which also turned off `GL_LIGHT0`, `GL_LIGHT1` and `GL_COLOR_MATERIAL`. Forge only re-enables `GL_LIGHTING` after an `IItemRenderer`, so every slot rendered afterward was left with ambient lighting only.
