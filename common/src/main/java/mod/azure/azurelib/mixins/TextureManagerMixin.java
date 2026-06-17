package mod.azure.azurelib.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.cache.texture.AnimatableTexture;

@Mixin(value = TextureManager.class, priority = 2010)
public abstract class TextureManagerMixin {

    @Unique
    private final Map<Identifier, Boolean> azurelib$animationCache = new HashMap<>();

    @Unique
    private final Map<Identifier, AnimatableTexture> azurelib$textureCache = new HashMap<>();

    @Shadow
    public abstract void register(Identifier resourceLocation, AbstractTexture abstractTexture);

    @Shadow
    protected abstract AbstractTexture loadTexture(Identifier path, AbstractTexture texture);

    @Inject(
        method = "getTexture(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/texture/AbstractTexture;",
        at = @At("RETURN"),
        cancellable = true,
        require = 0
    )
    private void azurelib$replaceAnimatableTexture(
            Identifier location,
        CallbackInfoReturnable<AbstractTexture> cir
    ) {
        var currentTexture = cir.getReturnValue();

        if (currentTexture == null || currentTexture.getClass() != SimpleTexture.class) {
            return;
        }

        if (azurelib$textureCache.containsKey(location)) {
            cir.setReturnValue(azurelib$textureCache.get(location));
            return;
        }

        var cached = azurelib$animationCache.get(location);
        if (cached != null && !cached) {
            return;
        }

        if (!azurelib$hasAnimationMetadata(location)) {
            azurelib$animationCache.put(location, false);
            return;
        }

        var animatableTexture = new AnimatableTexture(location);

        try {
            loadTexture(location, animatableTexture);
        } catch (Exception e) {
            AzureLib.LOGGER.error("Failed to load texture {}", location);
            azurelib$animationCache.put(location, false);
            return;
        }

        if (!animatableTexture.isAnimated()) {
            azurelib$animationCache.put(location, false);
            return;
        }

        azurelib$animationCache.put(location, true);
        azurelib$textureCache.put(location, animatableTexture);

        this.register(location, animatableTexture);
        cir.setReturnValue(animatableTexture);
    }

    @Unique
    private boolean azurelib$hasAnimationMetadata(Identifier texture) {
        var mcmeta = Identifier.fromNamespaceAndPath(
            texture.getNamespace(),
            texture.getPath() + ".mcmeta"
        );

        try {
            return Minecraft.getInstance()
                .getResourceManager()
                .getResource(mcmeta)
                .isPresent();
        } catch (Exception e) {
            return false;
        }
    }
}
