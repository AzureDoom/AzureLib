package mod.azure.azurelib;

import com.mojang.blaze3d.platform.InputConstants;
import mod.azure.azurelib.network.Networking;
import mod.azure.azurelib.platform.Services;
import mod.azure.azurelib.rewrite.render.armor.AzArmorRendererRegistry;
import mod.azure.azurelib.rewrite.render.item.AzItemRendererRegistry;
import mod.azure.azurelib.testing.armor.DoomicornArmorRenderer;
import mod.azure.azurelib.testing.block.be.StargateBlockRenderer;
import mod.azure.azurelib.testing.entity.MarauderRenderer;
import mod.azure.azurelib.testing.item.PistolRenderer;
import mod.azure.azurelib.util.IncompatibleModsCheck;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import org.lwjgl.glfw.GLFW;

public final class ClientListener implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(IncompatibleModsCheck::warnings);
        Keybindings.RELOAD = new KeyMapping("key.azurelib.reload", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R,
                "category.azurelib.binds");
        KeyBindingHelper.registerKeyBinding(Keybindings.RELOAD);
        Keybindings.SCOPE = new KeyMapping("key.azurelib.scope", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT,
                "category.azurelib.binds");
        KeyBindingHelper.registerKeyBinding(Keybindings.SCOPE);
        Keybindings.FIRE_WEAPON = new KeyMapping("key.azurelib.fire", InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN, "category.azurelib.binds");
        KeyBindingHelper.registerKeyBinding(Keybindings.FIRE_WEAPON);
        Services.NETWORK.registerClientReceiverPackets();
        Networking.PacketRegistry.registerClient();

        AzItemRendererRegistry.register(FabricAzureLibMod.PISTOL_ITEM, PistolRenderer::new);
        AzArmorRendererRegistry.register(
                DoomicornArmorRenderer::new,
                FabricAzureLibMod.DOOMICORN_HELMET,
                FabricAzureLibMod.DOOMICORN_CHESTPLATE,
                FabricAzureLibMod.DOOMICORN_LEGGINGS,
                FabricAzureLibMod.DOOMICORN_BOOTS
        );
        BlockRenderLayerMap.INSTANCE.putBlock(FabricAzureLibMod.STARGATE_BLOCK, RenderType.translucent());
        BlockEntityRenderers.register(
                FabricAzureLibMod.STARGATE_BLOCK_ENTITY,
                (BlockEntityRendererProvider.Context rendererDispatcherIn) -> new StargateBlockRenderer()
        );
        EntityRendererRegistry.register(FabricAzureLibMod.MARAUDER, MarauderRenderer::new);
    }
}
