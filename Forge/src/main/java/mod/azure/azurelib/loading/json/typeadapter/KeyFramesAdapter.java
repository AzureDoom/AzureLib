/**
 * This class is a fork of the matching class found in the Geckolib repository.
 * Original source: https://github.com/bernie-g/geckolib
 * Copyright © 2024 Bernie-G.
 * Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.loading.json.typeadapter;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mod.azure.azurelib.core.animation.Animation;
import mod.azure.azurelib.core.keyframe.event.data.CustomInstructionKeyframeData;
import mod.azure.azurelib.core.keyframe.event.data.ParticleKeyframeData;
import mod.azure.azurelib.core.keyframe.event.data.SoundKeyframeData;
import mod.azure.azurelib.util.JsonUtil;
import net.minecraft.util.JSONUtils;

/**
 * {@link Gson} {@link JsonDeserializer} for {@link Animation.Keyframes}.<br>
 * Acts as the deserialization interface for {@code Keyframes}
 */
@Deprecated()
public class KeyFramesAdapter implements JsonDeserializer<Animation.Keyframes> {
	@Override
	public Animation.Keyframes deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject obj = json.getAsJsonObject();
		SoundKeyframeData[] sounds = buildSoundFrameData(obj);
		ParticleKeyframeData[] particles = buildParticleFrameData(obj);
		CustomInstructionKeyframeData[] customInstructions = buildCustomFrameData(obj);

		return new Animation.Keyframes(sounds, particles, customInstructions);
	}

	private static SoundKeyframeData[] buildSoundFrameData(JsonObject rootObj) {
		JsonObject soundsObj = JSONUtils.getJsonObject(rootObj, "sound_effects", new JsonObject());
		SoundKeyframeData[] sounds = new SoundKeyframeData[soundsObj.size()];
		int index = 0;

		for (Map.Entry<String, JsonElement> entry : soundsObj.entrySet()) {
			sounds[index] = new SoundKeyframeData(Double.parseDouble(entry.getKey()) * 20d, JSONUtils.getString(entry.getValue().getAsJsonObject(), "effect"));
			index++;
		}

		return sounds;
	}

	private static ParticleKeyframeData[] buildParticleFrameData(JsonObject rootObj) {
		JsonObject particlesObj = JSONUtils.getJsonObject(rootObj, "particle_effects", new JsonObject());
		ParticleKeyframeData[] particles = new ParticleKeyframeData[particlesObj.size()];
		int index = 0;

		for (Map.Entry<String, JsonElement> entry : particlesObj.entrySet()) {
			JsonObject obj = entry.getValue().getAsJsonObject();
			String effect = JSONUtils.getString(obj, "effect", "");
			String locator = JSONUtils.getString(obj, "locator", "");
			String script = JSONUtils.getString(obj, "pre_effect_script", "");

			particles[index] = new ParticleKeyframeData(Double.parseDouble(entry.getKey()) * 20d, effect, locator, script);
			index++;
		}

		return particles;
	}

	private static CustomInstructionKeyframeData[] buildCustomFrameData(JsonObject rootObj) {
		JsonObject customInstructionsObj = JSONUtils.getJsonObject(rootObj, "timeline", new JsonObject());
		CustomInstructionKeyframeData[] customInstructions = new CustomInstructionKeyframeData[customInstructionsObj.size()];
		int index = 0;

		for (Map.Entry<String, JsonElement> entry : customInstructionsObj.entrySet()) {
			String instructions = "";

			if (entry.getValue() instanceof JsonArray) {
				instructions = JsonUtil.GEO_GSON.fromJson(((JsonArray)entry.getValue()), ObjectArrayList.class).toString();
			}
			else if (entry.getValue() instanceof JsonPrimitive) {
				instructions = ((JsonPrimitive)entry.getValue()).getAsString();
			}

			customInstructions[index] = new CustomInstructionKeyframeData(Double.parseDouble(entry.getKey()) * 20d, instructions);
			index++;
		}

		return customInstructions;
	}
}
