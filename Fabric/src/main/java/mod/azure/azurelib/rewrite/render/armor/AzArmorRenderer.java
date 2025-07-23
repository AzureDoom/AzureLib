package mod.azure.azurelib.rewrite.render.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.rewrite.animation.impl.AzItemAnimator;
import mod.azure.azurelib.rewrite.model.AzBakedModel;
import mod.azure.azurelib.rewrite.render.AzProvider;
import mod.azure.azurelib.rewrite.render.AzRendererConfig;

public class AzArmorRenderer {

    private final AzProvider<ItemStack> provider;

    private final AzArmorRendererPipeline rendererPipeline;

    @Nullable
    private AzItemAnimator reusedAzItemAnimator;

    public AzArmorRenderer(AzRendererConfig<ItemStack> config) {
        this.provider = new AzProvider<>(config::createAnimator, config::modelLocation);
        this.rendererPipeline = createPipeline(config);
    }

    protected AzArmorRendererPipeline createPipeline(AzRendererConfig config) {
        return new AzArmorRendererPipeline(config, this);
    }

    /**
     * Prepare the renderer for the current render cycle.<br>
     * Must be called prior to render as the default HumanoidModel doesn't give render context.<br>
     * Params have been left nullable so that the renderer can be called for model/texture purposes safely. If you do
     * grab the renderer using null parameters, you should not use it for actual rendering.
     *
     * @param entity    The entity being rendered with the armor on
     * @param stack     The ItemStack being rendered
     * @param slot      The slot being rendered
     * @param baseModel The default (vanilla) model that would have been rendered if this model hadn't replaced it
     */
    public void prepForRender(
        @Nullable Entity entity,
        ItemStack stack,
        @Nullable EquipmentSlot slot,
        @Nullable HumanoidModel<?> baseModel
    ) {
        if (entity == null || slot == null || baseModel == null) {
            return;
        }

        rendererPipeline.context().prepare(entity, stack, slot, baseModel);

        @Nullable
        AzBakedModel model = provider.provideBakedModel(stack);
        prepareAnimator(stack, model);
    }

    private void prepareAnimator(ItemStack stack, AzBakedModel model) {
        AzItemAnimator cachedEntityAnimator = (AzItemAnimator) provider.provideAnimator(stack);

        if (cachedEntityAnimator != null && model != null) {
            cachedEntityAnimator.setActiveModel(model);
        }

        // Point the renderer's current animator reference to the cached entity animator before rendering.
        reusedAzItemAnimator = cachedEntityAnimator;
    }

    public @Nullable AzItemAnimator animator() {
        return reusedAzItemAnimator;
    }

    public AzProvider<ItemStack> provider() {
        return provider;
    }

    public AzArmorRendererPipeline rendererPipeline() {
        return rendererPipeline;
    }
}
