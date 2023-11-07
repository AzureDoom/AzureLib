package mod.azure.azurelib.common.api.common.items;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

public class AzureSpawnEgg extends SpawnEggItem {

    /**
     * TODO: Make egg work correctly for both loaders using this common version, currently only works correctly with Fabric.
     *
     * @param type           Your registered Entity
     * @param primaryColor   Primary Egg Color
     * @param secondaryColor Secondary Egg Color
     */
    public AzureSpawnEgg(EntityType<? extends Mob> type, int primaryColor, int secondaryColor) {
        super(type, primaryColor, secondaryColor, new Item.Properties().stacksTo(64));
    }

}