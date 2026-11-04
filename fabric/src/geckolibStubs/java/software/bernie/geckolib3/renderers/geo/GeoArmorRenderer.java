package software.bernie.geckolib3.renderers.geo;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib3.model.AnimatedGeoModel;

import java.util.Map;

/**
 * COMPILE-ONLY STUB of GeckoLib 3 (Fabric 1.18.2). Not packaged; the real class comes from GeckoLib at runtime.
 * <p>
 * GeckoLib's Fabric jar can't be remapped to Mojang mappings (GeoProjectilesRenderer#getTextureLocation clashes with
 * EntityRenderer#getTextureLocation), so AzureLib compiles against these stubs instead. Only declare members
 * FabricGeckoLibArmor actually uses, with exactly the real descriptors: remapJar rewrites the Minecraft types to
 * intermediary, and the GeckoLib names stay as they are.
 */
@SuppressWarnings("rawtypes")
public class GeoArmorRenderer<T> {

    public static final Map<Class<? extends ArmorItem>, GeoArmorRenderer> renderers = null;

    public String headBone;

    public String bodyBone;

    public String rightArmBone;

    public String leftArmBone;

    public String rightLegBone;

    public String leftLegBone;

    public String rightBootBone;

    public String leftBootBone;

    public GeoArmorRenderer<T> setCurrentItem(
        LivingEntity entityLiving,
        ItemStack itemStack,
        EquipmentSlot armorSlot,
        HumanoidModel model
    ) {
        throw new AssertionError("stub");
    }

    public void render(PoseStack stack, MultiBufferSource bufferIn, int packedLightIn) {
        throw new AssertionError("stub");
    }

    public AnimatedGeoModel<T> getGeoModelProvider() {
        throw new AssertionError("stub");
    }
}
