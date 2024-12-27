package mod.azure.azurelib.fabric.core2.example;

import mod.azure.azurelib.fabric.core2.example.entities.ovamorph.Ovamorph;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.entity.BlockEntityType;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.fabric.FabricAzureLibMod;
import mod.azure.azurelib.fabric.core2.example.blocks.StargateBlockEntity;
import mod.azure.azurelib.fabric.core2.example.entities.doomhunter.DoomHunter;
import mod.azure.azurelib.fabric.core2.example.entities.marauder.MarauderEntity;

public class ExampleEntityTypes {

    public static final EntityType<DoomHunter> DOOMHUNTER = register(
        "doomhunter",
        EntityType.Builder.of(DoomHunter::new, MobCategory.MONSTER).sized(3.0f, 7.0f)
    );

    public static final EntityType<MarauderEntity> MARAUDER = register(
        "marauder",
        EntityType.Builder.of(MarauderEntity::new, MobCategory.MONSTER).sized(1.5f, 2.6f)
    );

    public static final EntityType<Ovamorph> OVAMORPH = register(
        "ovamorph",
        EntityType.Builder.of(Ovamorph::new, MobCategory.MONSTER).sized(0.98f, 0.98f)
    );

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        var entityType = builder.build(name);
        var resourceLocation = AzureLib.modResource(name);
        Registry.register(BuiltInRegistries.ENTITY_TYPE, resourceLocation, entityType);
        return entityType;
    }

    public static BlockEntityType<StargateBlockEntity> STARGATE = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        AzureLib.modResource("stargate"),
        FabricBlockEntityTypeBuilder.create(StargateBlockEntity::new, FabricAzureLibMod.STARGATE).build(null)
    );;

    public static void initialize() {
        FabricDefaultAttributeRegistry.register(DOOMHUNTER, DoomHunter.createMonsterAttributes());
        FabricDefaultAttributeRegistry.register(MARAUDER, MarauderEntity.createMonsterAttributes());
        FabricDefaultAttributeRegistry.register(OVAMORPH, Ovamorph.createMonsterAttributes());
    }
}
