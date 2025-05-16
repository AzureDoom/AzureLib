package mod.azure.azurelib.mixins.fabric;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

import mod.azure.azurelib.config.ConfigHolder;
import mod.azure.azurelib.network.AzureLibNetwork;
import mod.azure.azurelib.network.S2C_SendConfigData;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void configuration_sendServerConfigs(Connection connection, ServerPlayer player, CallbackInfo ci) {
        Set<String> set = ConfigHolder.getSynchronizedConfigs();
        set.forEach(id -> AzureLibNetwork.sendClientPacket(player, new S2C_SendConfigData(id)));
    }
}
