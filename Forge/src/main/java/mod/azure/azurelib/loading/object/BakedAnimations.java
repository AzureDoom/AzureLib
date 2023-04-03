package mod.azure.azurelib.loading.object;

import java.util.Map;

import javax.annotation.Nullable;

import mod.azure.azurelib.cache.AzureLibCache;
import mod.azure.azurelib.core.animation.Animation;
import net.minecraft.util.ResourceLocation;

/**
 * Container object that holds a deserialized map of {@link Animation Animations}.<br>
 * Kept as a unique object so that it can be registered as a {@link com.google.gson.JsonDeserializer deserializer} for {@link com.google.gson.Gson Gson}
 */
public class BakedAnimations {
	
	protected final Map<String, Animation> animations;
	protected final Map<String, ResourceLocation> includes;
	
	public BakedAnimations(Map<String, Animation> animations, Map<String, ResourceLocation> includes) {
		this.animations = animations;
		this.includes = includes;
	}
	
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
	
	public Map<String, Animation> animations() {
		return this.animations;
	}
	
	public Map<String, ResourceLocation> includes() {
		return this.includes;
	}
	
}
