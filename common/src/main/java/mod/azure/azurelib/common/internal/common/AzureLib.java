package mod.azure.azurelib.common.internal.common;

import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.UUID;
import java.util.function.Supplier;

import mod.azure.azurelib.common.internal.common.util.AzureLibUtil;
import mod.azure.azurelib.common.platform.Services;

/**
 * Base class for AzureLib!<br>
 * Hello World!<br>
 * There's not much to really see here, but feel free to stay a while and have a snack or something.
 *
 * @see AzureLibUtil
 */
public final class AzureLib {

    public static final String MOD_ID = "azurelib";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final Marker MAIN_MARKER = MarkerManager.getMarker("main");

    /**
     * @deprecated
     */
    @Deprecated(forRemoval = true)
    public static final Supplier<DataComponentType<Long>> STACK_ANIMATABLE_ID_COMPONENT = Services.PLATFORM
        .registerDataComponent(
            "stack_animatable_id",
            builder -> builder.persistent(Codec.LONG)
                .networkSynchronized(
                    ByteBufCodecs.VAR_LONG
                )
        );

    public static final Supplier<DataComponentType<UUID>> AZ_ID = Services.PLATFORM
        .registerDataComponent(
            "az_id",
            builder -> builder.persistent(UUIDUtil.CODEC)
                .networkSynchronized(UUIDUtil.STREAM_CODEC)
        );

    public static boolean hasInitialized;

    public static boolean hasKeyBindsInitialized;

    private AzureLib() {
        throw new UnsupportedOperationException();
    }

    public static void initialize() {
        if (!hasInitialized) {
            Services.INITIALIZER.initialize();
        }
        hasInitialized = true;
    }

    public static ResourceLocation modResource(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }
}
