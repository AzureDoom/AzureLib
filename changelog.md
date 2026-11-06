v1.1.6

### Additions
- Added Blockbench polygon mesh rendering. Meshes inside a bone render and animate with it, like cubes. Requires AzureLib Animator 2.4.0.
- Added skinned meshes with smooth deformation. Meshes inside a Blockbench armature bend with the armature's bones using linear blend skinning (up to four weights per vertex), matching the Blockbench preview. Armature bones export as regular bones, so animations, procedural poses, IK, sockets and hitboxes work on them.
- Bedrock `poly_mesh` geometry from other tools now loads and renders, including mixed triangle and quad polygons and the `tri_list` / `quad_list` shorthands.
- Bone hitboxes that fit their bone's geometry now include rigid meshes.

### Fixes
- Fixed `poly_mesh` geometry failing to load. Positions, normals and UVs were read as flat arrays, and polygons with different corner counts overflowed the polygon array.

### Changes
- N/A

### Developer Notes
- `PolyMesh` now reads `positions`, `normals` and `uvs` as `double[][]`, and `PolysUnion.union()` is now `int[][][]` with one entry per polygon corner (`[position, normal, uv]`).
- `Bone` has a new `skinnedMeshes` component, read from the bone's `azurelib:skinned_meshes` list.
- Added `AzBakedModelFactory#constructMesh` and `#constructSkinnedMesh`. They have default implementations, so custom factories only need to call them from `constructBone`.
- `AzBone` has `getMeshes()`, `getSkinnedMeshes()` and `getSkinBindings()`. Skin bindings are resolved per `AzBakedModel` instance, so deep-copied models skin against their own bones.
- Meshes render from `AzModelRenderer#renderCubesOfBone`, through the new overridable `renderMesh` and `renderSkinnedMesh`. Renderers that call `renderCubesOfBone` get mesh support automatically, and quad filters and per-bone texture overrides apply to meshes too.
