package mod.azure.azurelib.helper;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import mod.azure.azurelib.AzureLib;

public class AzureTags {

    public static final TagKey<Item> GUNS = TagKey.create(Registries.ITEM, AzureLib.modResource("guns"));
}
