package mod.azure.azurelib.mixins;

import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class AzureLibMixinPlugin implements IMixinConfigPlugin {

    private static final Logger LOGGER = LogManager.getLogger("AzureLib");

    private static final String REACHFIX_HOOK_RESOURCE = "meldexun/reachfix/hook/NetHandlerPlayServerHook.class";

    private static final String VANILLA_REACH_MIXIN =
        "mod.azure.azurelib.mixins.NetHandlerPlayServerMixin_AzHitboxReach";

    private static final String REACHFIX_REACH_MIXIN = "compat.reachfix.ReachFixHookMixin_AzHitboxReach";

    private boolean reachFixPresent;

    @Override
    public void onLoad(String mixinPackage) {
        reachFixPresent = hasResource(REACHFIX_HOOK_RESOURCE);
        if (reachFixPresent) {
            LOGGER.info("ReachFix detected, using ReachFix-compatible hitbox reach check");
        }
    }

    private static boolean hasResource(String path) {
        try {
            return Launch.classLoader.getResource(path) != null;
        } catch (Throwable t) {
            ClassLoader fallback = AzureLibMixinPlugin.class.getClassLoader();
            return fallback != null && fallback.getResource(path) != null;
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return !(reachFixPresent && VANILLA_REACH_MIXIN.equals(mixinClassName));
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return reachFixPresent ? Collections.singletonList(REACHFIX_REACH_MIXIN) : null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
