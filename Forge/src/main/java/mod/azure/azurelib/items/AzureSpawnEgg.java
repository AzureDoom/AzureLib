package mod.azure.azurelib.items;

import java.util.function.Supplier;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;

public class AzureSpawnEgg extends ForgeSpawnEggItem {

	public AzureSpawnEgg(Supplier<? extends EntityType<? extends MobEntity>> type, int primaryColor, int secondaryColor) {
		super(type, primaryColor, secondaryColor, new Item.Properties().stacksTo(64));
	}

}