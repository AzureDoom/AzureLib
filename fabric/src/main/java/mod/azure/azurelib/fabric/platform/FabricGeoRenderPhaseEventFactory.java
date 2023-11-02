package mod.azure.azurelib.fabric.platform;

import mod.azure.azurelib.fabric.event.FabricGeoRenderPhaseEvent;
import mod.azure.azurelib.common.platform.services.GeoRenderPhaseEventFactory;

/**
 * @author Boston Vanseghi
 */
public class FabricGeoRenderPhaseEventFactory implements GeoRenderPhaseEventFactory {
    @Override
    public GeoRenderPhaseEvent create() {
        return new FabricGeoRenderPhaseEvent();
    }
}
