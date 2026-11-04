v4.1.5

### Additions
- Added `AzIkTargets.bone(name)`, an IK target that follows a bone as animated this frame, optionally offset from its pivot with `bone(name, x, y, z)`. Animate the position of an empty target bone and the limb reaches for it, so you only keyframe where a hand or foot goes.
- Added support for Better Combat weapon animations and with the Azurelib hitboxes.
- Added support for Geckolib armors in AzArmorLayer.

### Fixes
- Fixed armor, held items, attachments and other per-bone render layers disappearing from entities and block entities when their geometry was served from the render cache. Cached models now keep their layers, and entities with render layers or tracked bones can use the geometry cache again.

### Changes
- N/A

### Developer Notes
- `AzIkTargets.bone` looks the bone up once per animatable (and again if its model changes), and leaves the limb as animated if the model has no such bone. Keep the target bone outside the chain it drives, for example under the body or root; if it were inside the limb, moving the limb would move its own target.
