package mod.azure.azurelib.animation.dispatch.command.sequence;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.animation.dispatch.command.stage.AzAnimationStage;
import mod.azure.azurelib.network.AzByteBuf;
import mod.azure.azurelib.util.codec.AzListStreamCodec;

public final class AzAnimationSequence {

    private final List<AzAnimationStage> stages;

    public AzAnimationSequence(List<AzAnimationStage> stages) {
        this.stages = stages;
    }

    public List<AzAnimationStage> stages() {
        return this.stages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzAnimationSequence))
            return false;
        AzAnimationSequence other = (AzAnimationSequence) o;
        return java.util.Objects.equals(this.stages, other.stages);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.stages);
        return result;
    }

    @Override
    public String toString() {
        return "AzAnimationSequence[stages=" + this.stages + "]";
    }

    private static final AzListStreamCodec<AzAnimationStage> STAGE_LIST_CODEC =
        new AzListStreamCodec<>(AzAnimationStage.DECODER, AzAnimationStage.ENCODER);

    public static final Function<AzByteBuf, AzAnimationSequence> DECODER = buf -> {
        List<AzAnimationStage> stages = STAGE_LIST_CODEC.decode(buf);
        return new AzAnimationSequence(stages);
    };

    public static final BiConsumer<AzByteBuf, AzAnimationSequence> ENCODER = (buf, sequence) -> {
        STAGE_LIST_CODEC.encode(buf, sequence.stages());
    };

}
