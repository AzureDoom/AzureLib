package mod.azure.azurelib.common.simple;

import mod.azure.azurelib.common.animation.pose.AzPoseBone;

@FunctionalInterface
public interface AzSimpleBoneCallback<T> {

    void apply(T animatable, AzPoseBone bone, float partialTick);
}
