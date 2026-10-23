package mod.azure.azurelib.platform;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.platform.services.IClientPlatformHelper;

public class NeoForgeClientPlatformHelper implements IClientPlatformHelper {

    @Override
    public String getArmorTexture(
        LivingEntity entity,
        ItemStack stack,
        EquipmentSlot slot,
        String defaultTexture,
        @Nullable String type
    ) {
        return ForgeHooksClient.getArmorTexture(entity, stack, defaultTexture, slot, type);
    }

    @Override
    public @Nullable HumanoidModel<?> getArmorModel(
        LivingEntity entity,
        ItemStack stack,
        EquipmentSlot slot,
        HumanoidModel<?> fallback
    ) {
        Model model = ForgeHooksClient.getArmorModel(entity, stack, slot, fallback);
        return model instanceof HumanoidModel<?> humanoid ? humanoid : null;
    }
}
