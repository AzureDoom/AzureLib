v1.1.3

### Additions
- Added spring bone colliders. `AzSpringCollider` is a sphere or capsule attached to a bone, positioned in Blockbench pixels from the bone's pivot, that follows the bone's animated pose. Spring chains can't pass through them, so tails, ears and capes swing around the body instead of through it. One collider can be shared by several chains.
- Added `AzSpringBone.Builder#collider` to give a chain its colliders, `#radius` for how thick the chain is, and `#ground` to keep the chain above the entity's feet (or the model's origin for items and block entities).

### Fixes
- N/A

### Changes
- Spring bone chains now bend and whip. Each joint springs towards where the animation puts it relative to the joint before it, so lag builds up towards the tip; before, every joint lagged by the same angle and chains swung as one stiff piece.

### Developer Notes
- Because each joint now follows its parent, a given stiffness makes chains curl more than before. Chains tuned for the old behaviour may need a higher stiffness: around `0.3` to `0.5` suits tails.
- Collisions are resolved at the chain's joints. A collider thinner than the distance between two joints can slip between them, so keep colliders at least that thick or add some `radius`.
- With a `radius`, a chain stays above the ground and outside colliders, but can settle partway into its own thickness.
- A collider on a bone the model doesn't have is skipped. Don't put a collider on one of the chain's own bones, or overlapping the chain's animated pose, or the chain is always being pushed out and never settles.
