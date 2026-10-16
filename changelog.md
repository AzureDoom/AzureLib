v1.0.6

### Additions
- N/A

### Fixes
- Fixed a black box appearing beside the potion effect panel in inventory screens while an AzureLib item was held on the cursor. AzureLib left GL blend disabled after rendering a GUI item, so the transparent part of the effect background drew as opaque black.

### Changes
- `AzItemGuiRenderUtil` now restores the GL state vanilla's GUI item rendering leaves behind after drawing an AzureLib item: blend on with `SRC_ALPHA`/`ONE_MINUS_SRC_ALPHA`, alpha func `GREATER 0.1`, depth on, colour reset to white, and the block atlas rebound.

### Developer Notes
- Any GUI code drawn after an AzureLib item (potion effects, tooltips, other mods' overlays) now sees the same GL state it would after a vanilla item.
