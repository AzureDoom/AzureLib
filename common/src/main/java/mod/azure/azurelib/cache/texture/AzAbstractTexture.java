package mod.azure.azurelib.cache.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Base type for AzureLib's generated textures.
 * <p>
 * 26.2 replaced the old {@code AbstractTexture#load}/{@code RenderCall}-based upload path with
 * {@link ReloadableTexture#loadContents}, which returns the decoded image and lets the texture manager perform the GPU
 * upload on the render thread — see {@link AnimatableTexture} and {@link AutoGlowingTexture} for the subclasses that
 * implement it.
 */
public abstract class AzAbstractTexture extends ReloadableTexture {

    protected static final String APPENDIX = "_glowmask";

    private static final Set<Identifier> GENERATED = ConcurrentHashMap.newKeySet();

    public AzAbstractTexture(Identifier location) {
        super(location);
    }

    public static void onRenderThread(Runnable renderCall) {
        if (RenderSystem.isOnRenderThread()) {
            renderCall.run();
        } else {
            RenderSystem.queueFencedTask(renderCall);
        }
    }

    protected static void generateTexture(
        Identifier texturePath,
        Consumer<TextureManager> textureManagerConsumer
    ) {
        if (!RenderSystem.isOnRenderThread())
            throw new IllegalThreadStateException(
                "Texture loading called outside of the render thread! This should DEFINITELY not be happening."
            );

        if (!GENERATED.add(texturePath))
            return;

        textureManagerConsumer.accept(Minecraft.getInstance().getTextureManager());
    }

    /** Upload a NativeImage into an existing GPU texture. */
    public static void uploadSimple(GpuTexture texture, NativeImage image) {
        if (texture == null || texture.isClosed())
            return;

        RenderSystem.getDevice().createCommandEncoder().writeToTexture(texture, image);
    }

    public static Identifier appendToPath(Identifier location, String suffix) {
        String path = location.getPath();
        int i = path.lastIndexOf('.');

        return Identifier.fromNamespaceAndPath(
            location.getNamespace(),
            path.substring(0, i) + suffix + path.substring(i)
        );
    }

    public static Identifier getEmissiveResource(Identifier baseResource) {
        Identifier path = appendToPath(baseResource, APPENDIX);

        generateTexture(
            path,
            textureManager -> textureManager.registerAndLoad(path, new AutoGlowingTexture(baseResource, path))
        );

        return path;
    }

    public static RenderType getRenderType(Identifier texture) {
        return RenderTypes.entityTranslucentEmissive(getEmissiveResource(texture), false);
    }

    public static RenderType getOutlineRenderType(Identifier texture) {
        return RenderTypes.entityTranslucentEmissive(getEmissiveResource(texture), true);
    }
}
