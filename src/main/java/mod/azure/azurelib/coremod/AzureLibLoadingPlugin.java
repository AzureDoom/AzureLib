package mod.azure.azurelib.coremod;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.Name("AzureLib")
public class AzureLibLoadingPlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {

    private static final String MIXIN_LIST = "/azurelib.mixins.json";

    @Override
    public String getMixinConfig() {
        return "azurelib.early.mixins.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        List<String> mixins = new ArrayList<>();

        try (InputStream stream = AzureLibLoadingPlugin.class.getResourceAsStream(MIXIN_LIST)) {
            if (stream == null) {
                throw new IllegalStateException("Missing " + MIXIN_LIST);
            }

            JsonObject json = new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8))
                .getAsJsonObject();
            addAll(json, "mixins", mixins);

            if (FMLLaunchHandler.side().isClient()) {
                addAll(json, "client", mixins);
            } else {
                addAll(json, "server", mixins);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read " + MIXIN_LIST, e);
        }

        return mixins;
    }

    private static void addAll(JsonObject json, String key, List<String> out) {
        if (json.has(key) && json.get(key).isJsonArray()) {
            JsonArray array = json.getAsJsonArray(key);

            for (JsonElement element : array) {
                out.add(element.getAsString());
            }
        }
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
