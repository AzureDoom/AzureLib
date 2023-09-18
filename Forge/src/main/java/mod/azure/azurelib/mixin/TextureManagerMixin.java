package mod.azure.azurelib.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mod.azure.azurelib.cache.texture.AnimatableTexture;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

@Mixin(TextureManager.class)
public abstract class TextureManagerMixin {
	@Shadow @Final private Map<ResourceLocation, Texture> byPath;

	@Shadow public abstract void register(ResourceLocation resourceLocation, Texture abstractTexture);
	
	@Inject(method = "getTexture(Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/client/renderer/texture/Texture;", at = @At("HEAD"))
	private void wrapAnimatableTexture(ResourceLocation path, CallbackInfoReturnable<Texture> callback) {
		Texture existing = this.byPath.get(path);

		if (existing == null) {
			existing = new AnimatableTexture(path);

			register(path, existing);
		}
	}
}