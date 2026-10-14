package mod.azure.azurelib.platform;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.event.registry.MessageListenerRegistryEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.EntrypointManager;

import java.lang.invoke.MethodHandles;

/**
 * StationAPI event-bus entrypoint ({@code stationapi:event_bus} in {@code fabric.mod.json}).
 */
public final class AzureLibStationEvents {

    static {
        EntrypointManager.registerLookup(MethodHandles.lookup());
    }

    @EventListener
    private static void registerMessages(MessageListenerRegistryEvent event) {
        // Server-to-client only: the client side of the listener decodes and runs AzureLib's packets.
        event.register(
            StationAzureLibNetwork.channelId(),
            (player, message) -> StationAzureLibNetwork.receive(message.bytes)
        );
    }
}
