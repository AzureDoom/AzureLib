package mod.azure.azurelib.animation.parse;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.math.NumberUtils;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.controller.keyframe.AzBoneAnimation;
import mod.azure.azurelib.animation.controller.keyframe.AzKeyframe;
import mod.azure.azurelib.animation.controller.keyframe.AzKeyframeStack;
import mod.azure.azurelib.animation.easing.AzEasingType;
import mod.azure.azurelib.animation.easing.AzEasingTypeLoader;
import mod.azure.azurelib.animation.easing.AzEasingTypes;
import mod.azure.azurelib.animation.primitive.AzBakedAnimation;
import mod.azure.azurelib.animation.primitive.AzBakedAnimations;
import mod.azure.azurelib.animation.primitive.AzKeyframes;
import mod.azure.azurelib.animation.primitive.AzLoopType;
import mod.azure.azurelib.core.math.Constant;
import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.molang.MolangException;
import mod.azure.azurelib.core.molang.MolangParser;
import mod.azure.azurelib.core.molang.expressions.MolangValue;
import mod.azure.azurelib.util.JsonUtil;

/**
 * {@link Gson} {@link JsonDeserializer} for {@link AzBakedAnimations}.<br>
 * Acts as the deserialization interface for {@code BakedAnimations}
 */
public class AzBakedAnimationsAdapter implements JsonDeserializer<AzBakedAnimations> {

    /**
     * Processes a given JSON element and transforms it into a list of pairs, where each pair consists of a string key
     * and a corresponding JSON element. Depending on the type of the input element, it handles primitive values,
     * arrays, and objects differently, ensuring a uniform output structure. For JSON primitives, a synthetic triplet
     * array is generated. For JSON arrays, the array is paired with the key "0". For JSON objects, individual entries
     * are processed recursively, with special handling for nested objects without a "vector" key.
     *
     * @param element The JSON element to be processed. It can be a {@link JsonPrimitive}, {@link JsonObject}, or
     *                {@link JsonArray}. If null, an empty list is returned.
     * @return A list of {@link Pair} objects where each pair contains a string key and a corresponding
     *         {@link JsonElement}. This list represents the processed structure of the input JSON element.
     * @throws JsonParseException If the provided JSON element is of an unsupported type or is invalid.
     */
    private static List<Pair<String, JsonElement>> getKeyframes(JsonElement element) {
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
            if (obj.has("vector"))
                return ObjectArrayList.of(Pair.of("0", obj));

            List<Pair<String, JsonElement>> list = new ObjectArrayList<>();

            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                double timestamp = readTimestamp(entry.getKey());

                if (timestamp == 0 && !list.isEmpty())
                    throw new JsonParseException(
                        "Invalid keyframe data - multiple starting keyframes?" + entry.getKey()
                    );

                if (entry.getValue() instanceof JsonObject entryObj && !entryObj.has("vector")) {
                    addBedrockKeyframes(timestamp, entryObj, list);

                    continue;
                }

                list.add(Pair.of(String.valueOf(timestamp), entry.getValue()));
            }

