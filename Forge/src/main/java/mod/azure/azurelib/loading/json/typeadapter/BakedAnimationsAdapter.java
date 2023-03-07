package mod.azure.azurelib.loading.json.typeadapter;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;

import com.eliotlash.mclib.math.Constant;
import com.eliotlash.mclib.math.IValue;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.core.animation.Animation;
import mod.azure.azurelib.core.animation.EasingType;
import mod.azure.azurelib.core.keyframe.BoneAnimation;
import mod.azure.azurelib.core.keyframe.Keyframe;
import mod.azure.azurelib.core.keyframe.KeyframeStack;
import mod.azure.azurelib.core.molang.MolangException;
import mod.azure.azurelib.core.molang.MolangParser;
import mod.azure.azurelib.core.molang.expressions.MolangValue;
import mod.azure.azurelib.loading.object.BakedAnimations;
import mod.azure.azurelib.util.JsonUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

/**
 * {@link com.google.gson.Gson} {@link JsonDeserializer} for {@link mod.azure.azurelib.loading.object.BakedAnimations}.<br>
 * Acts as the deserialization interface for {@code BakedAnimations}
 */
public class BakedAnimationsAdapter implements JsonDeserializer<BakedAnimations> {
	@Override
	public BakedAnimations deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject jsonObj = json.getAsJsonObject();
		
		JsonObject animationJsonList = jsonObj.getAsJsonObject("animations");
		JsonArray includeListJSONObj = jsonObj.getAsJsonArray("includes");
		Map<String, ResourceLocation> includes = new Object2ObjectOpenHashMap<>(includeListJSONObj.size());
		for(JsonElement entry : includeListJSONObj.asList()) {
			JsonObject obj = entry.getAsJsonObject();
			ResourceLocation fileId = new ResourceLocation(obj.get("file_id").getAsString());
			for(JsonElement animName : obj.getAsJsonArray("animations")) {
				String ani = animName.getAsString();
				if(includes.containsKey(ani)) {
					AzureLib.LOGGER.warn("Animation {} is already included! File already including: {}  File trying to include from again: {}", ani, includes.get(ani).toString(), fileId.toString());
				} else {
					includes.put(ani, fileId);
				}
			}
		}
		
		Map<String, Animation> animations = new Object2ObjectOpenHashMap<>(animationJsonList.size());

		for (Map.Entry<String, JsonElement> entry : animationJsonList.entrySet()) {
			try {
				animations.put(entry.getKey(), bakeAnimation(entry.getKey(), entry.getValue().getAsJsonObject(), context));
			}
			catch (MolangException ex) {
				AzureLib.LOGGER.error("Unable to parse animation: " + entry.getKey());
				ex.printStackTrace();
			}
		}

