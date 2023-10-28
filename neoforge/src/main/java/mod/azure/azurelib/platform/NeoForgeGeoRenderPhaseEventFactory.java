package mod.azure.azurelib.platform;

import mod.azure.azurelib.event.NeoForgeGeoRenderPhaseEvent;
import mod.azure.azurelib.platform.services.GeoRenderPhaseEventFactory;

/**
 * @author Boston Vanseghi
 */
public class NeoForgeGeoRenderPhaseEventFactory implements GeoRenderPhaseEventFactory {
    @Override
    public GeoRenderPhaseEvent create() {
        return new NeoForgeGeoRenderPhaseEvent();
    }
}
