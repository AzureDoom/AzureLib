package mod.azure.azurelib.rewrite.animation.dispatch.command.sequence;

import net.minecraft.network.FriendlyByteBuf;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.rewrite.animation.dispatch.command.stage.AzAnimationStage;
import mod.azure.azurelib.rewrite.util.codec.AzListStreamCodec;

public class AzAnimationSequence {
    private final List<AzAnimationStage> stages;

    private static final AzListStreamCodec<AzAnimationStage> STAGE_LIST_CODEC =
        new AzListStreamCodec<>(AzAnimationStage.DECODER, AzAnimationStage.ENCODER);

    public static final Function<FriendlyByteBuf, AzAnimationSequence> DECODER = buf -> {
        List<AzAnimationStage> stages = STAGE_LIST_CODEC.decode(buf);
        return new AzAnimationSequence(stages);
    };

    public static final BiConsumer<FriendlyByteBuf, AzAnimationSequence> ENCODER = (buf, sequence) -> {
        STAGE_LIST_CODEC.encode(buf, sequence.stages());
    };

    public AzAnimationSequence(List<AzAnimationStage> stages) {
        this.stages = stages;
    }

    public List<AzAnimationStage> stages() {
        return stages;
    }

    @Override
    public String toString() {
        return "AzAnimationSequence{" +
                   "stages=" + stages +
                   '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AzAnimationSequence that = (AzAnimationSequence) o;

        return stages.equals(that.stages);
    }

    @Override
    public int hashCode() {
        return stages.hashCode();
    }
}

