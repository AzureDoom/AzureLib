package mod.azure.azurelib.platform;

import mod.azure.azurelib.platform.services.IClientPlatformHelper;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

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
