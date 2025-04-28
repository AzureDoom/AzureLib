package mod.azure.azurelib.rewrite.animation.dispatch.command.stage;

import mod.azure.azurelib.rewrite.animation.property.AzAnimationStageProperties;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record AzAnimationStage(
    String name,
    AzAnimationStageProperties properties
) {

    public static final Function<FriendlyByteBuf, AzAnimationStage> DECODER = buf -> {
        String name = buf.readUtf();
        AzAnimationStageProperties properties = AzAnimationStageProperties.DECODER.apply(buf);
        return new AzAnimationStage(name, properties);
    };

    public static final BiConsumer<FriendlyByteBuf, AzAnimationStage> ENCODER = (buf, stage) -> {
        buf.writeUtf(stage.name());
        AzAnimationStageProperties.ENCODER.accept(buf, stage.properties());
    };


}
