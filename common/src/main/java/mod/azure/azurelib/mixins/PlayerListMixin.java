/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
package mod.azure.azurelib.mixins;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

import mod.azure.azurelib.config.ConfigHolder;
import mod.azure.azurelib.platform.Services;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void azurelib$sendServerConfigs(Connection connection, ServerPlayer player, CallbackInfo ci) {
        Set<String> set = ConfigHolder.getSynchronizedConfigs();
        set.forEach(id -> Services.NETWORK.sendClientPacket(player, id));
    }
}
