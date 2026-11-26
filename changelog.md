v1.1.10

### Additions
- Added `AzSpringBone.Builder#maxDeflection(degrees)` to cap how far a spring may turn a bone away from its animated direction in one solve. Defaults to 180 (no cap).
- Added smooth shading for Bedrock `poly_mesh` geometry. Mesh parts listed in a bone's `azurelib:mesh_parts` with `"shading": "smooth"` now get averaged vertex normals instead of one flat normal per face, so curved meshes (spheres, rings, bearings) are lit with smooth gradients instead of hard bands. Faces only smooth with neighbors in the same part whose angle is within `smooth_angle` (degrees, default 60), so hard edges such as rims stay sharp. Parts set to `"shading": "flat"`, and meshes without `azurelib:mesh_parts`, keep using their exported normals.

### Fixes
- Fixed spring bone chains flipping or twisting a bone by 100+ degrees in a single frame when its particle ended up far from, or nearly opposite, the animated bone direction (for example while ground collision or colliders hold a chain away from its animation). Between 90 and 150 degrees the rotation axis now blends smoothly from the shortest-arc axis into the axis used the previous frame, and above 150 degrees it keeps the previous frame's axis, so the solve stays continuous instead of jumping between solutions.
- Fixed hitbox offscreen posing re-posing visible entities at partialTick 0 when the client's gameTime jumped (server time packets in multiplayer, or several client ticks in one frame). An entity now also counts as rendered if it was captured during rendering within the last 250 ms, so its pose modifiers and spring bones no longer get interleaved partialTick 0 evaluations.
- Fixed spring bones and other procedural pose modifiers being evaluated multiple times per frame when an entity is rendered in a GUI or HUD overlay (such as RPG-Hud's entity inspection preview). GUI renders now reuse the existing animated bone pose without advancing animation controllers, updating spring bone simulations, or affecting animation LOD state. GUI renders are also excluded from hitbox capture, preventing temporary entity transformations used by previews from interfering with world-space hitboxes.

### Changes
- N/A

### Developer Notes
- `azurelib:mesh_parts` is now parsed into a new `MeshPart` record (name, smooth, smoothAngle, start, count), and `PolyMesh` has a new `parts` component. The original 5-argument `PolyMesh` constructor is kept and defaults to no parts, and `PolyMesh#withParts` attaches them.
- Smoothing happens when `MeshPolygons` bakes the mesh, so it costs nothing per frame. Positions within 0.0001 blocks of each other are treated as the same vertex, so UV seams don't show as lighting seams.
- - Added `AzRenderContext#isGuiRender()` to detect GUI rendering passes using the active projection type or projection matrix, depending on the Minecraft version. Entity renderers use this to prevent secondary GUI rendering passes from modifying persistent animation, procedural pose, spring bone, LOD, and hitbox state. This improves compatibility with third-party HUD mods and entity previews without requiring mod-specific integration.
