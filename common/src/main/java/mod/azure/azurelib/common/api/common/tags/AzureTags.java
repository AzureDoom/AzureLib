package mod.azure.azurelib.common.api.common.tags;

import mod.azure.azurelib.common.internal.common.AzureLib;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class AzureTags {
    public static final TagKey<Item> GUNS = TagKey.create(Registries.ITEM, AzureLib.modResource("guns"));
}
