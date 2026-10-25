v1.1.1

### Additions
- Added ReachFix compatibility for AzureLib entity hitboxes. With ReachFix installed, attacking or interacting with an AzureLib entity now respects its bone hitboxes when checking reach on the server, instead of only the vanilla bounding box.

### Fixes
- Fixed a conflict with ReachFix where AzureLib's server-side reach mixin could stop `NetHandlerPlayServer` from transforming, or be skipped entirely, depending on load order.
- Fixed log spam caused by the embedded Mixin library's `module-info.class` being packaged into the AzureLib jar.

### Changes
- AzureLib's vanilla `processUseEntity` reach mixin is now turned off when ReachFix is detected, and ReachFix's own range check is extended instead. Behavior without ReachFix is unchanged.

### Developer Notes
- N/A