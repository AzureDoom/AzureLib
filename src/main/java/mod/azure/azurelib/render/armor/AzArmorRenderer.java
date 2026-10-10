package mod.azure.azurelib.render.armor;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

import java.util.UUID;
import javax.annotation.Nullable;

import mod.azure.azurelib.animation.impl.AzItemAnimator;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.render.AzProvider;
import mod.azure.azurelib.render.AzRendererConfig;
import mod.azure.azurelib.util.AzEquipmentSlot;
import mod.azure.azurelib.util.AzItemIds;

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
            AzArmorRenderer::getStackId
        );
        this.rendererPipeline = createPipeline(config);
    }

    protected AzArmorRendererPipeline createPipeline(AzRendererConfig config) {
        return new AzArmorRendererPipeline(config, this);
    }

    /**
     * Prepare the renderer for the current render cycle.<br>
     * Must be called prior to render as the default ModelBiped doesn't give render context.<br>
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
        @Nullable AzEquipmentSlot slot,
        @Nullable ModelBiped baseModel
    ) {
        if (entity == null || slot == null || baseModel == null) {
            return;
        }

        this.entity = entity;

        rendererPipeline.context().prepare(entity, stack, slot, baseModel);

        AzBakedModel model = provider.provideBakedModel(entity, stack);
        prepareAnimator(stack, model);
    }

    private void prepareAnimator(ItemStack stack, AzBakedModel model) {
        // Point the renderer's current animator reference to the cached entity animator before rendering.
        reusedAzItemAnimator = (AzItemAnimator) provider.provideAnimator(entity, stack);
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

    private static UUID getStackId(ItemStack stack) {
        net.minecraft.nbt.NBTTagCompound tag = stack.getTagCompound();

        if (tag != null && AzItemIds.has(tag)) {
            return AzItemIds.get(tag);
        }

        return UUID.randomUUID();
    }
}
