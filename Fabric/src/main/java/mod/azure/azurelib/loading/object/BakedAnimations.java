package mod.azure.azurelib.loading.object;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.core.animation.Animation;

/**
 * Container object that holds a deserialized map of {@link Animation Animations}.<br>
 * Kept as a unique object so that it can be registered as a {@link com.google.gson.JsonDeserializer deserializer} for {@link com.google.gson.Gson Gson}
 */
public record BakedAnimations(Map<String, Animation> animations) {
	/**
	 * Gets an {@link Animation} by its name, if present
	 */
	@Nullable
	public Animation getAnimation(String name){
		return animations.get(name);
	}
}
