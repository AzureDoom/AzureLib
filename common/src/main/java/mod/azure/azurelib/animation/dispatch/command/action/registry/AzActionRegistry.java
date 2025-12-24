package mod.azure.azurelib.animation.dispatch.command.action.registry;

import it.unimi.dsi.fastutil.objects.Object2ShortArrayMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.animation.dispatch.command.action.AzAction;
import mod.azure.azurelib.animation.dispatch.command.action.impl.controller.*;
import mod.azure.azurelib.animation.dispatch.command.action.impl.root.*;

/**
 * The AzActionRegistry class serves as a centralized registry for mapping {@link AzAction} implementations to their
 * associated {@link ResourceLocation} identifiers and corresponding encoders/decoders. This registry enables efficient
 * encoding, decoding, and dispatching of animation-related actions within the animation system. <br>
 * Key Responsibilities:
 * <ul>
 * <li>Maintain a bidirectional mapping between {@link ResourceLocation} identifiers and short integer IDs for efficient
 * serialization/deserialization.</li>
 * <li>Register {@link AzAction} implementations and their corresponding encoders and decoders.</li>
 * <li>Provide methods for retrieving encoders/decoders and IDs based on resource locations or integer IDs.
 * </ul>
 */
public class AzActionRegistry {

    // Mappings for resource location to ID and encoders/decoders by ID
    private static final Map<ResourceLocation, Short> RESOURCE_LOCATION_TO_ID = new Object2ShortArrayMap<>();

    private static final Map<Short, Function<FriendlyByteBuf, AzAction>> DECODERS_BY_ID = new HashMap<>();

    private static final Map<Short, BiConsumer<FriendlyByteBuf, AzAction>> ENCODERS_BY_ID = new HashMap<>();

    private static short NEXT_FREE_ID = 0;

    static {
        // Register root actions
        register(
            AzRootCancelAllAction.RESOURCE_LOCATION,
            AzRootCancelAllAction::decode,
            AzRootCancelAllAction::encode
        );
        register(
            AzRootPlayAnimationSequenceAction.RESOURCE_LOCATION,
            AzRootPlayAnimationSequenceAction::decode,
            AzRootPlayAnimationSequenceAction::encode
        );
        register(
            AzRootSetAnimationSpeedAction.RESOURCE_LOCATION,
            AzRootSetAnimationSpeedAction::decode,
            AzRootSetAnimationSpeedAction::encode
        );
        register(
            AzRootSetEasingTypeAction.RESOURCE_LOCATION,
            AzRootSetEasingTypeAction::decode,
            AzRootSetEasingTypeAction::encode
        );
        register(
            AzRootSetFreezeTickAction.RESOURCE_LOCATION,
            AzRootSetFreezeTickAction::decode,
            AzRootSetFreezeTickAction::encode
        );
        register(
            AzRootSetRepeatTimesAction.RESOURCE_LOCATION,
            AzRootSetRepeatTimesAction::decode,
            AzRootSetRepeatTimesAction::encode
        );
        register(
            AzRootSetReverseAction.RESOURCE_LOCATION,
            AzRootSetReverseAction::decode,
            AzRootSetReverseAction::encode
        );
        register(
            AzRootSetTransitionSpeedAction.RESOURCE_LOCATION,
            AzRootSetTransitionSpeedAction::decode,
            AzRootSetTransitionSpeedAction::encode
        );
        register(
            AzRootSetStartTickOffsetAction.RESOURCE_LOCATION,
            AzRootSetStartTickOffsetAction::decode,
            AzRootSetStartTickOffsetAction::encode
        );

        // Register controller actions
        register(
            AzControllerCancelAction.RESOURCE_LOCATION,
            AzControllerCancelAction::decode,
            AzControllerCancelAction::encode
        );
        register(
            AzControllerPlayAnimationSequenceAction.RESOURCE_LOCATION,
            AzControllerPlayAnimationSequenceAction::decode,
            AzControllerPlayAnimationSequenceAction::encode
        );
        register(
            AzControllerSetAnimationSpeedAction.RESOURCE_LOCATION,
            AzControllerSetAnimationSpeedAction::decode,
            AzControllerSetAnimationSpeedAction::encode
        );
        register(
            AzControllerSetEasingTypeAction.RESOURCE_LOCATION,
            AzControllerSetEasingTypeAction::decode,
            AzControllerSetEasingTypeAction::encode
        );
        register(
            AzControllerSetTransitionSpeedAction.RESOURCE_LOCATION,
            AzControllerSetTransitionSpeedAction::decode,
            AzControllerSetTransitionSpeedAction::encode
        );
        register(
            AzControllerSetStartTickOffsetAction.RESOURCE_LOCATION,
            AzControllerSetStartTickOffsetAction::decode,
            AzControllerSetStartTickOffsetAction::encode
        );
        register(
            AzControllerSetFreezeTickAction.RESOURCE_LOCATION,
            AzControllerSetFreezeTickAction::decode,
            AzControllerSetFreezeTickAction::encode
        );
        register(
            AzControllerSetRepeatTimesAction.RESOURCE_LOCATION,
            AzControllerSetRepeatTimesAction::decode,
            AzControllerSetRepeatTimesAction::encode
        );
        register(
            AzControllerSetReverseAction.RESOURCE_LOCATION,
            AzControllerSetReverseAction::decode,
            AzControllerSetReverseAction::encode
        );
    }

    /**
     * Returns a decoder function for the given {@link ResourceLocation}.
     */
    public static @Nullable Function<FriendlyByteBuf, ? extends AzAction> getDecoderOrNull(
        ResourceLocation resourceLocation
    ) {
        var id = RESOURCE_LOCATION_TO_ID.get(resourceLocation);
        return DECODERS_BY_ID.get(id);
    }

    /**
     * Returns a decoder function for the given ID.
     */
    public static @Nullable Function<FriendlyByteBuf, AzAction> getDecoderOrNull(short id) {
        return DECODERS_BY_ID.get(id);
    }

    /**
     * Returns an encoder function for the given {@link ResourceLocation}.
     */
    public static @Nullable BiConsumer<FriendlyByteBuf, AzAction> getEncoderOrNull(ResourceLocation resourceLocation) {
        var id = RESOURCE_LOCATION_TO_ID.get(resourceLocation);
        return ENCODERS_BY_ID.get(id);
    }

    /**
     * Returns an encoder function for the given ID.
     */
    public static @Nullable BiConsumer<FriendlyByteBuf, AzAction> getEncoderOrNull(short id) {
        return ENCODERS_BY_ID.get(id);
    }

    /**
     * Returns the ID associated with a given {@link ResourceLocation}.
     */
    public static @Nullable Short getIdOrNull(ResourceLocation resourceLocation) {
        return RESOURCE_LOCATION_TO_ID.get(resourceLocation);
    }

    /**
     * Registers a new action with its resource location, decoder, and encoder.
     */
    private static <A extends AzAction> void register(
        ResourceLocation resourceLocation,
        Function<FriendlyByteBuf, A> decoder,
        BiConsumer<FriendlyByteBuf, A> encoder
    ) {
        var id = RESOURCE_LOCATION_TO_ID.computeIfAbsent(resourceLocation, ($) -> NEXT_FREE_ID++);
        DECODERS_BY_ID.put(id, (Function<FriendlyByteBuf, AzAction>) decoder);
        ENCODERS_BY_ID.put(id, (BiConsumer<FriendlyByteBuf, AzAction>) encoder);
    }
}
