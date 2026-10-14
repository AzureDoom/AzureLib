package mod.azure.azurelib.render.texture;

import java.io.IOException;

import mod.azure.azurelib.resource.AzResourceManager;

/**
 * A GL texture managed by {@link TextureManager}. Mirrors 1.7.10's interface of the same name.
 */
public interface ITextureObject {

    void loadTexture(AzResourceManager resourceManager) throws IOException;

    int getGlTextureId();
}
