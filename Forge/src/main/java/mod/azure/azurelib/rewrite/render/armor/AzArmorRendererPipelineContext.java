package mod.azure.azurelib.rewrite.render.armor;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import mod.azure.azurelib.core.object.Color;
import mod.azure.azurelib.rewrite.render.AzRendererPipeline;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;
import mod.azure.azurelib.rewrite.render.armor.bone.AzArmorBoneContext;

public class AzArmorRendererPipelineContext extends AzRendererPipelineContext<ItemStack> {

    private final AzArmorBoneContext boneContext;

    private BipedModel<?> baseModel;

    private Entity currentEntity;

    private EquipmentSlotType currentSlot;

    private ItemStack currentStack;

    private boolean translucent = false;

    public AzArmorRendererPipelineContext(AzRendererPipeline<ItemStack> rendererPipeline) {
        super(rendererPipeline);
        this.baseModel = null;
        this.boneContext = new AzArmorBoneContext();
        this.currentEntity = null;
        this.currentSlot = null;
        this.currentStack = null;
    }

    @Override
    public RenderType getDefaultRenderType(
        ItemStack animatable,
        ResourceLocation texture,
        IRenderTypeBuffer bufferSource,
        float partialTick
    ) {
        return translucent
            ? RenderType.getEntityTranslucent(texture)
            : RenderType.getEntityCutoutNoCull(texture);
    }

    public void prepare(
        Entity entity,
        ItemStack stack,
        EquipmentSlotType slot,
        BipedModel<?> baseModel
    ) {
        this.baseModel = baseModel;
        this.currentEntity = entity;
        this.currentStack = stack;
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
        return this.currentStack.getItem() instanceof DyeableArmorItem
            ? Color.ofOpaque(((DyeableArmorItem) this.currentStack.getItem()).getColor(animatable))
            : Color.WHITE;
    }

    public BipedModel<?> baseModel() {
        return baseModel;
    }

    public AzArmorBoneContext boneContext() {
        return boneContext;
    }

    public Entity currentEntity() {
        return currentEntity;
    }

    public EquipmentSlotType currentSlot() {
        return currentSlot;
    }

    public ItemStack currentStack() {
        return currentStack;
    }
}
