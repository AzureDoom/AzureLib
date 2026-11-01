package mod.azure.azurelib.mixins.compat;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.util.List;
import java.util.Set;

public final class JegMixinPlugin implements IMixinConfigPlugin {

    private static final String JEG_PROJECTILE =
        "ttv.migami.jeg.entity.projectile.ProjectileEntity";

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(
        String targetClassName,
        String mixinClassName
    ) {
        if (!mixinClassName.endsWith("JegProjectileMixin")) {
            return true;
        }

        try {
            ClassNode node = MixinService.getService()
                .getBytecodeProvider()
                .getClassNode(JEG_PROJECTILE);

            return node != null;
        } catch (Exception exception) {
            return false;
        }
    }

    @Override
    public void acceptTargets(
        Set<String> myTargets,
        Set<String> otherTargets
    ) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(
        String targetClassName,
        ClassNode targetClass,
        String mixinClassName,
        IMixinInfo mixinInfo
    ) {}

    @Override
    public void postApply(
        String targetClassName,
        ClassNode targetClass,
        String mixinClassName,
        IMixinInfo mixinInfo
    ) {}
}
