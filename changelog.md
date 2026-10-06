v4.0.6

### Additions
- Ported first-person arm rendering from 1.21.1. Items with `leftArm` and/or `rightArm` bones now render the player's own arms, skin and sleeves included, in place of those bones while an animation is playing in first person. Anything parented to the arm bones still renders, so held parts and attachments follow the player's arms. Skipped when the First-person Model mod is installed, since it handles arms itself.

### Fixes
- Fixed AzureLib items crashing when `disableAnimationInContexts` or `enableAnimationInContexts` was used, and fixed item renderers never knowing which display context they were rendering in (first person, GUI, ground, ...). Since 26.x, Minecraft no longer tells special item renderers the display context, so it is now captured from the item's render state.

### Developer Notes
- New `AzItemDisplayContextCapture` exposes the display context of the item currently being rendered, captured by a new `ItemStackRenderState` mixin. `AzItemRendererConfig#shouldAnimateInContext` now treats a `null` context as `ItemDisplayContext.NONE`.
- First-person arms are rendered by `AzItemArmRenderUtil`. Since 1.21.2, the player model's sleeves are child parts of its arms, so each arm is drawn in two passes: the arm as solid skin, then only its sleeve as translucent skin, keeping the player's sleeve visibility settings.