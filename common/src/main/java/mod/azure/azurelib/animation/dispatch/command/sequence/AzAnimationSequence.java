package mod.azure.azurelib.animation.dispatch.command.sequence;

import net.minecraft.network.FriendlyByteBuf;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.animation.dispatch.command.stage.AzAnimationStage;
import mod.azure.azurelib.util.codec.AzListStreamCodec;

public record AzAnimationSequence(
    List<AzAnimationStage> stages
) {

    private static final AzListStreamCodec<AzAnimationStage> STAGE_LIST_CODEC =
        new AzListStreamCodec<>(AzAnimationStage.DECODER, AzAnimationStage.ENCODER);

    public static final Function<FriendlyByteBuf, AzAnimationSequence> DECODER = buf -> {
        List<AzAnimationStage> stages = STAGE_LIST_CODEC.decode(buf);
        return new AzAnimationSequence(stages);
    };

    public static final BiConsumer<FriendlyByteBuf, AzAnimationSequence> ENCODER = (buf, sequence) -> {
        STAGE_LIST_CODEC.encode(buf, sequence.stages());
    };

}
