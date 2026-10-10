package mod.azure.azurelib.animation.property.codec;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.animation.easing.AzEasingType;
import mod.azure.azurelib.animation.easing.AzEasingTypeRegistry;
import mod.azure.azurelib.animation.easing.AzEasingTypes;
import mod.azure.azurelib.animation.property.AzAnimationProperties;
import mod.azure.azurelib.network.AzByteBuf;

public class AzAnimationPropertiesCodec {

    public static final Function<AzByteBuf, AzAnimationProperties> DECODER = buf -> {
        byte propertyLength = buf.readByte();
        AzAnimationProperties properties = AzAnimationProperties.EMPTY;

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
                    boolean hasTickOffset = buf.readBoolean();
                    double startTickOffset = hasTickOffset ? buf.readDouble() : 0D;
                    properties = properties.withStartTickOffset(startTickOffset);
                }
                    break;
                case 4:
                    properties = properties.withFreezeTickOffset(buf.readDouble());
                    break;
                case 5:
                    properties = properties.withRepeatXTimes(buf.readDouble());
                    break;
                case 6:
                    properties = properties.withShouldReverse(buf.readBoolean());
                    break;
            }
        }

        return properties;
    };

    public static final BiConsumer<AzByteBuf, AzAnimationProperties> ENCODER = (buf, properties) -> {
        int propertyLength = 0;
        propertyLength += properties.hasAnimationSpeed() ? 1 : 0;
        propertyLength += properties.hasTransitionLength() ? 1 : 0;
        propertyLength += properties.hasEasingType() ? 1 : 0;
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

        if (properties.hasStartTickOffset()) {
            buf.writeByte(3);
            buf.writeDouble(properties.startTickOffset());
        }

        if (properties.hasFreezeTickOffset()) {
            buf.writeByte(4);
            buf.writeDouble(properties.freezeTickOffset());
        }

        if (properties.hasRepeatXTimes()) {
            buf.writeByte(5);
            buf.writeDouble(properties.repeatXTimes());
        }

        if (properties.hasReversing()) {
            buf.writeByte(6);
            buf.writeBoolean(properties.isReversing());
        }
    };
}
