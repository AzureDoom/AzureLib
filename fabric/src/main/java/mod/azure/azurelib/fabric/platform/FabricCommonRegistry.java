package mod.azure.azurelib.fabric.platform;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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

import mod.azure.azurelib.common.platform.services.CommonRegistry;

public class FabricCommonRegistry implements CommonRegistry {

    private static <T, R extends Registry<? super T>> Supplier<T> registerSupplier(
        R registry,
        String modID,
        String id,
        Supplier<T> object
    ) {
        final T registeredObject = Registry.register(
            (Registry<T>) registry,
            ResourceLocation.fromNamespaceAndPath(modID, id),
            object.get()
        );

        return () -> registeredObject;
    }

    private static <T, R extends Registry<? super T>> Holder<T> registerHolder(
        R registry,
        String modID,
        String id,
        Supplier<T> object
    ) {
        return Registry.registerForHolder(
            (Registry<T>) registry,
            ResourceLocation.fromNamespaceAndPath(modID, id),
            object.get()
        );
    }

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(
        String modID,
        String blockEntityName,
        Supplier<BlockEntityType<T>> blockEntityType
    ) {
        return registerSupplier(BuiltInRegistries.BLOCK_ENTITY_TYPE, modID, blockEntityName, blockEntityType);
    }

    @Override
    public <T extends Block> Supplier<T> registerBlock(String modID, String blockName, Supplier<T> block) {
        return registerSupplier(BuiltInRegistries.BLOCK, modID, blockName, block);
    }

    @Override
    public <T extends Entity> Supplier<EntityType<T>> registerEntity(
        String modID,
        String entityName,
        Supplier<EntityType<T>> entity
    ) {
        return registerSupplier(BuiltInRegistries.ENTITY_TYPE, modID, entityName, entity);
    }

    @Override
    public <T extends ArmorMaterial> Holder<T> registerArmorMaterial(
        String modID,
        String matName,
        Supplier<T> armorMaterial
    ) {
        return registerHolder(BuiltInRegistries.ARMOR_MATERIAL, modID, matName, armorMaterial);
    }

    @Override
    public <T extends Item> Supplier<T> registerItem(String modID, String itemName, Supplier<T> item) {
        return registerSupplier(BuiltInRegistries.ITEM, modID, itemName, item);
    }

    @Override
    public <T extends SoundEvent> Supplier<T> registerSound(String modID, String soundName, Supplier<T> sound) {
        return registerSupplier(BuiltInRegistries.SOUND_EVENT, modID, soundName, sound);
    }

    @Override
    public <T extends MenuType<?>> Supplier<T> registerScreen(String modID, String screenName, Supplier<T> menuType) {
        return registerSupplier(BuiltInRegistries.MENU, modID, screenName, menuType);
    }

    @Override
    public <T extends Structure> Supplier<StructureType<T>> registerStructure(
        String modID,
        String structureName,
        MapCodec<T> structure
    ) {
        return registerSupplier(BuiltInRegistries.STRUCTURE_TYPE, modID, structureName, () -> typeConvert(structure));
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
        return registerSupplier(BuiltInRegistries.PARTICLE_TYPE, modID, particleName, particle);
    }

    @Override
    public <T extends CreativeModeTab> Supplier<T> registerCreativeModeTab(
        String modID,
        String tabName,
        Supplier<T> tab
    ) {
        return registerSupplier(BuiltInRegistries.CREATIVE_MODE_TAB, modID, tabName, tab);
    }

    @Override
    public <T extends MobEffect> Holder<T> registerStatusEffect(
        String modID,
        String effectName,
        Supplier<T> statusEffect
    ) {
        return registerHolder(BuiltInRegistries.MOB_EFFECT, modID, effectName, statusEffect);
    }

    @Override
    public <T extends Fluid> Supplier<T> registerFluid(String modID, String fluidName, Supplier<T> fluid) {
        return registerSupplier(BuiltInRegistries.FLUID, modID, fluidName, fluid);
    }

    @Override
    public <E extends Mob> Supplier<SpawnEggItem> makeSpawnEggFor(
        Supplier<EntityType<E>> entityType,
        int primaryEggColour,
        int secondaryEggColour,
        Item.Properties itemProperties
    ) {
        return () -> new SpawnEggItem(entityType.get(), primaryEggColour, secondaryEggColour, itemProperties);
    }

    @Override
    public CreativeModeTab.Builder newCreativeTabBuilder() {
        return FabricItemGroup.builder();
    }
}
