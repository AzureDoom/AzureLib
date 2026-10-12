package mod.azure.azurelib.fabric.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.config.ConfigHolder;
import mod.azure.azurelib.config.ConfigHolderRegistry;
import mod.azure.azurelib.config.client.ConfigurationClient;

public final class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> ConfigurationClient.getScreenForGroup(
            ConfigHolderRegistry.getConfigsByGroup(AzureLib.MOD_ID),
            AzureLib.MOD_ID,
            parent
        );
    }

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        Map<String, ConfigScreenFactory<?>> map = new HashMap<>();
        for (
            Map.Entry<String, List<ConfigHolder<?>>> entry : ConfigHolderRegistry.getConfigGroupingByGroup().entrySet()
        ) {
            var group = entry.getKey();
            if (group.equals(AzureLib.MOD_ID))
                continue;
            var holders = entry.getValue();
            map.put(group, parent -> ConfigurationClient.getScreenForGroup(holders, group, parent));
        }
        return map;
    }
}
