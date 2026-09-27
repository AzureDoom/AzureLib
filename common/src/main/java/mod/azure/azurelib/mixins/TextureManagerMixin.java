/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.cache.texture.AnimatableTexture;

@Mixin(value = TextureManager.class, priority = 2010)
public abstract class TextureManagerMixin {

    @Unique
    private final Map<ResourceLocation, Boolean> azurelib$animationCache = new HashMap<>();

    @Shadow
    public abstract void register(ResourceLocation resourceLocation, AbstractTexture abstractTexture);

    @Shadow
    protected abstract AbstractTexture loadTexture(ResourceLocation path, AbstractTexture texture);

    @Inject(
        method = "getTexture(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/texture/AbstractTexture;",
        at = @At("RETURN"),
        cancellable = true,
        require = 0
    )
    private void azurelib$replaceAnimatableTexture(
        ResourceLocation location,
        CallbackInfoReturnable<AbstractTexture> cir
    ) {
        AbstractTexture currentTexture = cir.getReturnValue();

        if (currentTexture == null || currentTexture.getClass() != SimpleTexture.class) {
            return;
        }

        Boolean cached = azurelib$animationCache.get(location);
        if (cached != null && !cached) {
            return;
        }

        if (!azurelib$hasAnimationMetadata(location)) {
            azurelib$animationCache.put(location, false);
            return;
        }

        AnimatableTexture animatableTexture = new AnimatableTexture(location);

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

        this.register(location, animatableTexture);
        cir.setReturnValue(animatableTexture);
    }

    @Inject(method = "reload", at = @At("HEAD"))
    private void azurelib$clearAnimationCache(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        RenderSystem.recordRenderCall(azurelib$animationCache::clear);
    }

    @Unique
    private boolean azurelib$hasAnimationMetadata(ResourceLocation texture) {
        ResourceLocation mcmeta = new ResourceLocation(
            texture.getNamespace(),
            texture.getPath() + ".mcmeta"
        );

        try {
            Minecraft.getInstance()
                .getResourceManager()
                .getResource(mcmeta);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
