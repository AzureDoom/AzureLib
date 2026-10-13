v1.0.2

### Additions
- N/A

### Fixes
- Fixed AzureLib armor rendering in the wrong place on players and mobs when an AzureLib entity was wearing the same armor item. The chestplate would float above the wearer's head.
- Fixed arm, leg and boot pieces of AzureLib armor on AzureLib entities being placed at the wrong position (for example, gauntlets rendering at the entity's feet), depending on the order the entity's bones rendered in.

### Changes
- N/A

### Developer Notes
- Each armor item shares one `AzArmorModel` across every wearer. `AzArmorLayer` used to leave that shared model's biped parts posed with the entity's bone pivots and rotations, and vanilla's `ModelBiped#setRotationAngles` never resets `bipedBody`'s rotation point or the arms' Y rotation point. That stale state then leaked into the next wearer.
- `AzArmorModel#render` now resets every biped part to the default `ModelBiped` pose before calling `setRotationAngles`, so the vanilla armor layer path always starts from a clean pose.
- `AzArmorLayer` now saves the biped pose of every armor model it uses during an entity's render and restores it in `render(context)` once the model pass is finished. Subclasses that override `render` must call `super.render(context)`.
- `AzArmorLayer` now caches each entity's posed biped parts per armor model and applies that cache before each piece renders. `applyBaseTransformations` positions every armor bone from every biped part, so each piece now sees this entity's full pose rather than default or leftover values. The cache is weakly keyed on the entity.
