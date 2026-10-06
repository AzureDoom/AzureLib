v4.0.7

### Additions
- Restored the in-game config screen for AzureLib's config API on 26.3, including config group selection, nested object screens, array editing (add/remove elements), and confirmation dialogs for "Use default config" and "Rollback changes".
- Added a color picker for `@Configurable.Gui.ColorValue` string fields, with RGB and ARGB slider dialogs.
- Added ModMenu integration on Fabric and mod list config buttons on NeoForge for every mod that registers configs through AzureLib.

### Fixes
- Fixed the Fabric ModMenu entrypoint pointing to a missing `ModMenuIntegration` class.
- Fixed editing array config elements in the GUI overwriting the stored default values.
- Fixed color values written from the color picker dropping leading zeros (e.g. `#0000FF` was saved as `#FF`).

### Developer Notes
- Added `ConfigurationClient` with `getConfigScreen`, `getConfigScreenByGroup` and `getScreenForGroup` for opening config screens from your own code.
- Added `DisplayAdapters.registerCustom(Class, DisplayAdapter)` to override how a config value type is displayed in the GUI.
- `@ValueUpdateCallback` validation warnings and errors are now shown in the config screen.
- Mod list config buttons are registered per config group, so the `@Config` group (defaults to the config id) must match your mod ID.