v3.0.16

### Changes
- Added Root-level actions that apply to all controllers on an animator.
- Added Controller-level actions that only apply to the controller you want it to.
- Added new AzPlayBehaviors:
    - FREEZE_ON_FRAME
        - This behavior locks the animation controller's timer to a defined tick value, effectively "freezing" the animation at a specific frame.
    - REPEAT_X_TIMES
        - This behavior repeats the animation X times.
- Changes to AzAnimator to add support for stopping the animation timer.
- Made AzEntityModelRenderer provider and rendererPipeline protected from private.
- Added logger to AutoGlowingTexture if its glow mask doesn't match the base texture size.

### Fixes
- Fixed an issue AzCommand.create() would apply settings to ALL controllers; this is now only on the controller you provide the name for.
- Fixed an issue where broken JSON files would cause the game to fail to load properly, these are now auto-skipped and logged.
- Band-aid fixed a crash with AzBlockandItemLayer crashing when the entity is glowing. Item rendering is now just skipped to avoid crashing.