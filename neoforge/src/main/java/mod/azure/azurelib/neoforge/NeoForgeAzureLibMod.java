package mod.azure.azurelib.neoforge;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import mod.azure.azurelib.common.api.common.enchantments.IncendiaryEnchantment;
import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.common.AzureLibMod;
import mod.azure.azurelib.common.internal.common.blocks.TickingLightBlock;
import mod.azure.azurelib.common.internal.common.blocks.TickingLightEntity;
import mod.azure.azurelib.common.internal.common.config.AzureLibConfig;
import mod.azure.azurelib.common.internal.common.config.format.ConfigFormats;
import mod.azure.azurelib.common.internal.common.config.io.ConfigIO;
import mod.azure.azurelib.common.internal.common.network.packet.*;
import mod.azure.azurelib.common.platform.services.AzureLibNetwork;
import mod.azure.azurelib.neoforge.network.S2C_NeoSendConfigData;
import mod.azure.azurelib.neoforge.platform.NeoForgeAzureLibNetwork;

@Mod(AzureLib.MOD_ID)
public final class NeoForgeAzureLibMod {

    public static NeoForgeAzureLibMod instance;

    public NeoForgeAzureLibMod(IEventBus modEventBus) {
        instance = this;
        AzureLib.initialize();
        modEventBus.addListener(this::registerPackets);
        AzureLibMod.config = AzureLibMod.registerConfig(AzureLibConfig.class, ConfigFormats.json()).getConfigInstance();
        modEventBus.addListener(this::init);
        AzureEnchantments.ENCHANTMENTS.register(modEventBus);
        AzureBlocks.BLOCKS.register(modEventBus);
        AzureEntities.TILE_TYPES.register(modEventBus);
    }

    private void init(FMLCommonSetupEvent event) {
        ConfigIO.FILE_WATCH_MANAGER.startService();
    }

    private void registerPackets(final RegisterPayloadHandlerEvent ev) {
        final IPayloadRegistrar registrar = ev.registrar(AzureLib.MOD_ID);

        registrar.play(
            AzureLibNetwork.ANIM_DATA_SYNC_PACKET_ID,
            AnimDataSyncPacket::receive,
            NeoForgeAzureLibNetwork::handlePacket
        );
        registrar.play(
            AzureLibNetwork.ANIM_TRIGGER_SYNC_PACKET_ID,
            AnimTriggerPacket::receive,
            NeoForgeAzureLibNetwork::handlePacket
        );
        registrar.play(
            AzureLibNetwork.ENTITY_ANIM_DATA_SYNC_PACKET_ID,
            EntityAnimDataSyncPacket::receive,
            NeoForgeAzureLibNetwork::handlePacket
        );
        registrar.play(
            AzureLibNetwork.ENTITY_ANIM_TRIGGER_SYNC_PACKET_ID,
            EntityAnimTriggerPacket::receive,
            NeoForgeAzureLibNetwork::handlePacket
        );
        registrar.play(
            AzureLibNetwork.BLOCK_ENTITY_ANIM_DATA_SYNC_PACKET_ID,
            BlockEntityAnimDataSyncPacket::receive,
            NeoForgeAzureLibNetwork::handlePacket
        );
        registrar.play(
            AzureLibNetwork.BLOCK_ENTITY_ANIM_TRIGGER_SYNC_PACKET_ID,
            BlockEntityAnimTriggerPacket::receive,
            NeoForgeAzureLibNetwork::handlePacket
        );
        registrar.play(
            AzureLibNetwork.CONFIG_PACKET_ID,
            S2C_NeoSendConfigData::receive,
            NeoForgeAzureLibNetwork::handlePacket
        );
    }

    public class AzureEnchantments {

        public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(
            Registries.ENCHANTMENT,
            AzureLib.MOD_ID
        );

        public static final Supplier<Enchantment> INCENDIARYENCHANTMENT = ENCHANTMENTS.register(
            "incendiaryenchantment",
            () -> new IncendiaryEnchantment(Enchantment.Rarity.RARE, EquipmentSlot.MAINHAND)
        );
    }

    public class AzureBlocks {

        public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, AzureLib.MOD_ID);

        public static final Supplier<TickingLightBlock> TICKING_LIGHT_BLOCK = BLOCKS.register(
            "lightblock",
            () -> new TickingLightBlock(
                BlockBehaviour.Properties.of()
                    .sound(SoundType.CANDLE)
                    .lightLevel(
                        TickingLightBlock.litBlockEmission(15)
                    )
                    .pushReaction(PushReaction.DESTROY)
                    .noOcclusion()
            )
        );
    }

    public class AzureEntities {

        public static final DeferredRegister<BlockEntityType<?>> TILE_TYPES = DeferredRegister.create(
            Registries.BLOCK_ENTITY_TYPE,
            AzureLib.MOD_ID
        );

        public static final Supplier<BlockEntityType<TickingLightEntity>> TICKING_LIGHT_ENTITY = TILE_TYPES.register(
            "lightblock",
            () -> BlockEntityType.Builder.of(TickingLightEntity::new, AzureBlocks.TICKING_LIGHT_BLOCK.get())
                .build(
                    null
                )
        );
    }
}
