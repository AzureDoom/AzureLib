v3.0.0

- Rewrite Entity animation system. See guide for converting here:
  - No longer have to supply a cache.
  - No longer have to use a GeoEnity interface.
- Rewrite Item animation system. See guide for converting here:
  - Fixes issue with Item animations not working the first time due to missing NBT tag check.
  - No longer have to supply a cache.
  - No longer have to use a GeoItem interface.
  - "Item" rendering/animating is now considered "ItemStack" rendering/animating.
  - No longer register the render in the Item.
    - This is done in your clients onInitializeClient for Fabric and NeoForges FMLClientSetupEvent using AzItemRendererRegistry#register. 
- Rewrite Block Entity animation system. See guide for converting here:
  - No longer have to supply a cache.
  - No longer have to use a GeoEntity interface.
- Rewrite Armor animation system. See guide for converting here:
  - No longer have to supply a cache.
  - No longer have to use a GeoItem interface.
  - "Item" rendering/animating is now considered "ItemStack" rendering/animating.
  - No longer register the render in the Item.
    - This is done in your clients onInitializeClient for Fabric and NeoForges FMLClientSetupEvent using AzArmorRendererRegistry#register.
- Animations are now done fully using a trigger animation call from the Az<Type>Animator, which is set in your class like so:
```java
public class PistolItem extends Item {

    private final CustomAzItemAnimator dispatcher;

    public PistolItem() {
        super(new Properties().stacksTo(1));
        this.dispatcher = new CustomAzItemAnimator();
    }
  
    public void exampleMethod(Level level) {
      if (level.isClientSide()) {
          this.dispatcher.fromClient(FIRING_COMMAND).sendForItem(entity, itemStack);
      } else {
          this.dispatcher.fromServer(FIRING_COMMAND).sendForItem(entity, itemStack);
      }
    }
}
```
- New system fixes Animations not firing properly on Items on first use. 
  - You have to now register your item in your mods onInitialize for Fabric and NeoForges FMLCommonSetupEvent using AzIdentityRegistry#register
    - AzIdentityRegistry#register can take 1 item or multiple if you have a lot of item. 
- New system fixes Aniamtions not pausing correctly when in singleplayer. (Old system/Geckolib "pauses" it but it still ticks so doesn't hold the animations spot properly)
- New system fixes Animation triggers not working with armors.
- New system shows about a 40% drop in memory usage compared to old systems/Azurelib.
- Move to new Az Naming scheme from Geo
- No longer ship with a forked SBL, no longer being used in my mods moving forward.
- MORE WIP