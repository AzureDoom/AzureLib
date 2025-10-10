package mod.azure.azurelib.common.animation.property.codec;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import mod.azure.azurelib.common.animation.easing.AzEasingTypeRegistry;
import mod.azure.azurelib.common.animation.easing.AzEasingTypes;
import mod.azure.azurelib.common.animation.property.AzAnimationProperties;

public class AzAnimationPropertiesCodec implements StreamCodec<FriendlyByteBuf, AzAnimationProperties> {

    @Override
    public @NotNull AzAnimationProperties decode(FriendlyByteBuf buf) {
        var propertyLength = buf.readByte();
        var properties = AzAnimationProperties.EMPTY;

        for (int i = 0; i < propertyLength; i++) {
            var code = buf.readByte();

            switch (code) {
                case 0 -> properties = properties.withAnimationSpeed(buf.readDouble());
                case 1 -> properties.withTransitionLength(buf.readFloat());
                case 2 -> {
                    var easingType = AzEasingTypeRegistry.getOrDefault(buf.readUtf(), AzEasingTypes.NONE);
                    properties = properties.withEasingType(easingType);
                }
                case 3 -> properties = properties.withStartTickOffset(buf.readDouble());
                case 4 -> properties = properties.withFreezeTickOffset(buf.readDouble());
                case 5 -> properties = properties.withRepeatXTimes(buf.readDouble());
                case 6 -> properties = properties.withShouldReverse(buf.readBoolean());
            }
        }

        return properties;
    }

    @Override
    public void encode(FriendlyByteBuf buf, AzAnimationProperties properties) {
        var propertyLength = 0;
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
            buf.writeUtf(properties.easingType().name());
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
    }
}
