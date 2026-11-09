package mod.azure.azurelib.animation.primitive;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import mod.azure.azurelib.animation.easing.AzEasingType;
import mod.azure.azurelib.animation.easing.AzEasingTypeLoader;
import mod.azure.azurelib.animation.easing.AzEasingTypes;
import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.molang.MolangParser;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public record AzPoseWeights(List<Entry> entries) {

    public static final AzPoseWeights NONE = new AzPoseWeights(List.of());

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public @Nullable Entry get(String modifierId) {
        for (int i = 0, n = entries.size(); i < n; i++) {
            Entry entry = entries.get(i);

            if (entry.modifierId().equals(modifierId)) {
                return entry;
            }
        }

        return null;
    }

    public record Entry(
        String modifierId,
        double weight,
        double fadeIn,
        double fadeOut,
        boolean restore,
        @Nullable Track track
    ) {

        public double targetAt(double animTick) {
            return track == null ? weight : track.sample(animTick);
        }
    }

    public record Track(Key[] keys) {

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

    public record Key(double tick, IValue weight, AzEasingType easing, @Nullable Double easingArg, boolean step) {

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
        if (!(animationObj.get("pose_weights") instanceof JsonObject obj) || obj.isEmpty()) {
            return NONE;
        }

        ArrayList<Entry> entries = new ArrayList<>(obj.size());

        for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
            String id = e.getKey();

            if (id.isEmpty()) {
                continue;
            }

            if (e.getValue() instanceof JsonPrimitive primitive && primitive.isNumber()) {
                entries.add(new Entry(id, clamp01(primitive.getAsDouble()), 0, 0, true, null));
                continue;
            }

            if (!(e.getValue() instanceof JsonObject node)) {
                continue;
            }

            Track track = node.get("keyframes") instanceof JsonObject keyframes ? parseTrack(keyframes) : null;

            entries.add(
                new Entry(
                    id,
                    clamp01(readNumber(node, "weight", 1)),
                    Math.max(0, readNumber(node, "fade_in", 0)),
                    Math.max(0, readNumber(node, "fade_out", 0)),
                    !(node.get("restore") instanceof JsonPrimitive restore && restore.isBoolean()
                        && !restore.getAsBoolean()),
                    track
                )
            );
        }

        return entries.isEmpty() ? NONE : new AzPoseWeights(List.copyOf(entries));
    }

    private static @Nullable Track parseTrack(JsonObject keyframes) {
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

            if (element instanceof JsonArray array && !array.isEmpty()) {
                valueElement = array.get(0);
            } else if (element instanceof JsonObject keyObj) {
                valueElement = keyObj.has("value") ? keyObj.get("value") : new JsonPrimitive(0);

                if (keyObj.has("easing")) {
                    easing = AzEasingTypeLoader.fromJson(keyObj.get("easing"));
                }

                if (keyObj.get("easingArgs") instanceof JsonArray args && !args.isEmpty()
                    && args.get(0) instanceof JsonPrimitive arg && arg.isNumber()) {
                    easingArg = arg.getAsDouble();
                }

                step = keyObj.get("interpolation") instanceof JsonPrimitive interpolation
                    && interpolation.isString()
                    && "step".equals(interpolation.getAsString());
            }

            keys.add(new Key(seconds * 20d, MolangParser.parseJson(valueElement), easing, easingArg, step));
        }

        if (keys.isEmpty()) {
            return null;
        }

        keys.sort(Comparator.comparingDouble(Key::tick));
        return new Track(keys.toArray(Key[]::new));
    }

    private static double readNumber(JsonObject obj, String key, double fallback) {
        return obj.get(key) instanceof JsonPrimitive primitive && primitive.isNumber()
            ? primitive.getAsDouble()
            : fallback;
    }

    private static double clamp01(double value) {
        return Double.isNaN(value) ? 0 : Math.clamp(value, 0, 1);
    }
}
