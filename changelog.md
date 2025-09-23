v3.0.30

### Fixes
- Fixed Entity rendering for entities using items with custom renderers causing rendering issues.
- Fixed Entity rotation on non-living entities using AzEntityRenderer.
- Fixed Armor rendering for baby entities being used all entities, now properly checks if entity is baby for armor scaling.
- Fixed an issue where AzCommand#create would cause transition ticks to always be 0, this must now be set manually, which will override the controller's default value.