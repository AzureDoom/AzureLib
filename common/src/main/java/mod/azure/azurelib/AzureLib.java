package mod.azure.azurelib;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;
import java.util.function.Supplier;

import mod.azure.azurelib.platform.Services;
import mod.azure.azurelib.util.AzureLibUtil;

/**
 * Base class for AzureLib!<br>
 * Hello World!<br>
 * There's little to really see here, but feel free to stay a while and have a snack or something.
 *
 * @see AzureLibUtil
 */
public final class AzureLib {

    public static final String MOD_ID = "azurelib";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final Supplier<DataComponentType<UUID>> AZ_ID = Services.PLATFORM
        .registerDataComponent(
            "az_id",
            builder -> builder.persistent(UUIDUtil.CODEC)
                .networkSynchronized(UUIDUtil.STREAM_CODEC)
        );

    private AzureLib() {
        throw new UnsupportedOperationException();
    }

    public static void initialize() {}

    public static Identifier modResource(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }
}
