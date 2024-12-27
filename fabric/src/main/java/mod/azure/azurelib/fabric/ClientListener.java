package mod.azure.azurelib.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import mod.azure.azurelib.fabric.core2.example.entities.ovamorph.OvamorphRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import org.lwjgl.glfw.GLFW;

import mod.azure.azurelib.common.api.client.helper.ClientUtils;
import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.common.network.packet.*;
import mod.azure.azurelib.core2.render.armor.AzArmorRendererRegistry;
import mod.azure.azurelib.core2.render.item.AzItemRendererRegistry;
import mod.azure.azurelib.fabric.core2.example.ExampleEntityTypes;
import mod.azure.azurelib.fabric.core2.example.armors.AzDoomArmorRenderer;
import mod.azure.azurelib.fabric.core2.example.blocks.StargateRender;
import mod.azure.azurelib.fabric.core2.example.entities.doomhunter.DoomHunterRenderer;
import mod.azure.azurelib.fabric.core2.example.entities.marauder.MarauderRenderer;
import mod.azure.azurelib.fabric.core2.example.items.AzPistolRenderer;

public final class ClientListener implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        if (AzureLib.hasKeyBindsInitialized) {
            ClientUtils.RELOAD = new KeyMapping(
                "key.azurelib.reload",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.azurelib.binds"
            );
            KeyBindingHelper.registerKeyBinding(ClientUtils.RELOAD);
            ClientUtils.SCOPE = new KeyMapping(
                "key.azurelib.scope",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
                "category.azurelib.binds"
            );
            KeyBindingHelper.registerKeyBinding(ClientUtils.SCOPE);
            ClientUtils.FIRE_WEAPON = new KeyMapping(
                "key.azurelib.fire",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category.azurelib.binds"
            );
            KeyBindingHelper.registerKeyBinding(ClientUtils.FIRE_WEAPON);
        }
        ClientPlayNetworking.registerGlobalReceiver(
            BlockEntityAnimTriggerPacket.TYPE,
            (packet, context) -> packet.handle()
        );
        ClientPlayNetworking.registerGlobalReceiver(
            BlockEntityAnimDataSyncPacket.TYPE,
            (packet, context) -> packet.handle()
        );
        ClientPlayNetworking.registerGlobalReceiver(EntityAnimTriggerPacket.TYPE, (packet, context) -> packet.handle());
        ClientPlayNetworking.registerGlobalReceiver(
            AzEntityDispatchCommandPacket.TYPE,
            (packet, context) -> packet.handle()
        );
        ClientPlayNetworking.registerGlobalReceiver(
            AzItemStackDispatchCommandPacket.TYPE,
            (packet, context) -> packet.handle()
        );
        ClientPlayNetworking.registerGlobalReceiver(
            AzBlockEntityDispatchCommandPacket.TYPE,
            (packet, context) -> packet.handle()
        );
        ClientPlayNetworking.registerGlobalReceiver(
            EntityAnimDataSyncPacket.TYPE,
            (packet, context) -> packet.handle()
        );
        ClientPlayNetworking.registerGlobalReceiver(AnimTriggerPacket.TYPE, (packet, context) -> packet.handle());
        ClientPlayNetworking.registerGlobalReceiver(AnimDataSyncPacket.TYPE, (packet, context) -> packet.handle());
        ClientPlayNetworking.registerGlobalReceiver(SendConfigDataPacket.TYPE, (packet, context) -> packet.handle());

        AzItemRendererRegistry.register(FabricAzureLibMod.AZ_PISTOL, AzPistolRenderer::new);
        AzArmorRendererRegistry.register(
            AzDoomArmorRenderer::new,
            FabricAzureLibMod.AZ_DOOM_HELMET,
            FabricAzureLibMod.AZ_DOOM_CHESTPLATE,
            FabricAzureLibMod.AZ_DOOM_LEGGINGS,
            FabricAzureLibMod.AZ_DOOM_BOOTS
        );
        EntityRendererRegistry.register(ExampleEntityTypes.DOOMHUNTER, DoomHunterRenderer::new);
        EntityRendererRegistry.register(ExampleEntityTypes.MARAUDER, MarauderRenderer::new);
        EntityRendererRegistry.register(ExampleEntityTypes.OVAMORPH, OvamorphRenderer::new);
        BlockRenderLayerMap.INSTANCE.putBlock(FabricAzureLibMod.STARGATE, RenderType.translucent());
        BlockEntityRenderers.register(
            ExampleEntityTypes.STARGATE,
            (BlockEntityRendererProvider.Context rendererDispatcherIn) -> new StargateRender()
        );
    }
}
