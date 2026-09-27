package mod.azure.azurelib.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import mod.azure.azurelib.cache.texture.AnimatableTexture;

@Mixin(value = TextureManager.class, priority = 2010)
public abstract class TextureManagerMixin {

    @Shadow
    @Final
    private ResourceManager resourceManager;

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
        if (!azurelib$hasMcmeta(location)) {
            return original.call(location);
        }

        AnimatableTexture texture = new AnimatableTexture(location);
        TextureContents contents = loadContentsSafe(location, texture);

        if (texture.hasPendingAnimation()) {
            texture.apply(contents);
            register(location, texture);

            return texture;
        }

        texture.close();
        contents.image().close();

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

    @Unique
    private boolean azurelib$hasMcmeta(Identifier location) {
        return this.resourceManager.getResource(
            Identifier.fromNamespaceAndPath(location.getNamespace(), location.getPath() + ".mcmeta")
        ).isPresent();
    }
}
