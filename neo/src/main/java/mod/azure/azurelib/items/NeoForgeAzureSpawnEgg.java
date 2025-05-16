package mod.azure.azurelib.items;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;

import java.util.function.Supplier;

public class NeoForgeAzureSpawnEgg extends ForgeSpawnEggItem {

    public NeoForgeAzureSpawnEgg(
        Supplier<? extends EntityType<? extends Mob>> type,
        int primaryColor,
        int secondaryColor
    ) {
        super(type, primaryColor, secondaryColor, new Item.Properties().stacksTo(64));
    }

    public NeoForgeAzureSpawnEgg(EntityType<? extends Mob> type, int primaryColor, int secondaryColor) {
        super(() -> type, primaryColor, secondaryColor, new Item.Properties().stacksTo(64));
    }

}
