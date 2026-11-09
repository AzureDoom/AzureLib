package mod.azure.azurelib.simple;

import mod.azure.azurelib.animation.pose.AzPoseBone;

@FunctionalInterface
public interface AzSimpleBoneCallback<T> {

    void apply(T animatable, AzPoseBone bone, float partialTick);
}
