package mod.azure.azurelib;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import mod.azure.azurelib.client.AzureLibClient;
import mod.azure.azurelib.config.ConfigHolder;
import mod.azure.azurelib.rewrite.render.armor.AzArmorRendererRegistry;
import mod.azure.azurelib.rewrite.render.item.AzItemRendererRegistry;
import mod.azure.azurelib.testing.armor.DoomicornArmorRenderer;
import mod.azure.azurelib.testing.block.be.StargateBlockRenderer;
import mod.azure.azurelib.testing.entity.MarauderRenderer;
import mod.azure.azurelib.testing.item.PistolRenderer;

@EventBusSubscriber(modid = AzureLib.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModListener {

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        AzItemRendererRegistry.register(NeoForgeAzureLibMod.AzureItems.PISTOL_ITEM.get(), PistolRenderer::new);
        AzArmorRendererRegistry.register(
            DoomicornArmorRenderer::new,
            NeoForgeAzureLibMod.AzureItems.DOOMICORN_HELMET.get(),
            NeoForgeAzureLibMod.AzureItems.DOOMICORN_CHESTPLATE.get(),
            NeoForgeAzureLibMod.AzureItems.DOOMICORN_LEGGINGS.get(),
            NeoForgeAzureLibMod.AzureItems.DOOMICORN_BOOTS.get()
        );
    }

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(NeoForgeAzureLibMod.AzureEntities.MARAUDER.get(), MarauderRenderer::new);
        event.registerBlockEntityRenderer(
            NeoForgeAzureLibMod.AzureEntities.STARGATE_BLOCK_ENTITY.get(),
            (BlockEntityRendererProvider.Context rendererDispatcherIn) -> new StargateBlockRenderer()
        );
    }

    @SubscribeEvent
    public static void registerKeys(final RegisterKeyMappingsEvent event) {
        Keybindings.RELOAD = new KeyMapping(
            "key.azurelib.reload",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "category.azurelib.binds"
        );
        event.register(Keybindings.RELOAD);
        Keybindings.SCOPE = new KeyMapping(
            "key.azurelib.scope",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            "category.azurelib.binds"
        );
        event.register(Keybindings.SCOPE);
        Keybindings.FIRE_WEAPON = new KeyMapping(
            "key.azurelib.fire",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "category.azurelib.binds"
        );
        event.register(Keybindings.FIRE_WEAPON);
    }

    @SubscribeEvent
    public static void clientInit(final FMLClientSetupEvent event) {
        Map<String, List<ConfigHolder<?>>> groups = ConfigHolder.getConfigGroupingByGroup();
        ModList modList = ModList.get();
        for (Map.Entry<String, List<ConfigHolder<?>>> entry : groups.entrySet()) {
            String modId = entry.getKey();
            Optional<? extends ModContainer> optional = modList.getModContainerById(modId);
            optional.ifPresent(modContainer -> {
                List<ConfigHolder<?>> list = entry.getValue();
                modContainer.registerExtensionPoint(
                    ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> {
                        if (list.size() == 1) {
                            return AzureLibClient.getConfigScreen(list.get(0).getConfigId(), screen);
                        }
                        return AzureLibClient.getConfigScreenByGroup(list, modId, screen);
                    })
                );
            });
        }
    }
}
