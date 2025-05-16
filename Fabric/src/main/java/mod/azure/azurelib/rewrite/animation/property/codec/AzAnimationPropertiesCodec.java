package mod.azure.azurelib.rewrite.animation.property.codec;

import mod.azure.azurelib.rewrite.animation.easing.AzEasingType;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.rewrite.animation.easing.AzEasingTypeRegistry;
import mod.azure.azurelib.rewrite.animation.easing.AzEasingTypes;
import mod.azure.azurelib.rewrite.animation.property.AzAnimationProperties;

public class AzAnimationPropertiesCodec {

    public static final Function<FriendlyByteBuf, AzAnimationProperties> DECODER = buf -> {
        byte propertyLength = buf.readByte();
        AzAnimationProperties properties = AzAnimationProperties.EMPTY;

        for (int i = 0; i < propertyLength; i++) {
            byte code = buf.readByte();

            switch (code) {
                case 0:
                    properties = properties.withAnimationSpeed(buf.readDouble());
                    break;
                case 1:
                    properties = properties.withTransitionLength(buf.readFloat());
                    break;
                case 2:
                    AzEasingType easingType = AzEasingTypeRegistry.getOrDefault(buf.readUtf(), AzEasingTypes.NONE);
                    properties = properties.withEasingType(easingType);
                    break;
            }

        }

        return properties;
    };

    public static final BiConsumer<FriendlyByteBuf, AzAnimationProperties> ENCODER = (buf, properties) -> {
        int propertyLength = 0;
        propertyLength += properties.hasAnimationSpeed() ? 1 : 0;
        propertyLength += properties.hasTransitionLength() ? 1 : 0;
        propertyLength += properties.hasEasingType() ? 1 : 0;

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
    };
}
