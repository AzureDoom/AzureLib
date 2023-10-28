package mod.azure.azurelib.platform;

import mod.azure.azurelib.event.FabricGeoRenderPhaseEvent;
import mod.azure.azurelib.platform.services.GeoRenderPhaseEventFactory;

/**
 * @author Boston Vanseghi
 */
public class FabricGeoRenderPhaseEventFactory implements GeoRenderPhaseEventFactory {
    @Override
    public GeoRenderPhaseEvent create() {
        return new FabricGeoRenderPhaseEvent();
    }
}
