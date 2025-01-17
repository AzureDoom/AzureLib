package mod.azure.azurelib.common.platform.services;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Supplier;

public interface CommonRegistry {

    <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(
        String modID,
        String blockEntityName,
        Supplier<BlockEntityType<T>> blockEntityType
    );

    <T extends Block> Supplier<T> registerBlock(String modID, String blockName, Supplier<T> block);

    <T extends Entity> Supplier<EntityType<T>> registerEntity(
        String modID,
        String entityName,
        Supplier<EntityType<T>> entity
    );

    <T extends ArmorMaterial> Holder<T> registerArmorMaterial(String modID, String matName, Supplier<T> armorMaterial);

    <T extends Item> Supplier<T> registerItem(String modID, String itemName, Supplier<T> item);

    <T extends SoundEvent> Supplier<T> registerSound(String modID, String soundName, Supplier<T> sound);

    <T extends MenuType<?>> Supplier<T> registerScreen(String modID, String screenName, Supplier<T> menuType);

    <T extends Structure> Supplier<StructureType<T>> registerStructure(
        String modID,
        String structureName,
        MapCodec<T> structure
    );

    <T extends ParticleType<?>> Supplier<T> registerParticle(String modID, String particleName, Supplier<T> particle);

    <T extends CreativeModeTab> Supplier<T> registerCreativeModeTab(String modID, String tabName, Supplier<T> tab);

    default <T extends MobEffect> Holder<T> registerStatusEffect(
        String modID,
        String effectName,
        Supplier<T> statusEffect
    ) {
        return null;
    }

    default <T extends Fluid> Supplier<T> registerFluid(String modID, String fluidName, Supplier<T> item) {
        return null;
    }

    <E extends Mob> Supplier<SpawnEggItem> makeSpawnEggFor(
        Supplier<EntityType<E>> entityType,
        int primaryEggColour,
        int secondaryEggColour,
        Item.Properties itemProperties
    );

    CreativeModeTab.Builder newCreativeTabBuilder();
}
