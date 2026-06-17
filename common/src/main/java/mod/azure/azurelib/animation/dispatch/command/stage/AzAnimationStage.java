package mod.azure.azurelib.animation.dispatch.command.stage;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import mod.azure.azurelib.animation.property.AzAnimationStageProperties;

/**
 * Represents a single stage of an animation along with its associated properties.
 * <p>
 * An instance of {@code AzAnimationStage} contains: - A name identifying the animation stage. - A set of properties
 * defined by {@link AzAnimationStageProperties}, describing the behavior and characteristics of the animation stage.
 */
public record AzAnimationStage(
    String name,
    AzAnimationStageProperties properties
) {

    /**
     * A codec implementation for serializing and deserializing instances of {@link AzAnimationStage}.
     * <p>
     * The following fields of the {@code AzAnimationStage} are encoded and decoded: - The name of the animation stage
     * ({@code String}), utilizing UTF-8 encoding. - The associated properties ({@link AzAnimationStageProperties}),
     * referencing its codec for composite serialization.
     */
    public static final StreamCodec<FriendlyByteBuf, AzAnimationStage> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        AzAnimationStage::name,
        AzAnimationStageProperties.CODEC,
        AzAnimationStage::properties,
        AzAnimationStage::new
    );

}
