package mod.azure.azurelib.common.internal.mixins;

import mod.azure.azurelib.common.platform.Services;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class AzureLibMixinPlugin implements IMixinConfigPlugin {

    private static final Map<String, Supplier<Boolean>> CONDITIONS = Map.of(
            "mod.azure.azurelib.common.internal.mixins.NeoMixinHumanoidArmorLayer", () -> Services.PLATFORM.getPlatformName().equalsIgnoreCase("neoforge"),
            "mod.azure.azurelib.common.internal.mixins.FabricMixinHumanoidArmorLayer", () -> Services.PLATFORM.getPlatformName().equalsIgnoreCase("fabric"));

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return CONDITIONS.getOrDefault(mixinClassName, () -> true).get();
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return Collections.emptyList();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
