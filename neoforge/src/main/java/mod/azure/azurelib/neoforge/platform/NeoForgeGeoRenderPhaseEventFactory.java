package mod.azure.azurelib.neoforge.platform;

import mod.azure.azurelib.common.platform.services.GeoRenderPhaseEventFactory;
import mod.azure.azurelib.neoforge.event.NeoForgeGeoRenderPhaseEvent;

/**
 * @author Boston Vanseghi
 */
public class NeoForgeGeoRenderPhaseEventFactory implements GeoRenderPhaseEventFactory {
    @Override
    public GeoRenderPhaseEvent create() {
        return new NeoForgeGeoRenderPhaseEvent();
    }
}
