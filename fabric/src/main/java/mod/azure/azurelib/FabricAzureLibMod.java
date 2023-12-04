package mod.azure.azurelib;

import mod.azure.azurelib.config.AzureLibConfig;
import mod.azure.azurelib.config.format.ConfigFormats;
import mod.azure.azurelib.config.io.ConfigIO;
import mod.azure.azurelib.enchantments.IncendiaryEnchantment;
import mod.azure.azurelib.entities.TickingLightBlock;
import mod.azure.azurelib.entities.TickingLightEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class FabricAzureLibMod implements ModInitializer {
    public static BlockEntityType<TickingLightEntity> TICKING_LIGHT_ENTITY;
    public static final TickingLightBlock TICKING_LIGHT_BLOCK = new TickingLightBlock();
    public static final Enchantment INCENDIARYENCHANTMENT = new IncendiaryEnchantment(Enchantment.Rarity.RARE, EquipmentSlot.MAINHAND);

    @Override
    public void onInitialize() {
        ConfigIO.FILE_WATCH_MANAGER.startService();
        AzureLibMod.config = AzureLibMod.registerConfig(AzureLibConfig.class, ConfigFormats.json()).getConfigInstance();
        AzureLib.initialize();
        Registry.register(BuiltInRegistries.BLOCK, AzureLib.modResource("lightblock"), FabricAzureLibMod.TICKING_LIGHT_BLOCK);
        FabricAzureLibMod.TICKING_LIGHT_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, AzureLib.MOD_ID + ":lightblock", FabricBlockEntityTypeBuilder.create(TickingLightEntity::new, FabricAzureLibMod.TICKING_LIGHT_BLOCK).build(null));
        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
            ConfigIO.FILE_WATCH_MANAGER.stopService();
        });
        Registry.register(BuiltInRegistries.ENCHANTMENT, AzureLib.modResource("incendiaryenchantment"), INCENDIARYENCHANTMENT);
    }
}
