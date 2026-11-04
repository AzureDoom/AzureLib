package mod.azure.azurelib.compat.geckolib;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;

@ApiStatus.Internal
public final class FabricGeckoLibArmorLookup {

    @SuppressWarnings("unchecked")
    public static HumanoidModel<?> getArmorModel(
        LivingEntity entity,
        ItemStack stack,
        EquipmentSlot slot,
        HumanoidModel<?> fallback
    ) {
        if (!(stack.getItem() instanceof GeoItem)) {
            return fallback;
        }

        return RenderProvider.of(stack)
            .getHumanoidArmorModel(entity, stack, slot, (HumanoidModel<LivingEntity>) fallback);
    }

    private FabricGeckoLibArmorLookup() { /* NO-OP */}
}
