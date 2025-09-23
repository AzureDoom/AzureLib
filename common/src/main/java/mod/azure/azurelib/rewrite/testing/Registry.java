package mod.azure.azurelib.rewrite.testing;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

import java.util.function.Supplier;

import mod.azure.azurelib.common.internal.common.registry.SilencedEntityTypeBuilder;
import mod.azure.azurelib.common.platform.Services;

public class Registry {

    private Registry() {}

    public static final Supplier<EntityType<MutantZombieEntity>> MUTANT_ZOMBIE = registerEntity(
        "mutant_zombie",
        MutantZombieEntity::new,
        MobCategory.MONSTER,
        1.5f,
        2.6f
    );

    public static final Supplier<Item> WOLF_HELMET = registerItem(
        "wolf_armor_helmet",
        () -> new ArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1))
    );

    public static final Supplier<Item> WOLF_CHESTPLATE = registerItem(
        "wolf_armor_chestplate",
        () -> new ArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1))
    );

    public static final Supplier<Item> WOLF_LEGGINGS = registerItem(
        "wolf_armor_leggings",
        () -> new ArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1))
    );

    public static final Supplier<Item> WOLF_BOOTS = registerItem(
        "wolf_armor_boots",
        () -> new ArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1))
    );

    public static final Supplier<SpawnEggItem> MUTANT_ZOMBIE_SPAWN_EGG = registerItem(
        "mutant_zombie_spawn_egg",
        Services.COMMON_REGISTRY.makeSpawnEggFor(
            Registry.MUTANT_ZOMBIE,
            0xe9e2ed,
            0x574f44,
            new Item.Properties()
        )
    );

    static <T extends Entity> Supplier<EntityType<T>> registerEntity(
        String entityName,
        EntityType.EntityFactory<T> entity,
        MobCategory mobCategory,
        float width,
        float height
    ) {
        return Services.COMMON_REGISTRY.register(
            BuiltInRegistries.ENTITY_TYPE,
            entityName,
            () -> create(entity, mobCategory, width, height).buildWithoutDataFixerCheck()
        );
    }

    static <T extends Entity> SilencedEntityTypeBuilder create(
        EntityType.EntityFactory<T> entity,
        MobCategory mobCategory,
        float width,
        float height
    ) {
        return (SilencedEntityTypeBuilder) EntityType.Builder.of(entity, mobCategory).sized(width, height);
    }

    static <T extends Item> Supplier<T> registerItem(String itemName, Supplier<T> item) {
        return Services.COMMON_REGISTRY.register(BuiltInRegistries.ITEM, itemName, item);
    }

    public static void initialize() {}
}
