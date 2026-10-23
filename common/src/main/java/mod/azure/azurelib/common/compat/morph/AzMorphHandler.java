package mod.azure.azurelib.common.compat.morph;

import net.minecraft.world.entity.LivingEntity;

@FunctionalInterface
public interface AzMorphHandler<T extends LivingEntity> {

    void onMorphTick(T shape, AzMorphContext context);
}
