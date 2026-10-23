v3.2.0

### Additions
- Added bone hitboxes. Entities can now define hitboxes that follow bones in their model, rotate with them, and stay in sync with whatever animation is playing. Implement `AzHitboxHolder` and return an `AzHitboxSet` to use them.
- Hitboxes can fit a bone's cubes automatically or use an explicit size, offset and inflate in Blockbench pixels, and several hitboxes can share one bone.
- Hitboxes have three roles.
    - `COMBAT` hitboxes take crosshair targeting, projectiles and damage.
    - `INTERACT` hitboxes can be right-clicked to trigger custom logic without affecting combat.
    - `ANCHOR` hitboxes can't be hit and exist only to track a bone's position.
- Melee hits, projectile hits and right-clicks report which hitbox they landed on, so damage can be scaled per hitbox (headshots, armoured plates, weak points).
- Hitboxes are drawn with F3 + B, colored by role and damage multiplier.
- Clients report animated poses to the server, so server-side checks such as projectiles and AI attacks line up with what players see.
- Entities that a nearby player isn't looking at still have their hitboxes posed and reported, so server-side checks keep working behind the camera.
- Added a Bone Hitboxes page to the docs.
- First-person item arms now render the player's worn chestplate sleeve, supporting vanilla armor (including dye, trims and enchantment glint), NeoForge custom armor textures, tints and models, and AzureLib armor.
- Morph compatibility with Walkers and ReMorphed. AzureLib entities now animate when a player is morphed into them instead of standing in their idle pose.
  - Animations driven by Molang queries work with no changes.
  - Mods that pick clips in their entity tick, from `MoveAnalysis`, or from server AI can hook in with `AzMorphable` or `AzMorphCompat.register` / `registerNamespace`. See the Morph Compatibility docs page.
  - Use `AzMorphCompat.isMorphCopy` to skip spawn animations on morphs, and `AzMorphCompat.exclude` to opt a type out.
  - Walkers is optional; nothing runs unless it is installed.

### Fixes
- N/A

### Changes
- The server's attack reach check now includes an entity's hitboxes, so parts that stick out past the vanilla bounding box (tails, weapons) can be hit from as far as they reach.
- Entities using an `AzEntityRenderer` that have hitboxes now run their animator while off-screen within 64 blocks of a player. Keyframe events, including sound keyframes, will fire for those entities even when they aren't visible.

### Developer Notes
- `AzHitboxHolder#onHitboxHit` runs on the server at the start of `LivingEntity#hurt` and returns the damage to apply. `AzHitboxHolder#onHitboxInteract` runs on both sides before `mobInteract`.
- Server-side poses only exist while a player within 64 blocks has the entity loaded. With no player nearby, hitbox queries return nothing and projectiles fall back to the vanilla bounding box, so server logic that reads hitboxes should keep a fallback.
- Reported poses are bounds-checked but come from clients. A modified client can shift a mob's hitboxes within `maxReach` and `maxHalfExtent`. Use `AzHitboxSet.Builder#clientOnly()` where that isn't acceptable.
- Added `AzRendererPipeline#modelRenderer()`, `AzEntityRenderer#poseOffscreen` and `AzEntityModelRenderer#poseBonesOffscreen`. The entity transforms and bone matrix tracking in `AzEntityModelRenderer` were moved into private methods shared by the on-screen and off-screen paths. Behavior is unchanged for subclasses.
