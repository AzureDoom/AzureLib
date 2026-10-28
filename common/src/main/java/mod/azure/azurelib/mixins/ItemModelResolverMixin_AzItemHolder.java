package mod.azure.azurelib.mixins;

import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.render.item.AzItemHolderCarrier;

/**
 * Records which living entity holds an item while its render state is being filled in. {@code updateForLiving},
 * {@code updateForNonLiving} and {@code updateForTopItem} all funnel through {@code appendItemLayers}, so this one
 * injection covers hands, armor layers, item frames and AzureLib's own bone item layers.
 * <p>
 * Optional, like the 1.21.1 {@code renderStatic} hook: if the method's signature changes, the injection is skipped and
 * item animations simply see no holder.
 * </p>
 */
@Mixin(ItemModelResolver.class)
public abstract class ItemModelResolverMixin_AzItemHolder {

    @Inject(
        method = "appendItemLayers(Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/ItemOwner;I)V",
        at = @At("HEAD"),
        require = 0
    )
    private void azurelib$recordItemHolder(
        ItemStackRenderState output,
        ItemStack stack,
        ItemDisplayContext displayContext,
        @Nullable Level level,
        @Nullable ItemOwner owner,
        int seed,
        CallbackInfo ci
    ) {
        ((AzItemHolderCarrier) output).azurelib$setHolderId(
            owner instanceof LivingEntity living ? living.getId() : AzItemHolderCarrier.NO_HOLDER
        );
    }
}
