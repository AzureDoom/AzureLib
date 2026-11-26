v4.1.10

### Additions
- N/A

### Fixes
- Fixed AzureLib entity animations stuttering heavily in 4.1.9.
- Fixed the "missing mandatory fields min_format and max_format" warning on startup.

### Changes
- N/A

### Developer Notes
- `AzRenderContext#isGuiRender()` no longer inspects the projection type. A mixin wraps `LevelExtractor#extract` and marks the level extraction window with `AzRenderContext#beginLevelExtract()` and `#endLevelExtract()` (depth-counted, reset in a `finally` block). Any entity extracted outside that window, including through GUI screens, HUD overlays, and third-party previews, counts as a GUI render. This also covers the local player's extraction and block entity extraction.
