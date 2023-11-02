package mod.azure.azurelib.common.internal.mixins;

import java.util.Map;

import mod.azure.azurelib.common.internal.common.cache.texture.AnimatableTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

@Mixin(TextureManager.class)
public abstract class TextureManagerMixin {
	@Shadow @Final private Map<ResourceLocation, AbstractTexture> byPath;

	@Shadow public abstract void register(ResourceLocation resourceLocation, AbstractTexture abstractTexture);

	@Inject(method = "getTexture(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/texture/AbstractTexture;", at = @At("HEAD"))
	private void wrapAnimatableTexture(ResourceLocation path, CallbackInfoReturnable<AbstractTexture> callback) {
		AbstractTexture existing = this.byPath.get(path);

		if (existing == null) {
			existing = new AnimatableTexture(path);

			register(path, existing);
		}
	}
}