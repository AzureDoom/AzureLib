package mod.azure.azurelib.render.texture;

/**
 * The {@code texture} section of a {@code .mcmeta} file. Mirrors 1.7.10's class of the same name.
 */
public final class TextureMetadataSection {

    private final boolean blur;

    private final boolean clamp;

    public TextureMetadataSection(boolean blur, boolean clamp) {
        this.blur = blur;
        this.clamp = clamp;
    }

    public boolean getTextureBlur() {
        return this.blur;
    }

    public boolean getTextureClamp() {
        return this.clamp;
    }
}
