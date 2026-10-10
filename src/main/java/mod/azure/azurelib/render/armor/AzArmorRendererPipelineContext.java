package mod.azure.azurelib.render.armor;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mod.azure.azurelib.core.object.Color;
import mod.azure.azurelib.render.AzRendererPipeline;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.armor.bone.AzArmorBoneContext;
import mod.azure.azurelib.render.vertex.MultiBufferSource;
import mod.azure.azurelib.render.vertex.RenderType;
import mod.azure.azurelib.util.AzEquipmentSlot;

public class AzArmorRendererPipelineContext extends AzRendererPipelineContext<UUID, ItemStack> {

    private final AzArmorBoneContext boneContext;

    private ModelBiped baseModel;

    private AzEquipmentSlot currentSlot;

    private ItemStack currentStack;

    private boolean translucent = false;

    public AzArmorRendererPipelineContext(AzRendererPipeline<UUID, ItemStack> rendererPipeline) {
        super(rendererPipeline);
        this.baseModel = null;
        this.boneContext = new AzArmorBoneContext();
        this.currentEntity = null;
        this.currentSlot = null;
        this.currentStack = null;
    }

    @Override
    public @Nonnull RenderType getDefaultRenderType(
        ItemStack animatable,
        ResourceLocation texture,
        @Nullable MultiBufferSource bufferSource,
        float partialTick,
        RenderType defaultRenderType,
        float alpha
    ) {
        return translucent
            ? RenderType.itemEntityTranslucentCull(texture)
            : defaultRenderType;
    }

    public void prepare(
        @Nullable Entity entity,
        ItemStack stack,
        @Nullable AzEquipmentSlot slot,
        @Nullable ModelBiped baseModel
    ) {
        this.baseModel = baseModel;
        this.currentEntity = entity;
        this.currentStack = stack;
        this.animatable = stack;
        this.currentSlot = slot;
    }

    /**
     * Sets whether the rendering pipeline should render with a translucent effect or not.
     *
     * @param translucent A boolean value indicating whether to enable or disable translucency. If true, the rendering
     *                    pipeline will apply a translucent effect to rendered elements. If false, it will render with
     *                    an opaque effect.
     */
    public void setTranslucent(boolean translucent) {
        this.translucent = translucent;
    }

    /**
     * Gets a tint-applying color to render the given animatable with
     * <p>
     * Returns {@link Color#WHITE} by default
     */
    @Override
    public Color getRenderColor(ItemStack animatable, float partialTick, int packedLight) {
        if (
            this.currentStack.getItem() instanceof ItemArmor && ((ItemArmor) this.currentStack.getItem())
                .getArmorMaterial() == ItemArmor.ArmorMaterial.CLOTH
        ) {
            return Color.ofOpaque(((ItemArmor) this.currentStack.getItem()).getColor(animatable));
        }

        return Color.WHITE;
    }

    public ModelBiped baseModel() {
        return baseModel;
    }

    public AzArmorBoneContext boneContext() {
        return boneContext;
    }

    public Entity currentEntity() {
        return currentEntity;
    }

    public AzEquipmentSlot currentSlot() {
        return currentSlot;
    }

    public ItemStack currentStack() {
        return currentStack;
    }
}
