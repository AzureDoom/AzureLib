package mod.azure.azurelib.compat.geckolib;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

import mod.azure.azurelib.platform.Services;

public final class GeckoLibArmorCompat {

    public static final String MOD_ID = "geckolib";

    private static Boolean isLoaded;

    public static boolean isLoaded() {
        if (isLoaded == null) {
            isLoaded = Services.PLATFORM.isModLoaded(MOD_ID);
        }
        return isLoaded;
    }

    public static boolean hasArmorRenderer(ItemStack stack, EquipmentSlot slot) {
        return isLoaded() && GeckoLibArmorBridge.hasArmorRenderer(stack, slot);
    }

    public static @Nullable BiConsumer<PoseStack, SubmitNodeCollector> prepareArmorPiece(
        LivingEntity entity,
        ItemStack stack,
        EquipmentSlot slot,
        HumanoidModel<?> baseModel,
        ModelPart modelPart,
        float partialTick,
        int packedLight,
        int packedOverlay
    ) {
        if (!isLoaded()) {
            return null;
        }

        return GeckoLibArmorBridge.prepareArmorPiece(
            entity,
            stack,
            slot,
            baseModel,
            modelPart,
            partialTick,
            packedLight,
            packedOverlay
        );
    }

    private GeckoLibArmorCompat() { /* NO-OP */}
}
