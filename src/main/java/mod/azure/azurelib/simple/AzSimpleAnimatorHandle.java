package mod.azure.azurelib.simple;

import mod.azure.azurelib.animation.dispatch.AzDispatchSide;

public interface AzSimpleAnimatorHandle {

    void handleSimpleAction(AzDispatchSide originSide, AzSimplePlayAction action);
}
