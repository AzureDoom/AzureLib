package mod.azure.azurelib.common.internal.mixins;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemRenderer.class)
public interface ItemRendererAccessor {

    @Accessor("blockEntityRenderer")
    BlockEntityWithoutLevelRenderer getBlockEntityRenderer();
}
