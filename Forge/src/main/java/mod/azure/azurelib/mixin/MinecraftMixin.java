/**
 * This class is a fork of the matching class found in the Configuration repository.
 * Original source: https://github.com/Toma1O6/Configuration
 * Copyright © 2024 Toma1O6.
 * Licensed under the MIT License.
 */
/**
 * This class is a fork of the matching class found in the Geckolib repository.
 * Original source: https://github.com/Toma1O6/Configuration
 * Copyright © 2024 Toma1O6.
 * Licensed under the MIT License.
 */
package mod.azure.azurelib.mixin;

import mod.azure.azurelib.config.ConfigHolder;
import mod.azure.azurelib.config.io.ConfigIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.IWindowEventListener;
import net.minecraft.profiler.ISnooperInfo;
import net.minecraft.util.concurrent.RecursiveEventLoop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin extends RecursiveEventLoop<Runnable> implements ISnooperInfo, IWindowEventListener, net.minecraftforge.client.extensions.IForgeMinecraft {

    public MinecraftMixin(String p_i50401_1_) {
        super(p_i50401_1_);
    }

    @Inject(method = "clearLevel(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;resetData()V"))
    private void azurelib$reloadClientConfigs(Screen screen, CallbackInfo ci) {
        ConfigHolder.getSynchronizedConfigs().stream()
                .map(ConfigHolder::getConfig)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(ConfigIO::reloadClientValues);
    }
}
