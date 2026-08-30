/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright (c) 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import mod.azure.azurelib.render.armor.AzArmorRenderStateCache;
import mod.azure.azurelib.render.armor.AzArmorRendererRegistry;

@Mixin(HumanoidArmorLayer.class)
public abstract class MixinHumanoidArmorLayer<S extends HumanoidRenderState, M extends HumanoidModel<S>, A extends HumanoidModel<S>> extends RenderLayer<S, M> {

    public MixinHumanoidArmorLayer(RenderLayerParent<S, M> renderer) {
        super(renderer);
    }

    /**
     * Replaces vanilla's armor-piece submission only when AzureLib has a renderer registered and all required render
     * context is available. Returning true leaves vanilla rendering untouched.
     */
    @WrapWithCondition(
        method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V"
        )
    )
    private boolean azurelib$wrapArmorPieceRender(
        HumanoidArmorLayer<S, M, A> layer,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        ItemStack itemStack,
        EquipmentSlot slot,
        int lightCoords,
        S state
    ) {
        var renderer = AzArmorRendererRegistry.getOrNull(itemStack);
        if (renderer == null) {
            return true;
        }

        var entity = AzArmorRenderStateCache.get(state);
        if (entity == null) {
            return true;
        }

        var armorModel = layer.getArmorModel(state, slot);

        return !renderer.render(
            poseStack,
            submitNodeCollector,
            entity,
            state,
            itemStack,
            slot,
            armorModel,
            lightCoords
        );
    }
}
