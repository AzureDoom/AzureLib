package mod.azure.azurelib.loading.object;

import mod.azure.azurelib.cache.AzureLibCache;
import mod.azure.azurelib.core.animation.Animation;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Container object that holds a deserialized map of {@link Animation Animations}.<br>
 * Kept as a unique object so that it can be registered as a {@link com.google.gson.JsonDeserializer deserializer} for {@link com.google.gson.Gson Gson}
 */
public record BakedAnimations(Map<String, Animation> animations, Map<String, ResourceLocation> includes) {
	/**
	 * Gets an {@link Animation} by its name, if present
	 */
	@Nullable
	public Animation getAnimation(String name){
		Animation result = animations.get(name);
		if(result == null && includes != null) {
			ResourceLocation otherFileID = includes.getOrDefault(name, null);
			if(otherFileID != null) {
				BakedAnimations otherBakedAnims = AzureLibCache.getBakedAnimations().get(otherFileID);
				if (otherBakedAnims.equals(this)) {
					//TODO: Throw exception
				} else {
					result = otherBakedAnims.getAnimationWithoutIncludes(name);
				}
			}
		}
		return result;
	}
	
	@Nullable
	private Animation getAnimationWithoutIncludes(String name) {
		return animations.get(name);
	}
	
}
