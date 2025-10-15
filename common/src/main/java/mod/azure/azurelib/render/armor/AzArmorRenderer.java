package mod.azure.azurelib.render.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.impl.AzItemAnimator;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.render.AzProvider;
import mod.azure.azurelib.render.AzRendererConfig;

public class AzArmorRenderer {

    private Entity entity;

    private final AzProvider<UUID, ItemStack> provider;

    private final AzArmorRendererPipeline rendererPipeline;

    @Nullable
    private AzItemAnimator reusedAzItemAnimator;

    public AzArmorRenderer(AzArmorRendererConfig config) {
        this.provider = new AzProvider<>(
            config::createAnimator,
            config::modelLocation,
            animator -> {
                if (animator.getTag() != null && animator.getTag().contains(AzureLib.ITEM_UUID_TAG)) {
                    animator.getTag().getUUID(AzureLib.ITEM_UUID_TAG);
                }
                return UUID.randomUUID();
            }
        );
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

        this.entity = entity;

        rendererPipeline.context().prepare(entity, stack, slot, baseModel);

        var model = provider.provideBakedModel(entity, stack);
        prepareAnimator(stack, model);
    }

    private void prepareAnimator(ItemStack stack, AzBakedModel model) {
        // Point the renderer's current animator reference to the cached entity animator before rendering.
        reusedAzItemAnimator = (AzItemAnimator) provider.provideAnimator(this.entity, stack);
    }

    public @Nullable AzItemAnimator animator() {
        return reusedAzItemAnimator;
    }

    public AzProvider<UUID, ItemStack> provider() {
        return provider;
    }

    public AzArmorRendererPipeline rendererPipeline() {
        return rendererPipeline;
    }
}