            return list;
        }

        throw new JsonParseException("Invalid object type provided to getTripletObj, got: " + element);
    }

    private static void addBedrockKeyframes(
        double timestamp,
        JsonObject keyframe,
        List<Pair<String, JsonElement>> keyframes
    ) {
        boolean addedFrame = false;

        if (keyframe.has("pre")) {
            JsonElement pre = keyframe.get("pre");
            addedFrame = true;

            keyframes.add(
                Pair.of(
                    String.valueOf(timestamp == 0 ? timestamp : timestamp - 0.001d),
                    pre.isJsonArray()
                        ? pre.getAsJsonArray()
                        : GsonHelper.getAsJsonArray(pre.getAsJsonObject(), "vector")
                )
            );
        }

        if (keyframe.has("post")) {
            JsonElement post = keyframe.get("post");
            JsonArray values = post.isJsonArray()
                ? post.getAsJsonArray()
                : GsonHelper.getAsJsonArray(post.getAsJsonObject(), "vector");

            if (keyframe.has("lerp_mode")) {
                var keyframeObj = new JsonObject();

                keyframeObj.add("vector", values);
                keyframeObj.add("easing", keyframe.get("lerp_mode"));

                keyframes.add(Pair.of(String.valueOf(timestamp), keyframeObj));
            } else {
                keyframes.add(Pair.of(String.valueOf(timestamp), values));
            }

            return;
        }

        if (!addedFrame)
            throw new JsonParseException("Invalid keyframe data - expected array, found " + keyframe);
    }

    /**
     * Calculates the overall length of the animation timeline across all provided bone animations. The calculation
     * considers the maximum keyframe time for rotation, position, and scale transformations for each bone and
     * determines the longest timeline among them.
     *
     * @param boneAnimations An array of {@link AzBoneAnimation} instances representing the animations for individual
     *                       bones. Each bone animation includes keyframe stacks for rotation, position, and scale
     *                       transformations.
     * @return The maximum length of the animation timeline. If no keyframes are present, it defaults to
     *         {@link Double#MAX_VALUE}.
     */
    private static double calculateAnimationLength(AzBoneAnimation[] boneAnimations) {
        double length = 0;

        for (var animation : boneAnimations) {
            length = Math.max(length, animation.rotationKeyframes().getLastKeyframeTime());
            length = Math.max(length, animation.positionKeyframes().getLastKeyframeTime());
            length = Math.max(length, animation.scaleKeyframes().getLastKeyframeTime());
        }

        return length == 0 ? Double.MAX_VALUE : length;
    }

    /**
     * Deserializes JSON data into an instance of {@link AzBakedAnimations}.
     *
     * @param json    The JSON element to deserialize, expected to contain a valid structure for animations and optional
     *                includes.
     * @param type    The type of object to deserialize to; this is typically {@link AzBakedAnimations}.
     * @param context A context for handling nested deserialization, such as for custom types embedded within the JSON
     *                structure.
     * @return A newly created {@link AzBakedAnimations} instance containing parsed animations and includes as specified
     *         in the provided JSON data.
     * @throws JsonParseException If the JSON structure is invalid or an error occurs during deserialization.
     */
    @Override
    public AzBakedAnimations deserialize(
        JsonElement json,
        Type type,
        JsonDeserializationContext context
    ) throws JsonParseException {
        JsonObject jsonObj = json.getAsJsonObject();

        JsonObject animationJsonList = jsonObj.getAsJsonObject("animations");
        JsonArray includeListJSONObj = jsonObj.getAsJsonArray("includes");
        Map<String, ResourceLocation> includes = readIncludes(includeListJSONObj);

        Map<String, AzBakedAnimation> animations = new Object2ObjectOpenHashMap<>(animationJsonList.size());

        for (Map.Entry<String, JsonElement> entry : animationJsonList.entrySet()) {
            try {
                animations.put(
                    entry.getKey(),
                    bakeAnimation(entry.getKey(), entry.getValue().getAsJsonObject(), context)
                );
            } catch (MolangException ex) {
                AzureLib.LOGGER.error("Unable to parse animation '{}'", entry.getKey(), ex);
            }
        }

        return new AzBakedAnimations(animations, includes);
    }

    /**
     * Reads and processes a JSON array of include entries, mapping animation names to their corresponding file
     * identifiers.
     * <p>
     * Each entry in the provided JSON array is expected to be a JSON object containing a "file_id" and an "animations"
     * array. Valid animation names from the "animations" array are mapped to the associated "file_id". Invalid or
     * malformed entries are logged and skipped during processing.
     *
     * @param includeListJSONObj a JSON array containing the include entries to be processed. Each entry must be a JSON
     *                           object with a "file_id" field (string) and an "animations" field (array of strings).
     * @return a map associating animation names (as strings) with their respective file identifiers (as
     *         {@code ResourceLocation}), or {@code null} if the input array is null, empty, or if no valid mappings are
     *         found.
     */
    private static Map<String, ResourceLocation> readIncludes(JsonArray includeListJSONObj) {
        if (includeListJSONObj == null || includeListJSONObj.isEmpty())
            return null;

        Map<String, ResourceLocation> includes = new Object2ObjectOpenHashMap<>(includeListJSONObj.size());

        for (JsonElement entry : includeListJSONObj) {
            if (!entry.isJsonObject()) {
                AzureLib.LOGGER.warn("Skipping malformed include entry: {}", entry);
                continue;
            }

            JsonObject obj = entry.getAsJsonObject();

            if (!obj.has("file_id")) {
                AzureLib.LOGGER.warn("Include entry is missing 'file_id': {}", obj);
                continue;
            }

            if (!obj.has("animations") || !obj.get("animations").isJsonArray()) {
                AzureLib.LOGGER.warn(
                    "Include entry for file '{}' is missing a valid 'animations' array: {}",
                    obj.get("file_id").getAsString(),
                    obj
                );
                continue;
            }

            ResourceLocation fileId;
            try {
                fileId = new ResourceLocation(obj.get("file_id").getAsString());
            } catch (Exception ex) {
                AzureLib.LOGGER.warn(
                    "Invalid include file_id '{}': {}",
                    obj.get("file_id").getAsString(),
                    ex.getMessage()
                );
                continue;
            }

            for (JsonElement animName : obj.getAsJsonArray("animations")) {
                if (!animName.isJsonPrimitive() || !animName.getAsJsonPrimitive().isString()) {
                    AzureLib.LOGGER.warn(
                        "Skipping non-string animation name in include file {}: {}",
                        fileId,
                        animName
                    );
                    continue;
                }

                String ani = animName.getAsString();

                if (ani.isBlank()) {
                    AzureLib.LOGGER.warn("Skipping blank animation name in include file {}", fileId);
                    continue;
                }

                ResourceLocation previous = includes.putIfAbsent(ani, fileId);
                if (previous != null) {
                    AzureLib.LOGGER.warn(
                        "Animation '{}' is already included. First source: {}, duplicate source: {}",
                        ani,
                        previous,
                        fileId
                    );
                }
            }
        }

        return includes.isEmpty() ? null : includes;
    }

    /**
     * Processes the provided JSON data to create an instance of {@link AzBakedAnimation}. This method interprets the
     * animation JSON object, constructs the necessary data structures such as bone animations and keyframes, and
     * applies logic to calculate the animation length if not explicitly defined.
     *
     * @param name         The name of the animation being created.
     * @param animationObj The JSON object containing the animation definition. This object may include details such as
     *                     animation length, loop type, bones, and keyframe data.
     * @param context      The deserialization context used for nested data structures such as {@link AzKeyframes}.
     * @return A constructed {@link AzBakedAnimation} instance containing the parsed animation details.
     * @throws MolangException If an error occurs while processing expressions or any other aspect of the Molang
     *                         language during animation creation.
     */
    private AzBakedAnimation bakeAnimation(
        String name,
        JsonObject animationObj,
        JsonDeserializationContext context
    ) throws MolangException {
        double length = animationObj.has("animation_length")
            ? GsonHelper.getAsDouble(animationObj, "animation_length") * 20d
            : -1;
        AzLoopType loopType = AzLoopType.fromJson(animationObj.get("loop"));
        AzBoneAnimation[] boneAnimations = bakeBoneAnimations(
            GsonHelper.getAsJsonObject(animationObj, "bones", new JsonObject())
        );
        AzKeyframes keyframes = context.deserialize(animationObj, AzKeyframes.class);

        if (length == -1)
            length = calculateAnimationLength(boneAnimations);

        return new AzBakedAnimation(name, length, loopType, boneAnimations, keyframes);
    }

    /**
     * Processes a JSON object representing bone animations and constructs an array of {@link AzBoneAnimation}
     * instances. Each bone's animation includes keyframe stacks for position, rotation, and scale transformations.
     *
     * @param bonesObj The JSON object containing bone animation data, where each key is the bone name and the value is
     *                 an object with keyframe data for scale, position, and rotation.
     * @return An array of {@link AzBoneAnimation} instances representing the deserialized animations for each bone.
     * @throws MolangException If an error occurs during the processing of keyframes or Molang expressions.
     */
    private AzBoneAnimation[] bakeBoneAnimations(JsonObject bonesObj) throws MolangException {
        AzBoneAnimation[] animations = new AzBoneAnimation[bonesObj.size()];
        int index = 0;

        for (Map.Entry<String, JsonElement> entry : bonesObj.entrySet()) {
            JsonObject entryObj = entry.getValue().getAsJsonObject();
            AzKeyframeStack<AzKeyframe<IValue>> scaleFrames = buildKeyframeStack(
                getKeyframes(entryObj.get("scale")),
                false
            );
            AzKeyframeStack<AzKeyframe<IValue>> positionFrames = buildKeyframeStack(
                getKeyframes(entryObj.get("position")),
                false
            );
            AzKeyframeStack<AzKeyframe<IValue>> rotationFrames = buildKeyframeStack(
                getKeyframes(entryObj.get("rotation")),
                true
            );

            animations[index] = new AzBoneAnimation(entry.getKey(), rotationFrames, positionFrames, scaleFrames);
            index++;
        }

        return animations;
    }

    /**
     * Builds a {@link AzKeyframeStack} containing keyframes for X, Y, and Z-axis transformations based on the provided
     * animation data. The method processes a list of paired time-stamped keyframe data, interprets the JSON structures,
     * applies appropriate transformations for rotations (if specified), and generates keyframes with defined easing
     * behaviors.
     *
     * @param entries       A list of {@link Pair} objects containing the timestamp as a {@link String} and associated
     *                      {@link JsonElement} data describing the keyframe. Each entry represents a point in time
     *                      within the animation timeline.
     * @param isForRotation A boolean indicating whether the keyframe transformations should account for rotation. If
     *                      true, the keyframe values undergo additional processing to convert angles to radians.
     * @return A {@link AzKeyframeStack} containing three lists of keyframes for X, Y, and Z transformations,
     *         respectively.
     * @throws MolangException If an error occurs during the parsing or interpretation of Molang expressions in the
     *                         keyframe data.
     */
    private AzKeyframeStack<AzKeyframe<IValue>> buildKeyframeStack(
        List<Pair<String, JsonElement>> entries,
        boolean isForRotation
    ) throws MolangException {
        if (entries.isEmpty())
            return new AzKeyframeStack<>();

        List<AzKeyframe<IValue>> xFrames = new ObjectArrayList<>();
        List<AzKeyframe<IValue>> yFrames = new ObjectArrayList<>();
        List<AzKeyframe<IValue>> zFrames = new ObjectArrayList<>();

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

            JsonArray keyframeVector = element instanceof JsonArray array
                ? array
                : GsonHelper.getAsJsonArray(element.getAsJsonObject(), "vector");
            MolangValue rawXValue = MolangParser.parseJson(keyframeVector.get(0));
            MolangValue rawYValue = MolangParser.parseJson(keyframeVector.get(1));
            MolangValue rawZValue = MolangParser.parseJson(keyframeVector.get(2));
            IValue xValue = isForRotation && rawXValue.isConstant()
                ? new Constant(Math.toRadians(-rawXValue.get()))
                : rawXValue;
            IValue yValue = isForRotation && rawYValue.isConstant()
                ? new Constant(Math.toRadians(-rawYValue.get()))
                : rawYValue;
            IValue zValue = isForRotation && rawZValue.isConstant()
                ? new Constant(Math.toRadians(rawZValue.get()))
                : rawZValue;

            JsonObject entryObj = element instanceof JsonObject obj ? obj : null;
            AzEasingType easingType = entryObj != null && entryObj.has("easing")
                ? AzEasingTypeLoader.fromJson(entryObj.get("easing"))
                : AzEasingTypes.LINEAR;
            List<IValue> easingArgs = entryObj != null && entryObj.has("easingArgs")
                ? JsonUtil.jsonArrayToList(
                    GsonHelper.getAsJsonArray(entryObj, "easingArgs"),
                    ele -> new Constant(ele.getAsDouble())
                )
                : new ObjectArrayList<>();

            xFrames.add(
                new AzKeyframe<>(timeDelta * 20, prevEntry == null ? xValue : xPrev, xValue, easingType, easingArgs)
            );
            yFrames.add(
                new AzKeyframe<>(timeDelta * 20, prevEntry == null ? yValue : yPrev, yValue, easingType, easingArgs)
            );
            zFrames.add(
                new AzKeyframe<>(timeDelta * 20, prevEntry == null ? zValue : zPrev, zValue, easingType, easingArgs)
            );

            xPrev = xValue;
            yPrev = yValue;
            zPrev = zValue;
            prevEntry = entry;
        }

        return new AzKeyframeStack<>(xFrames, yFrames, zFrames);
    }

    private static double readTimestamp(String timestamp) {
        return NumberUtils.isCreatable(timestamp) ? Double.parseDouble(timestamp) : 0;
    }
}
