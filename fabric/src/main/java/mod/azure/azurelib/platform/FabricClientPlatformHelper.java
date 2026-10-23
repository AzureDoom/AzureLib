package mod.azure.azurelib.platform;

import mod.azure.azurelib.platform.services.IClientPlatformHelper;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class FabricClientPlatformHelper implements IClientPlatformHelper {

    @Override
    public String getArmorTexture(
            LivingEntity entity,
            ItemStack stack,
            EquipmentSlot slot,
            String defaultTexture,
            @Nullable String type
    ) {
        return defaultTexture;
    }

    @Override
    public HumanoidModel<?> getArmorModel(
            LivingEntity entity,
            ItemStack stack,
            EquipmentSlot slot,
            HumanoidModel<?> fallback
    ) {
        return fallback;
    }
}
