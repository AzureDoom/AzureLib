v1.0.0

### Additions
- `AzEntityRendererRegistry` and `AzBlockEntityRendererRegistry` (replace StationAPI's renderer register events). Register from `client-init`; works before or after the dispatchers exist.
- `AzEntityRegistry` and `AzBlockEntityRegistry` (replace StationAPI's entity / block entity register events). Register from `init` on both sides; name/id clashes throw.

### Fixes
- N/A

### Changes
- Ported from Babric + StationAPI to Ornithe (gen2) + OSL, Java 8.
- Networking uses OSL's Networking API (`azurelib:packet` channel) instead of StationAPI message packets.
- Item stack identity is stored in AzureLib's own per-stack data (saved with the stack, kept by `copy()`); clients adopt the server's id from the item dispatch packet.
- Held block items now use Beta's biped hand placement (no Arsenic renderer).

### Developer Notes
- All vanilla member access goes through `platform/mc/Mc` and `McClient`, annotated with MCP names. See `PORTING.md`.
