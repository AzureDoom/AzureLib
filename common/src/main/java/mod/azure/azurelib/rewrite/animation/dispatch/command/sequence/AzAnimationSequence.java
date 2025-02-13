package mod.azure.azurelib.rewrite.animation.dispatch.command.sequence;

import mod.azure.azurelib.rewrite.animation.dispatch.command.stage.AzAnimationStage;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

public record AzAnimationSequence(
    List<AzAnimationStage> stages
) {

    public static void encode(FriendlyByteBuf buf, AzAnimationSequence sequence) {
        List<AzAnimationStage> stages = sequence.stages();
        buf.writeInt(stages.size());
        for (AzAnimationStage stage : stages) {
            stage.encode(buf, stage);
        }
    }

    public AzAnimationSequence decode(FriendlyByteBuf buf) {
        return new AzAnimationSequence(stages);
    }

}
