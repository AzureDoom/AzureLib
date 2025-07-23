v3.0.22

### Bug Fixes
- Fixed an issue where keyframes would cause a crash if their index went below 0. The index now defaults to 0 when empty.  
  *Credit to Collinvh for identifying and resolving the issue.*
- Fixed a problem where snapshots or queues being null would cause a crash. These are now safely skipped instead.  
  *Credit to Collinvh for the fix.*

### Enhancements
- **Partial Ticks Support:**
    - Added support for partial ticks in the `applyMolangQueries` method. The old version of `applyMolangQueries` has been made private and is marked for future removal to ensure streamlined functionality.

### New MoLang Queries
The following queries have been added:

- **`query.head_pitch`**
    - Returns the pitch (vertical rotation) of the head.
- **`query.head_yaw`**
    - Returns the yaw (horizontal rotation) of the head relative to the body.
- **`query.hurt_time`**
    - Indicates the remaining time in the entity's hurt animation.
- **`query.is_baby`**
    - Returns whether the entity is a baby (`true` or `false`).
- **`query.is_blocking`**
    - Determines if the entity is actively blocking (e.g., using a shield).
- **`query.is_using_item`**
    - Returns whether the entity is actively using an item (e.g., eating or drinking).
- **`query.limb_swing`**
    - Represents the position of the entity's limb swing animation (useful for walking or running effects).
- **`query.limb_swing_amount`**
    - Indicates the speed or intensity of the limb swing animation.
- **`query.in_air`**
    - Returns whether the entity is currently in the air and not on the ground.
- **`query.item_current_durability`**
    - Provides the current durability of an item as a fraction of its maximum durability.
- **`query.item_is_enchanted`**
    - Returns whether the item is enchanted.