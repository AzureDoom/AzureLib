package mod.azure.azurelib.resource;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nullable;

import mod.azure.azurelib.render.texture.AnimationMetadataSection;
import mod.azure.azurelib.render.texture.TextureMetadataSection;
import mod.azure.azurelib.util.ResourceLocation;

/**
 * A single readable resource, the Beta stand-in for 1.7.10's {@code IResource}.
 */
public interface AzResource {

    ResourceLocation getResourceLocation();

    InputStream getInputStream() throws IOException;

    /** True if a {@code <resource>.mcmeta} sidecar exists. */
    boolean hasMetadata();

    /**
     * Parses a section of the {@code .mcmeta} sidecar. Supported sections are {@code "texture"}
     * ({@link TextureMetadataSection}) and {@code "animation"} ({@link AnimationMetadataSection}); anything else
     * returns the raw {@link JsonObject}.
     */
    @Nullable
    Object getMetadata(String section);
}
