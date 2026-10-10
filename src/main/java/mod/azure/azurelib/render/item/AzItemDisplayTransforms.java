package mod.azure.azurelib.render.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.render.vertex.GlStateManager;

/**
 * The {@code display} transforms from an item's model json ({@code assets/<modid>/models/item/<name>.json}), the file
 * Blockbench exports and that modern versions read for {@code builtin/entity} items. 1.7.10 has no json item models, so
 * AzureLib reads and applies the transforms itself. Missing files or contexts simply apply no transform.
 */
public final class AzItemDisplayTransforms {

    private static final AzItemDisplayTransforms EMPTY = new AzItemDisplayTransforms(
        new EnumMap<AzItemDisplayContext, float[]>(AzItemDisplayContext.class)
    );

    private static final Map<Item, AzItemDisplayTransforms> CACHE = new ConcurrentHashMap<>();

    /** translation (x, y, z), rotation (x, y, z) in degrees, scale (x, y, z) */
    private final Map<AzItemDisplayContext, float[]> transforms;

    private AzItemDisplayTransforms(Map<AzItemDisplayContext, float[]> transforms) {
        this.transforms = transforms;
    }

    /** Forget loaded transforms, so they are re-read after a resource reload. */
    public static void clearCache() {
        CACHE.clear();
    }

    public static AzItemDisplayTransforms forItem(Item item) {
        AzItemDisplayTransforms cached = CACHE.get(item);

        if (cached == null) {
            cached = load(item);
            CACHE.put(item, cached);
        }

        return cached;
    }

    private static AzItemDisplayTransforms load(Item item) {
        Object name = Item.itemRegistry.getNameForObject(item);

        if (name == null) {
            return EMPTY;
        }

        ResourceLocation id = new ResourceLocation(name.toString());
        ResourceLocation modelLocation = new ResourceLocation(
            id.getResourceDomain(),
            "models/item/" + id.getResourcePath() + ".json"
        );

        try (
            InputStream stream = Minecraft.getMinecraft()
                .getResourceManager()
                .getResource(modelLocation)
                .getInputStream()
        ) {
            JsonElement root = new JsonParser().parse(IOUtils.toString(stream, StandardCharsets.UTF_8));

            if (!root.isJsonObject() || !root.getAsJsonObject().has("display")) {
                return EMPTY;
            }

            JsonObject display = root.getAsJsonObject().getAsJsonObject("display");
            Map<AzItemDisplayContext, float[]> transforms = new EnumMap<>(AzItemDisplayContext.class);

            for (AzItemDisplayContext context : AzItemDisplayContext.values()) {
                if (display.has(context.jsonKey())) {
                    transforms.put(context, parse(display.getAsJsonObject(context.jsonKey())));
                }
            }

            // Like vanilla, the left-hand transforms default to the right-hand ones.
            copyIfMissing(
                transforms,
                AzItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                AzItemDisplayContext.THIRD_PERSON_LEFT_HAND
            );
            copyIfMissing(
                transforms,
                AzItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                AzItemDisplayContext.FIRST_PERSON_LEFT_HAND
            );

            return new AzItemDisplayTransforms(transforms);
        } catch (Exception e) {
            AzureLib.LOGGER.debug("No item display transforms at {}", modelLocation);
            return EMPTY;
        }
    }

    private static void copyIfMissing(
        Map<AzItemDisplayContext, float[]> transforms,
        AzItemDisplayContext from,
        AzItemDisplayContext to
    ) {
        if (!transforms.containsKey(to) && transforms.containsKey(from)) {
            transforms.put(to, transforms.get(from));
        }
    }

    private static float[] parse(JsonObject json) {
        float[] translation = vec(json, "translation", 0);
        float[] rotation = vec(json, "rotation", 0);
        float[] scale = vec(json, "scale", 1);
        float[] out = new float[9];

        // Same limits and unit conversion as 1.12.2's ItemTransformVec3f deserializer.
        for (int i = 0; i < 3; i++) {
            out[i] = Math.max(-5.0F, Math.min(5.0F, translation[i] * 0.0625F));
            out[3 + i] = rotation[i];
            out[6 + i] = Math.max(-4.0F, Math.min(4.0F, scale[i]));
        }

        return out;
    }

    private static float[] vec(JsonObject json, String key, float fallback) {
        float[] out = { fallback, fallback, fallback };

        if (json.has(key) && json.get(key).isJsonArray()) {
            JsonArray array = json.getAsJsonArray(key);

            for (int i = 0; i < 3 && i < array.size(); i++) {
                out[i] = array.get(i).getAsFloat();
            }
        }

        return out;
    }

    /**
     * The Y scale of the context's transform, or 1 if the model has none.
     */
    public float scaleY(AzItemDisplayContext context) {
        float[] t = this.transforms.get(context);
        return t == null ? 1.0F : t[7];
    }

    /**
     * Applies the transform for the context to the current GL matrix, around the item's center.
     */
    public void apply(AzItemDisplayContext context) {
        float[] t = this.transforms.get(context);

        if (t == null) {
            return;
        }

        // Same as 1.12's ItemTransformVec3f: translate, rotate by the quaternion qX * qY * qZ (so the GL calls go X, Y,
        // Z), then scale.
        GlStateManager.translate(t[0], t[1], t[2]);
        GlStateManager.rotate(t[3], 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(t[4], 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(t[5], 0.0F, 0.0F, 1.0F);
        GlStateManager.scale(t[6], t[7], t[8]);
    }
}
