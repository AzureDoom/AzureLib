package mod.azure.azurelib.rewrite.animation.dispatch.command.sequence;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

import mod.azure.azurelib.rewrite.animation.dispatch.command.stage.AzAnimationStage;
import mod.azure.azurelib.rewrite.util.codec.AzListStreamCodec;

/**
 * Represents an animation sequence comprised of an ordered list of {@link AzAnimationStage} objects.
 * Each stage within the sequence defines a step in the overall animation process.
 *
 * The {@code AzAnimationSequence} class supports serialization and deserialization using a stream-based codec,
 * enabling efficient transmission and storage of animation sequences. The codec is defined as a public static
 * constant, allowing for standardized encoding and decoding of this type.
 *
 * Instances of this class are immutable and must be constructed with a complete list of animation stages.
 * It is typically used in scenarios where sequential animation definitions are required, such as building
 * or playing composite animations.
 *
 * Features:
 * - A list of animation stages defining the sequence.
 * - An associated {@code StreamCodec} for encoding and decoding.
 */
public record AzAnimationSequence(
    List<AzAnimationStage> stages
) {

    /**
     * A codec for encoding and decoding instances of {@link AzAnimationSequence}.
     * <p>
     * The codec's functionality is defined as follows:
     * - The stages of the animation sequence are encoded as a list using {@link AzListStreamCodec}.
     * - The fields of {@code AzAnimationSequence} are mapped to this codec using method references,
     *   namely {@link AzAnimationSequence#stages()} and the constructor {@link AzAnimationSequence#AzAnimationSequence(List)}.
     */
    public static final StreamCodec<FriendlyByteBuf, AzAnimationSequence> CODEC = StreamCodec.composite(
        new AzListStreamCodec<>(AzAnimationStage.CODEC),
        AzAnimationSequence::stages,
        AzAnimationSequence::new
    );
}
