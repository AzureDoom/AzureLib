package mod.azure.azurelib.neoforge.items;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

import java.util.function.Supplier;

public class NeoForgeAzureSpawnEgg extends DeferredSpawnEggItem {

    public NeoForgeAzureSpawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int primaryColor, int secondaryColor) {
        super(type, primaryColor, secondaryColor, new Item.Properties().stacksTo(64));
    }

    public NeoForgeAzureSpawnEgg(EntityType<? extends Mob> type, int primaryColor, int secondaryColor) {
        super(() -> type, primaryColor, secondaryColor, new Item.Properties().stacksTo(64));
    }

}