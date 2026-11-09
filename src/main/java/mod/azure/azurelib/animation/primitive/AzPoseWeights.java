package mod.azure.azurelib.animation.primitive;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.*;
import javax.annotation.Nullable;

import mod.azure.azurelib.animation.easing.AzEasingType;
import mod.azure.azurelib.animation.easing.AzEasingTypeLoader;
import mod.azure.azurelib.animation.easing.AzEasingTypes;
import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.molang.MolangParser;
import mod.azure.azurelib.util.function.Double2DoubleFunction;

public final class AzPoseWeights {

    public static final AzPoseWeights NONE = new AzPoseWeights(Collections.emptyList());

    private final List<Entry> entries;

    public AzPoseWeights(List<Entry> entries) {
        this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public List<Entry> entries() {
        return entries;
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Nullable
    public Entry get(String modifierId) {
        for (int i = 0, n = entries.size(); i < n; i++) {
            Entry entry = entries.get(i);
            if (entry.modifierId().equals(modifierId)) {
                return entry;
            }
        }
        return null;
    }

    public static final class Entry {

        private final String modifierId;

        private final double weight;

        private final double fadeIn;

        private final double fadeOut;

        private final boolean restore;

        @Nullable
        private final Track track;

        public Entry(
            String modifierId,
            double weight,
            double fadeIn,
            double fadeOut,
            boolean restore,
            @Nullable Track track
        ) {
            this.modifierId = modifierId;
            this.weight = weight;
            this.fadeIn = fadeIn;
            this.fadeOut = fadeOut;
            this.restore = restore;
            this.track = track;
        }

        public String modifierId() {
            return modifierId;
        }

        public double weight() {
            return weight;
        }

        public double fadeIn() {
            return fadeIn;
        }

        public double fadeOut() {
            return fadeOut;
        }

        public boolean restore() {
            return restore;
        }

        @Nullable
        public Track track() {
            return track;
        }

        public double targetAt(double animTick) {
            return track == null ? weight : track.sample(animTick);
        }
    }

    public static final class Track {

        private final Key[] keys;

        public Track(Key[] keys) {
            this.keys = keys.clone();
        }

        public Key[] keys() {
            return keys.clone();
        }

        public double sample(double tick) {
            if (keys.length == 0) {
                return 1;
            }
            if (tick <= keys[0].tick()) {
                return keys[0].value();
            }

            Key last = keys[keys.length - 1];
            if (tick >= last.tick()) {
                return last.value();
            }

            int i = 0;
            while (i < keys.length - 2 && keys[i + 1].tick() <= tick) {
                i++;
            }

            Key before = keys[i];
            Key after = keys[i + 1];
            if (before.step()) {
                return before.value();
            }

            double span = after.tick() - before.tick();
            double t = span > 1e-9 ? (tick - before.tick()) / span : 1;
            double eased = after.ease(t);
            double a = before.value();
            return clamp01(a + (after.value() - a) * eased);
        }
    }

    public static final class Key {

        private final double tick;

        private final IValue weight;

        private final AzEasingType easing;

        @Nullable
        private final Double easingArg;

        private final boolean step;

        public Key(
            double tick,
            IValue weight,
            AzEasingType easing,
            @Nullable Double easingArg,
            boolean step
        ) {
            this.tick = tick;
            this.weight = weight;
            this.easing = easing;
            this.easingArg = easingArg;
            this.step = step;
        }

        public double tick() {
            return tick;
        }

        public IValue weight() {
            return weight;
        }

        public AzEasingType easing() {
            return easing;
        }

        @Nullable
        public Double easingArg() {
            return easingArg;
        }

        public boolean step() {
            return step;
        }

        public double value() {
            return clamp01(weight.get());
        }

        double ease(double t) {
            if (easing == AzEasingTypes.LINEAR || easing.usesKeyframeData()) {
                return t;
            }
            Double2DoubleFunction curve = easing.buildTransformer(easingArg);
            double eased = curve.get(t);
            return Double.isFinite(eased) ? eased : t;
        }
    }

    public static AzPoseWeights fromJson(JsonObject animationObj) {
        JsonElement poseElement = animationObj.get("pose_weights");
        if (!(poseElement instanceof JsonObject obj)) {
            return NONE;
        }

        if (obj.isEmpty()) {
            return NONE;
        }

        ArrayList<Entry> entries = new ArrayList<>(obj.size());
        for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
            String id = e.getKey();
            if (id.isEmpty()) {
                continue;
            }

            JsonElement entryElement = e.getValue();
            if (entryElement instanceof JsonPrimitive && ((JsonPrimitive) entryElement).isNumber()) {
                entries.add(new Entry(id, clamp01(entryElement.getAsDouble()), 0, 0, true, null));
                continue;
            }
            if (!(entryElement instanceof JsonObject node)) {
                continue;
            }

            JsonElement keyframesElement = node.get("keyframes");
            Track track = keyframesElement instanceof JsonObject
                ? parseTrack((JsonObject) keyframesElement)
                : null;

            boolean restore = true;
            JsonElement restoreElement = node.get("restore");
            if (
                restoreElement instanceof JsonPrimitive
                    && ((JsonPrimitive) restoreElement).isBoolean()
                    && !restoreElement.getAsBoolean()
            ) {
                restore = false;
            }

            entries.add(
                new Entry(
                    id,
                    clamp01(readNumber(node, "weight", 1)),
                    Math.max(0, readNumber(node, "fade_in", 0)),
                    Math.max(0, readNumber(node, "fade_out", 0)),
                    restore,
                    track
                )
            );
        }

        return entries.isEmpty() ? NONE : new AzPoseWeights(entries);
    }

    @Nullable
    private static Track parseTrack(JsonObject keyframes) {
        ArrayList<Key> keys = new ArrayList<>(keyframes.size());
        for (Map.Entry<String, JsonElement> e : keyframes.entrySet()) {
            double seconds;
            try {
                seconds = Double.parseDouble(e.getKey());
            } catch (NumberFormatException ex) {
                continue;
            }

            JsonElement element = e.getValue();
            JsonElement valueElement = element;
            AzEasingType easing = AzEasingTypes.LINEAR;
            Double easingArg = null;
            boolean step = false;

            if (element instanceof JsonArray && !((JsonArray) element).isEmpty()) {
                valueElement = ((JsonArray) element).get(0);
            } else if (element instanceof JsonObject keyObj) {
                valueElement = keyObj.has("value") ? keyObj.get("value") : new JsonPrimitive(0);

                if (keyObj.has("easing")) {
                    easing = AzEasingTypeLoader.fromJson(keyObj.get("easing"));
                }

                JsonElement argsElement = keyObj.get("easingArgs");
                if (argsElement instanceof JsonArray args) {
                    if (
                        !args.isEmpty() && args.get(0) instanceof JsonPrimitive
                            && ((JsonPrimitive) args.get(0)).isNumber()
                    ) {
                        easingArg = args.get(0).getAsDouble();
                    }
                }

                JsonElement interpolation = keyObj.get("interpolation");
                step = interpolation instanceof JsonPrimitive
                    && ((JsonPrimitive) interpolation).isString()
                    && "step".equals(interpolation.getAsString());
            }

            keys.add(new Key(seconds * 20d, MolangParser.parseJson(valueElement), easing, easingArg, step));
        }

        if (keys.isEmpty()) {
            return null;
        }
        keys.sort(Comparator.comparingDouble(Key::tick));
        return new Track(keys.toArray(new Key[0]));
    }

    private static double readNumber(JsonObject obj, String key, double fallback) {
        JsonElement element = obj.get(key);
        return element instanceof JsonPrimitive && ((JsonPrimitive) element).isNumber()
            ? element.getAsDouble()
            : fallback;
    }

    private static double clamp01(double value) {
        return Double.isNaN(value) ? 0 : Math.max(0, Math.min(1, value));
    }
}
