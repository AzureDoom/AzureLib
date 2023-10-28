package mod.azure.azurelib.platform.services;

import mod.azure.azurelib.event.GeoRenderEvent;

public interface GeoRenderPhaseEventFactory {

    interface GeoRenderPhaseEvent {
        boolean handle(GeoRenderEvent geoRenderEvent);
    }

    GeoRenderPhaseEvent create();
}
