package mod.azure.azurelib.integration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import mod.azure.azurelib.AzureLibMod;
import mod.azure.azurelib.config.ConfigHolder;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        Map<String, ConfigScreenFactory<?>> map = new HashMap<>();
        Map<String, List<ConfigHolder<?>>> byGroup = ConfigHolder.getConfigGroupingByGroup();
        for (Map.Entry<String, List<ConfigHolder<?>>> entry : byGroup.entrySet()) {
            String group = entry.getKey();
            List<ConfigHolder<?>> configHolders = entry.getValue();
            ConfigScreenFactory<?> factory = parent -> {
                int i = configHolders.size();
                if (i > 1) {
                    return AzureLibMod.getConfigScreenByGroup(configHolders, group, parent);
                } else if (i == 1) {
                    return AzureLibMod.getConfigScreenForHolder(configHolders.get(0), parent);
                }
                return null;
            };
            map.put(group, factory);
        }
        return map;
    }
}
