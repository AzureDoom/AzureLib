package mod.azure.azurelib.compat.morph;

import net.minecraft.world.entity.LivingEntity;

@FunctionalInterface
public interface AzMorphHandler<T extends LivingEntity> {

    void onMorphTick(T shape, AzMorphContext context);
}
