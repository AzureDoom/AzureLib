v3.0.27

### Changes
- Hide arms bones always in items and only render arms when animations are playing for items.
- Added alpha value to getDefaultRenderType
- Made entityCutout the default entity render type.
- Removed Dynamic renderers as they are broken and no longer needed.
- Add an optional Waist Bone for armors. (Credit to cleannrooster)
- Added all missing Math functions, currently matching: https://bedrock.dev/docs/1.21.0.0/1.21.120.22/Molang#Math%20Functions now

### Fixes
- Fixed builder config render type config completely overwriting the default render type setups
- Fixes hurt/death red overlay from not working on entities
- Fixes when an invalid animation name is used, the controller/state machine will become stuck and no longer update