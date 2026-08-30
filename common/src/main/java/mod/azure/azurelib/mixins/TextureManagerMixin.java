package mod.azure.azurelib.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import mod.azure.azurelib.cache.texture.AnimatableTexture;

@Mixin(value = TextureManager.class, priority = 2010)
public abstract class TextureManagerMixin {

    @Shadow
    protected abstract TextureContents loadContentsSafe(
        Identifier textureId,
        ReloadableTexture texture
    );

    @Shadow
    public abstract void register(
        Identifier location,
        AbstractTexture texture
    );

    @WrapOperation(
        method = "getTexture(Lnet/minecraft/resources/Identifier;)" +
            "Lnet/minecraft/client/renderer/texture/AbstractTexture;",
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/resources/Identifier;)" +
                "Lnet/minecraft/client/renderer/texture/SimpleTexture;"
        ),
        require = 0
    )
    private SimpleTexture azurelib$replaceAnimatableTexture(
        Identifier location,
        Operation<SimpleTexture> original
    ) {
        AnimatableTexture texture =
            new AnimatableTexture(location);

        TextureContents contents =
            loadContentsSafe(location, texture);

        if (texture.isAnimated()) {
            texture.apply(contents);
            register(location, texture);

            return texture;
        }

        texture.close();

        return original.call(location);
    }

    @WrapWithCondition(
        method = "getTexture(Lnet/minecraft/resources/Identifier;)" +
            "Lnet/minecraft/client/renderer/texture/AbstractTexture;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/TextureManager;" +
                "registerAndLoad(" +
                "Lnet/minecraft/resources/Identifier;" +
                "Lnet/minecraft/client/renderer/texture/ReloadableTexture;)V"
        ),
        require = 0
    )
    private boolean azurelib$skipAnimatedRegistration(
        TextureManager textureManager,
        Identifier textureId,
        ReloadableTexture texture
    ) {
        return !(texture instanceof AnimatableTexture);
    }
}
