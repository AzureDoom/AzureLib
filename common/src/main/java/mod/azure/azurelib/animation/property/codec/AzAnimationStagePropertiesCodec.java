package mod.azure.azurelib.animation.property.codec;

import net.minecraft.network.FriendlyByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.animation.easing.AzEasingTypeRegistry;
import mod.azure.azurelib.animation.easing.AzEasingTypes;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviorRegistry;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import mod.azure.azurelib.animation.property.AzAnimationStageProperties;

public class AzAnimationStagePropertiesCodec {

    public static final Function<FriendlyByteBuf, AzAnimationStageProperties> DECODER = buf -> {
        var propertyLength = buf.readByte();
        var properties = AzAnimationStageProperties.EMPTY;

        for (int i = 0; i < propertyLength; i++) {
            var code = buf.readByte();

            switch (code) {
                case 0 -> {
                    var animationSpeed = buf.readNullable(FriendlyByteBuf::readDouble);
                    properties = properties.withAnimationSpeed(animationSpeed != null ? animationSpeed : 1D);
                }
                case 1 -> {
                    var transitionLength = buf.readNullable(FriendlyByteBuf::readFloat);
                    properties = properties.withTransitionLength(transitionLength != null ? transitionLength : 0F);
                }
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
                case 5 -> properties = properties.withFreezeTickOffset(buf.readDouble());
                case 6 -> properties = properties.withRepeatXTimes(buf.readDouble());
                case 7 -> properties = properties.withShouldReverse(buf.readBoolean());
            }
        }

        return properties;
    };

    public static final BiConsumer<FriendlyByteBuf, AzAnimationStageProperties> ENCODER = (buf, properties) -> {
        var propertyLength = 0;
        propertyLength += properties.hasAnimationSpeed() ? 1 : 0;
        propertyLength += properties.hasTransitionLength() ? 1 : 0;
        propertyLength += properties.hasEasingType() ? 1 : 0;
        propertyLength += properties.hasPlayBehavior() ? 1 : 0;
        propertyLength += properties.hasStartTickOffset() ? 1 : 0;
        propertyLength += properties.hasFreezeTickOffset() ? 1 : 0;
        propertyLength += properties.hasRepeatXTimes() ? 1 : 0;
        propertyLength += properties.hasReversing() ? 1 : 0;

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

        if (properties.hasFreezeTickOffset()) {
            buf.writeByte(5);
            buf.writeDouble(properties.freezeTickOffset());
        }

        if (properties.hasRepeatXTimes()) {
            buf.writeByte(6);
            buf.writeDouble(properties.repeatXTimes());
        }

        if (properties.hasReversing()) {
            buf.writeByte(7);
            buf.writeBoolean(properties.isReversing());
        }
    };
}
