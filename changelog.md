v1.1.0

### Additions
- Added bone hitboxes. Entities can now define hitboxes that follow bones in their model, rotate with them, and stay in sync with whatever animation is playing. Implement `AzHitboxHolder` and return an `AzHitboxSet` to use them.
- Hitboxes can fit a bone's cubes automatically or use an explicit size, offset and inflate in Blockbench pixels, and several hitboxes can share one bone.
- Hitboxes have three roles.
    - `COMBAT` hitboxes take crosshair targeting, projectiles and damage.
    - `INTERACT` hitboxes can be right-clicked to trigger custom logic without affecting combat.
    - `ANCHOR` hitboxes can't be hit and exist only to track a bone's position.
- Melee hits, projectile hits (arrows, snowballs, eggs and ghast fireballs) and right-clicks report which hitbox they landed on, so damage can be scaled per hitbox (headshots, armoured plates, weak points).
- Hitbox outlines can be toggled by holding F3 and pressing B, colored by role and damage multiplier. Beta has no entity box view, so this is AzureLib's own toggle.
- In single player, hitboxes are read directly from the rendered pose. In multiplayer, clients report animated poses to the server, so server-side checks such as projectiles and AI attacks line up with what players see.
- Entities that a nearby player isn't looking at still have their hitboxes posed (and reported in multiplayer), so checks keep working behind the camera.
- Added a Bone Hitboxes page to the docs.
- First-person item arms now render the player's worn chestplate sleeve, supporting vanilla armor and AzureLib armor.

### Fixes
- Fixed animated textures causing large frame-time spikes and heavy memory churn. Each frame change re-decoded the texture pixel by pixel and re-created its GPU storage, for both the base texture and its glow mask. Frames are now decoded once and uploaded into storage allocated a single time, so frame changes no longer allocate memory.
- Fixed Molang variable assignments incorrectly reading the right-hand expression as the assignment target, preventing v. and t. variables from being assigned correctly.

### Changes
- The server's attack and interact distance check now measures to an entity's hitboxes, so parts that stick out past the vanilla bounding box (tails, weapons) can be hit from as far as they reach.
- Entities using an `AzEntityRenderer` that have hitboxes now run their animator while off-screen within 64 blocks of a player. Keyframe events, including sound keyframes, will fire for those entities even when they aren't visible.
- AzureLib now sends packets from client to server on a second OSL Networking channel, `azurelib:serverbound`. Multiplayer hitboxes need AzureLib on the server.

### Developer Notes
- `AzHitboxHolder#onHitboxHit(AzHitboxHit, Entity, int)` runs at the start of `MobEntity#damage(Entity, int)` on the server (or in the single player world) and returns the damage to apply. The entity is the attacking player for melee or the projectile itself for projectiles. The default rounds `amount * damageMultiplier()`.
- `AzHitboxHolder#onHitboxInteract(AzHitboxHit, PlayerEntity)` runs on both sides before `Entity#interact(PlayerEntity)` and item use. Return `true` to consume the click.
- In multiplayer, server-side poses only exist while a player within 64 blocks has the entity loaded. With no player nearby, hitbox queries return nothing and projectiles fall back to the vanilla bounding box, so server logic that reads hitboxes should keep a fallback.
- Reported poses are bounds-checked but come from clients. A modified client can shift a mob's hitboxes within `maxReach` and `maxHalfExtent`. Use `AzHitboxSet.Builder#clientOnly()` where that isn't acceptable.
- Added `AzRendererPipeline#modelRenderer()`, `AzEntityRenderer#poseOffscreen` and `AzEntityModelRenderer#poseBonesOffscreen`. The entity transforms and bone matrix tracking in `AzEntityModelRenderer` were moved into private methods shared by the on-screen and off-screen paths. Behavior is unchanged for subclasses.
- Added `AzureLibNetwork#registerServerbound` and `AzureLibNetwork#sendToServer` with `AzServerboundPacket`. In single player, `sendToServer` handles the packet immediately with the local player.
- Added `AzArmorRendererPipelineContext#setVisiblePartOverride`, which limits an armor render to the bone matching one base model part.