		return new BakedAnimations(animations, includes);
	}

	private Animation bakeAnimation(String name, JsonObject animationObj, JsonDeserializationContext context) throws MolangException {
		double length = animationObj.has("animation_length") ? GsonHelper.getAsDouble(animationObj, "animation_length") * 20d : -1;
		Animation.LoopType loopType = Animation.LoopType.fromJson(animationObj.get("loop"));
		BoneAnimation[] boneAnimations = bakeBoneAnimations(GsonHelper.getAsJsonObject(animationObj, "bones", new JsonObject()));
		Animation.Keyframes keyframes = context.deserialize(animationObj, Animation.Keyframes.class);

		if (length == -1)
			length = calculateAnimationLength(boneAnimations);

		return new Animation(name, length, loopType, boneAnimations, keyframes);
	}

	private BoneAnimation[] bakeBoneAnimations(JsonObject bonesObj) throws MolangException {
		BoneAnimation[] animations = new BoneAnimation[bonesObj.size()];
		int index = 0;

		for (Map.Entry<String, JsonElement> entry : bonesObj.entrySet()) {
			JsonObject entryObj = entry.getValue().getAsJsonObject();
			KeyframeStack<Keyframe<IValue>> scaleFrames = buildKeyframeStack(
					getTripletObj(entryObj.get("scale")), false);
			KeyframeStack<Keyframe<IValue>> positionFrames = buildKeyframeStack(
					getTripletObj(entryObj.get("position")), false);
			KeyframeStack<Keyframe<IValue>> rotationFrames = buildKeyframeStack(
					getTripletObj(entryObj.get("rotation")), true);

			animations[index] = new BoneAnimation(entry.getKey(), rotationFrames, positionFrames, scaleFrames);
			index++;
		}

		return animations;
	}

	private static List<Pair<String, JsonElement>> getTripletObj(JsonElement element) {
		if (element == null)
			return List.of();

		if (element instanceof JsonPrimitive primitive) {
			JsonArray array = new JsonArray(3);

			array.add(primitive);
			array.add(primitive);
			array.add(primitive);

			element = array;
		}

		if (element instanceof JsonArray array)
			return ObjectArrayList.of(Pair.of("0", array));

		if (element instanceof JsonObject obj) {
			List<Pair<String, JsonElement>> list = new ObjectArrayList<>();

			for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
				if (entry.getValue() instanceof JsonObject entryObj && !entryObj.has("vector")) {
					String timestamp = entry.getKey();
					double time = NumberUtils.isCreatable(timestamp) ? Double.parseDouble(timestamp) : 0;

					if (entryObj.has("pre"))
						list.add(Pair.of(timestamp, GsonHelper.getAsJsonArray(entryObj, "pre")));

					if (entryObj.has("post"))
						list.add(Pair.of(String.valueOf(time + 0.0000001), GsonHelper.getAsJsonArray(entryObj, "post")));

					continue;
				}

				list.add(Pair.of(entry.getKey(), entry.getValue()));
			}

			return list;
		}

		throw new JsonParseException("Invalid object type provided to getTripletObj, got: " + element);
	}

	private KeyframeStack<Keyframe<IValue>> buildKeyframeStack(List<Pair<String, JsonElement>> entries, boolean isForRotation) throws MolangException {
		if (entries.isEmpty())
			return new KeyframeStack<>();

		List<Keyframe<IValue>> xFrames = new ObjectArrayList<>();
		List<Keyframe<IValue>> yFrames = new ObjectArrayList<>();
		List<Keyframe<IValue>> zFrames = new ObjectArrayList<>();

		IValue xPrev = null;
		IValue yPrev = null;
		IValue zPrev = null;
		Pair<String, JsonElement> prevEntry = null;

		for (Pair<String, JsonElement> entry : entries) {
			String key = entry.getFirst();
			JsonElement element = entry.getSecond();

			if (key.equals("easing") || key.equals("easingArgs") || key.equals("lerp_mode"))
				continue;

			double prevTime = prevEntry != null ? Double.parseDouble(prevEntry.getFirst()) : 0;
			double curTime = NumberUtils.isCreatable(key) ? Double.parseDouble(entry.getFirst()) : 0;
			double timeDelta = curTime - prevTime;

			JsonArray keyFrameVector = element instanceof JsonArray array ? array : GsonHelper.getAsJsonArray(element.getAsJsonObject(), "vector");
			MolangValue rawXValue = MolangParser.parseJson(keyFrameVector.get(0));
			MolangValue rawYValue = MolangParser.parseJson(keyFrameVector.get(1));
			MolangValue rawZValue = MolangParser.parseJson(keyFrameVector.get(2));
			IValue xValue = isForRotation && rawXValue.isConstant() ? new Constant(Math.toRadians(-rawXValue.get())) : rawXValue;
			IValue yValue = isForRotation && rawYValue.isConstant() ? new Constant(Math.toRadians(-rawYValue.get())) : rawYValue;
			IValue zValue = isForRotation && rawZValue.isConstant() ? new Constant(Math.toRadians(rawZValue.get())) : rawZValue;

			JsonObject entryObj = element instanceof JsonObject obj ? obj : null;
			EasingType easingType = entryObj != null && entryObj.has("easing") ? EasingType.fromJson(entryObj.get("easing")) : EasingType.LINEAR;
			List<IValue> easingArgs = entryObj != null && entryObj.has("easingArgs") ?
					JsonUtil.jsonArrayToList(GsonHelper.getAsJsonArray(entryObj, "easingArgs"), ele -> new Constant(ele.getAsDouble())) :
					new ObjectArrayList<>();

			xFrames.add(new Keyframe<>(timeDelta * 20, prevEntry == null ? xValue : xPrev, xValue, easingType, easingArgs));
			yFrames.add(new Keyframe<>(timeDelta * 20, prevEntry == null ? yValue : yPrev, yValue, easingType, easingArgs));
			zFrames.add(new Keyframe<>(timeDelta * 20, prevEntry == null ? zValue : zPrev, zValue, easingType, easingArgs));

			xPrev = xValue;
			yPrev = yValue;
			zPrev = zValue;
			prevEntry = entry;
		}

		return new KeyframeStack<>(xFrames, yFrames, zFrames);
	}

	private static double calculateAnimationLength(BoneAnimation[] boneAnimations) {
		double length = 0;

		for (BoneAnimation animation : boneAnimations) {
			length = Math.max(length, animation.rotationKeyFrames().getLastKeyframeTime());
			length = Math.max(length, animation.positionKeyFrames().getLastKeyframeTime());
			length = Math.max(length, animation.scaleKeyFrames().getLastKeyframeTime());
		}

		return length == 0 ? Double.MAX_VALUE : length;
	}
}
