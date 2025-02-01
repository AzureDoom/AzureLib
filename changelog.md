v3.0.5

- Changes a few Builder options from protected to public
- Adds support for q.* Molang.

v3.0.4

- Fixes useNewOffset for items not working when used.

v3.0.3

- Fixes setRenderType issue caused by last update.

v3.0.2

- Implement preRenderEntry and postRenderEntry call backs to RenderConfigs, allowing you to inject code into the preRender and postRender stages.

v3.0.1

- Fixes a packet issue with calling cancelAll on an AzCommand from the server.
- Implement getRenderType to RenderConfigs, as to make changing the render type easier.

v3.0.0

- Rewrite Entity animation system. See guide for converting here: https://moddedmc.wiki/en/project/azurelib/docs/updating/entities
  - No longer have to supply a cache.
  - No longer have to use a GeoEnity interface.
- Rewrite Item animation system. See guide for converting here: https://moddedmc.wiki/en/project/azurelib/docs/updating/items
  - Fixes issue with Item animations not working the first time due to missing NBT tag check.
  - No longer have to supply a cache.
  - No longer have to use a GeoItem interface.
  - "Item" rendering/animating is now considered "ItemStack" rendering/animating.
  - No longer register the render in the Item.
    - This is done in your clients onInitializeClient for Fabric and NeoForges FMLClientSetupEvent using AzItemRendererRegistry#register. 
- Rewrite Block Entity animation system. See guide for converting here: https://moddedmc.wiki/en/project/azurelib/docs/updating/blockentities
  - No longer have to supply a cache.
  - No longer have to use a GeoEntity interface.
- Rewrite Armor animation system. See guide for converting here: https://moddedmc.wiki/en/project/azurelib/docs/updating/armor
  - No longer have to supply a cache.
  - No longer have to use a GeoItem interface.
  - "Item" rendering/animating is now considered "ItemStack" rendering/animating.
  - No longer register the render in the Item.
    - This is done in your clients onInitializeClient for Fabric and NeoForges FMLClientSetupEvent using AzArmorRendererRegistry#register.
- Animations are now done fully using a trigger animation call from the Az<Type>Animator.
- New system fixes Animations not firing properly on Items on first use. 
  - You have to now register your item in your mods onInitialize for Fabric and NeoForges FMLCommonSetupEvent using AzIdentityRegistry#register
    - AzIdentityRegistry#register can take 1 item or multiple if you have a lot of item. 
- New system fixes Aniamtions not pausing correctly when in singleplayer. (Old system/Geckolib "pauses" it but it still ticks so doesn't hold the animations spot properly)
- New system fixes Animation triggers not working with armors.
- New system shows about a 40% drop in memory usage compared to old systems/Azurelib.
- Move to new Az Naming scheme from Geo
- Fixes crash with Minecolonies when using new render.

# Known issues
- PLAY_ONCE animations have an odd bug with bones resetting in between triggers.