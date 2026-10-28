package mod.azure.azurelib.mixins;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.animation.molang.AzMolangQueryContext;
import mod.azure.azurelib.render.item.AzItemDisplayContextCapture;
import mod.azure.azurelib.render.item.AzItemHolderCarrier;

/**
 * Exposes the item's {@link ItemDisplayContext} to AzureLib's special item renderer, which 26.x no longer passes it to.
 * Captured for the duration of the render state's submit and restored afterward, so nested item renders work.
 * <p>
 * Also carries the holder recorded by {@link ItemModelResolverMixin_AzItemHolder} into submit, where it is pushed onto
 * {@link AzMolangQueryContext} so item animations answer entity queries for the holder, like Bedrock attachables.
 * </p>
 */
@Mixin(ItemStackRenderState.class)
public abstract class ItemStackRenderStateMixin_AzDisplayContext implements AzItemHolderCarrier {

    @Shadow
    ItemDisplayContext displayContext;

    @Unique
    private int azurelib$holderId = AzItemHolderCarrier.NO_HOLDER;

    @Override
    public void azurelib$setHolderId(int entityId) {
        this.azurelib$holderId = entityId;
    }

    @Override
    public int azurelib$getHolderId() {
        return azurelib$holderId;
    }

    @Inject(method = "submit", at = @At("HEAD"))
    private void azurelib$captureDisplayContext(
        CallbackInfo ci,
        @Share("previous_display_context") LocalRef<ItemDisplayContext> previous
    ) {
        previous.set(AzItemDisplayContextCapture.set(this.displayContext));
        AzMolangQueryContext.INSTANCE.pushItemHolder(azurelib$resolveHolder());
    }

    @Inject(method = "submit", at = @At("RETURN"))
    private void azurelib$restoreDisplayContext(
        CallbackInfo ci,
        @Share("previous_display_context") LocalRef<ItemDisplayContext> previous
    ) {
        AzMolangQueryContext.INSTANCE.popItemHolder();
        AzItemDisplayContextCapture.set(previous.get());
    }

    @Unique
    private LivingEntity azurelib$resolveHolder() {
        if (azurelib$holderId == AzItemHolderCarrier.NO_HOLDER) {
            return null;
        }

        var level = Minecraft.getInstance().level;
        return level != null && level.getEntity(azurelib$holderId) instanceof LivingEntity living ? living : null;
    }
}
