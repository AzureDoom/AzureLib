package mod.azure.azurelib.common.simple;

import mod.azure.azurelib.common.animation.dispatch.AzDispatchSide;

public interface AzSimpleAnimatorHandle {

    void handleSimpleAction(AzDispatchSide originSide, AzSimplePlayAction action);
}
