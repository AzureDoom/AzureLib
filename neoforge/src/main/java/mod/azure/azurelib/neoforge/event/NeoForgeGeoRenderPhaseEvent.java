package mod.azure.azurelib.neoforge.event;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;

import mod.azure.azurelib.common.internal.common.event.GeoRenderEvent;
import mod.azure.azurelib.common.platform.services.GeoRenderPhaseEventFactory;

/**
 * @author Boston Vanseghi
 */
public class NeoForgeGeoRenderPhaseEvent implements GeoRenderPhaseEventFactory.GeoRenderPhaseEvent {

    // TODO: Move this.
    static class NeoForgeGeoRenderEvent extends Event implements ICancellableEvent {

        private final GeoRenderEvent geoRenderEvent;

        public NeoForgeGeoRenderEvent(GeoRenderEvent geoRenderEvent) {
            this.geoRenderEvent = geoRenderEvent;
        }

        public GeoRenderEvent getGeoRenderEvent() {
            return this.geoRenderEvent;
        }
    }

    @Override
    public boolean handle(GeoRenderEvent geoRenderEvent) {
        return NeoForge.EVENT_BUS.post(new NeoForgeGeoRenderEvent(geoRenderEvent)).hasResult();
    }
}
