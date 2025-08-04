package mod.azure.azurelib.rewrite.animation.property.codec;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import mod.azure.azurelib.rewrite.animation.easing.AzEasingTypeRegistry;
import mod.azure.azurelib.rewrite.animation.easing.AzEasingTypes;
import mod.azure.azurelib.rewrite.animation.play_behavior.AzPlayBehaviorRegistry;
import mod.azure.azurelib.rewrite.animation.play_behavior.AzPlayBehaviors;
import mod.azure.azurelib.rewrite.animation.property.AzAnimationStageProperties;

public class AzAnimationStagePropertiesCodec implements StreamCodec<FriendlyByteBuf, AzAnimationStageProperties> {

    @Override
    public @NotNull AzAnimationStageProperties decode(FriendlyByteBuf buf) {
        var propertyLength = buf.readByte();
        var properties = AzAnimationStageProperties.DEFAULT;

        for (int i = 0; i < propertyLength; i++) {
            var code = buf.readByte();

            switch (code) {
                case 0 -> {
                    var animationSpeed = buf.readNullable(FriendlyByteBuf::readDouble);
                    properties = properties.withAnimationSpeed(animationSpeed != null ? animationSpeed : 1D);
                }
                case 1 -> properties = properties.withTransitionLength(buf.readFloat());
                case 2 -> {
                    var easingType = AzEasingTypeRegistry.getOrDefault(buf.readUtf(), AzEasingTypes.NONE);
                    properties = properties.withEasingType(easingType);
                }
                case 3 -> {
                    var playBehavior = AzPlayBehaviorRegistry.getOrDefault(buf.readUtf(), AzPlayBehaviors.PLAY_ONCE);
                    properties = properties.withPlayBehavior(playBehavior);
                }
                case 4 -> {
                    var startTickOffset = buf.readNullable(FriendlyByteBuf::readDouble);
                    properties = properties.withStartTickOffset(startTickOffset != null ? startTickOffset : 0D);
                }
            }
        }

        return properties;
    }

    @Override
    public void encode(FriendlyByteBuf buf, AzAnimationStageProperties properties) {
        var propertyLength = 0;
        propertyLength += properties.hasAnimationSpeed() ? 1 : 0;
        propertyLength += properties.hasTransitionLength() ? 1 : 0;
        propertyLength += properties.hasEasingType() ? 1 : 0;
        propertyLength += properties.hasPlayBehavior() ? 1 : 0;
        propertyLength += properties.hasStartTickOffset() ? 1 : 0;

        buf.writeByte(propertyLength);

        if (properties.hasAnimationSpeed()) {
            buf.writeByte(0);
            buf.writeDouble(properties.animationSpeed());
        }

        if (properties.hasTransitionLength()) {
            buf.writeByte(1);
            buf.writeFloat(properties.transitionLength());
        }

        if (properties.hasEasingType()) {
            buf.writeByte(2);
            buf.writeUtf(properties.easingType().name());
        }

        if (properties.hasPlayBehavior()) {
            buf.writeByte(3);
            buf.writeUtf(properties.playBehavior().name());
        }

        if (properties.hasStartTickOffset()) {
            buf.writeByte(4);
            buf.writeDouble(properties.startTickOffset());
        }
    }
}
