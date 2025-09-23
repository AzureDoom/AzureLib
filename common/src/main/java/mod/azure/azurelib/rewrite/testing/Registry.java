package mod.azure.azurelib.rewrite.testing;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

import java.util.function.Supplier;

import mod.azure.azurelib.common.internal.common.registry.SilencedEntityTypeBuilder;
import mod.azure.azurelib.common.platform.Services;
import mod.azure.azurelib.rewrite.testing.item.PistolItem;

public class Registry {

    private Registry() {}

    public static final Supplier<EntityType<MarauderEntity>> MARAUDER = registerEntity(
        "marauder",
        MarauderEntity::new,
        MobCategory.MONSTER,
        1.5f,
        2.6f
    );

    public static final Supplier<Item> PISTOL = registerItem(
        "pistol",
        PistolItem::new
    );

    public static final Supplier<SpawnEggItem> MARAUDER_SPAWN_EGG = registerItem(
        "marauder_spawn_egg",
        Services.COMMON_REGISTRY.makeSpawnEggFor(
            Registry.MARAUDER,
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
