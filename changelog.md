v3.1.5

### Fixes
- Fixed some edge cases with AzCachedBoneUpdateUtil
- Fixed a bug that caused double rendering with armor trims.
- Fixed an issue where Azure armor used in AzArmorRender would not properly move with the entity.
- Added ItemMixin_EnsureCraftHasID to ensure items have an az_id UUID component when crafted if it has an identity registered.