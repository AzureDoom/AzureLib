package mod.azure.azurelib.animation.property.codec;

import net.minecraft.network.PacketBuffer;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.animation.easing.AzEasingType;
import mod.azure.azurelib.animation.easing.AzEasingTypeRegistry;
import mod.azure.azurelib.animation.easing.AzEasingTypes;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehavior;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviorRegistry;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import mod.azure.azurelib.animation.property.AzAnimationStageProperties;

public class AzAnimationStagePropertiesCodec {

    public static final Function<PacketBuffer, AzAnimationStageProperties> DECODER = buf -> {
        byte propertyLength = buf.readByte();
        AzAnimationStageProperties properties = AzAnimationStageProperties.EMPTY;

        for (int i = 0; i < propertyLength; i++) {
            byte code = buf.readByte();

            switch (code) {
                case 0: {
                    boolean hasAnimationSpeed = buf.readBoolean();
                    double animationSpeed = hasAnimationSpeed ? buf.readDouble() : 1D;
                    properties = properties.withAnimationSpeed(animationSpeed);
                }
                    break;
                case 1:
                    properties = properties.withTransitionLength(buf.readFloat());
                    break;
                case 2: {
                    AzEasingType easingType = AzEasingTypeRegistry.getOrDefault(
                        buf.readString(32767),
                        AzEasingTypes.NONE
                    );
                    properties = properties.withEasingType(easingType);
                }
                    break;
                case 3: {
                    AzPlayBehavior playBehavior = AzPlayBehaviorRegistry.getOrDefault(
                        buf.readString(32767),
                        AzPlayBehaviors.PLAY_ONCE
                    );
                    properties = properties.withPlayBehavior(playBehavior);
                }
                    break;
                case 4: {
                    boolean hasTickOffset = buf.readBoolean();
                    double startTickOffset = hasTickOffset ? buf.readDouble() : 0D;
                    properties = properties.withStartTickOffset(startTickOffset);
                }
                    break;
                case 5:
                    properties = properties.withFreezeTickOffset(buf.readDouble());
                    break;
                case 6:
                    properties = properties.withRepeatXTimes(buf.readDouble());
                    break;
                case 7:
                    properties = properties.withShouldReverse(buf.readBoolean());
                    break;
            }
        }

        return properties;
    };

    public static final BiConsumer<PacketBuffer, AzAnimationStageProperties> ENCODER = (buf, properties) -> {
        int propertyLength = 0;
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
            buf.writeString(properties.easingType().name());
        }

        if (properties.hasPlayBehavior()) {
            buf.writeByte(3);
            buf.writeString(properties.playBehavior().name());
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
