v3.1.9

### Changes
- Added a safe catch to AzCommand#sendForItem to catch any mod not properly checking for items in inventoryTick and sending an AzCommand.
- Added setShouldAnimateInContext, disableAnimationInContexts, and enableAnimationOnlyInContexts methods to AzItemRendererConfig.
    - This allows you to disable animations in certain display contexts.
- Added built-in support for items to render player arms in the first-person view as long as the proper bones exist in the item model.
    - Import the arms from this bbmodel to your item model and only move them in the animations you want to use them in.
    - Download the bbmodel here: https://github.com/AzureDoom/AzureLib/blob/1.21.1/player_arms_import.bbmodel