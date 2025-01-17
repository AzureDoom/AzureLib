package mod.azure.azurelib.neoforge.platform;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
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
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import mod.azure.azurelib.common.platform.services.CommonRegistry;

public class NeoForgeCommonRegistry implements CommonRegistry {

    public static DeferredRegister<BlockEntityType<?>> blockEntityTypeDeferredRegister;

    public static DeferredRegister<Block> blockDeferredRegister;

    public static DeferredRegister<EntityType<?>> entityTypeDeferredRegister;

    public static DeferredRegister<ArmorMaterial> armorMaterialDeferredRegister;

    public static DeferredRegister<Item> itemDeferredRegister;

    public static DeferredRegister<SoundEvent> soundEventDeferredRegister;

    public static DeferredRegister<MenuType<?>> menuTypeDeferredRegister;

    public static DeferredRegister<StructureType<?>> structureTypeDeferredRegister;

    public static DeferredRegister<ParticleType<?>> particleTypeDeferredRegister;

    public static DeferredRegister<CreativeModeTab> creativeModeTabDeferredRegister;

    public static DeferredRegister<MobEffect> statusEffectDeferredRegister;

    public static DeferredRegister<Fluid> fluidDeferredRegister;

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(
        String modID,
        String blockEntityName,
        Supplier<BlockEntityType<T>> blockEntityType
    ) {
        if (modID.isEmpty())
            modID = "minecraft";
        blockEntityTypeDeferredRegister = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, modID);
        return blockEntityTypeDeferredRegister.register(blockEntityName, blockEntityType);
    }

    @Override
    public <T extends Block> Supplier<T> registerBlock(String modID, String blockName, Supplier<T> block) {
        if (modID.isEmpty())
            modID = "minecraft";
        blockDeferredRegister = DeferredRegister.create(Registries.BLOCK, modID);
        return blockDeferredRegister.register(blockName, block);
    }

    @Override
    public <T extends Entity> Supplier<EntityType<T>> registerEntity(
        String modID,
        String entityName,
        Supplier<EntityType<T>> entity
    ) {
        // if (modID.isEmpty()) modID = "minecraft";
        // entityTypeDeferredRegister = DeferredRegister.create(Registries.ENTITY_TYPE, modID);
        // return entityTypeDeferredRegister.register(entityName, entity);
        return null;
    }

    @Override
    public <T extends ArmorMaterial> Holder<T> registerArmorMaterial(
        String modID,
        String matName,
        Supplier<T> armorMaterial
    ) {
        // if (modID.isEmpty()) modID = "minecraft";
        // armorMaterialDeferredRegister = DeferredRegister.create(Registries.ARMOR_MATERIAL, modID);
        // return (Holder<T>) armorMaterialDeferredRegister.register(matName, armorMaterial);
        return null;
    }

    @Override
    public <T extends Item> Supplier<T> registerItem(String modID, String itemName, Supplier<T> item) {
        // if (modID.isEmpty()) modID = "minecraft";
        // itemDeferredRegister = DeferredRegister.create(Registries.ITEM, modID);
        // return itemDeferredRegister.register(itemName, item);
        return null;
    }

    @Override
    public <T extends SoundEvent> Supplier<T> registerSound(String modID, String soundName, Supplier<T> sound) {
        // if (modID.isEmpty()) modID = "minecraft";
        // soundEventDeferredRegister = DeferredRegister.create(Registries.SOUND_EVENT, modID);
        // return soundEventDeferredRegister.register(soundName, sound);
        return null;
    }

    @Override
    public <T extends MenuType<?>> Supplier<T> registerScreen(String modID, String screenName, Supplier<T> menuType) {
        // if (modID.isEmpty()) modID = "minecraft";
        // menuTypeDeferredRegister = DeferredRegister.create(Registries.MENU, modID);
        // return menuTypeDeferredRegister.register(screenName, menuType);
        return null;
    }

    @Override
    public <T extends Structure> Supplier<StructureType<T>> registerStructure(
        String modID,
        String structureName,
        MapCodec<T> structure
    ) {
        // if (modID.isEmpty()) modID = "minecraft";
        // structureTypeDeferredRegister = DeferredRegister.create(Registries.STRUCTURE_TYPE, modID);
        // return structureTypeDeferredRegister.register(structureName, () -> typeConvert(structure));
        return null;
    }

    private static <S extends Structure> StructureType<S> typeConvert(MapCodec<S> codec) {
        return () -> codec;
    }

    @Override
    public <T extends ParticleType<?>> Supplier<T> registerParticle(
        String modID,
        String particleName,
        Supplier<T> particle
    ) {
        // if (modID.isEmpty()) modID = "minecraft";
        // particleTypeDeferredRegister = DeferredRegister.create(Registries.PARTICLE_TYPE, modID);
        // return particleTypeDeferredRegister.register(particleName, particle);
        return null;
    }

    @Override
    public <T extends CreativeModeTab> Supplier<T> registerCreativeModeTab(
        String modID,
        String tabName,
        Supplier<T> tab
    ) {
        // if (modID.isEmpty()) modID = "minecraft";
        // creativeModeTabDeferredRegister = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, modID);
        // return creativeModeTabDeferredRegister.register(tabName, tab);
        return null;
    }

    @Override
    public <T extends MobEffect> Holder<T> registerStatusEffect(
        String modID,
        String effectName,
        Supplier<T> statusEffect
    ) {
        // if (modID.isEmpty()) modID = "minecraft";
        // statusEffectDeferredRegister = DeferredRegister.create(Registries.MOB_EFFECT, modID);
        // return (Holder<T>) statusEffectDeferredRegister.register(effectName, statusEffect);
        return null;
    }

    @Override
    public <T extends Fluid> Supplier<T> registerFluid(String modID, String fluidName, Supplier<T> fluid) {
        // if (modID.isEmpty()) modID = "minecraft";
        // fluidDeferredRegister = DeferredRegister.create(Registries.FLUID, modID);
        // return fluidDeferredRegister.register(fluidName, fluid);
        return null;
    }

    @Override
    public <E extends Mob> Supplier<SpawnEggItem> makeSpawnEggFor(
        Supplier<EntityType<E>> entityType,
        int primaryEggColour,
        int secondaryEggColour,
        Item.Properties itemProperties
    ) {
        return () -> new DeferredSpawnEggItem(entityType, primaryEggColour, secondaryEggColour, itemProperties);
    }

    @Override
    public CreativeModeTab.Builder newCreativeTabBuilder() {
        return CreativeModeTab.builder();
    }
}
