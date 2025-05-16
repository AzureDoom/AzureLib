package mod.azure.azurelib.rewrite.render.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.core.object.Color;
import mod.azure.azurelib.rewrite.render.AzRendererPipeline;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;
import mod.azure.azurelib.rewrite.render.armor.bone.AzArmorBoneContext;

public class AzArmorRendererPipelineContext extends AzRendererPipelineContext<ItemStack> {

    private final AzArmorBoneContext boneContext;

    private HumanoidModel<?> baseModel;

    private Entity currentEntity;

    private EquipmentSlot currentSlot;

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
    public @NotNull RenderType getDefaultRenderType(
        ItemStack animatable,
        ResourceLocation texture,
        @Nullable MultiBufferSource bufferSource,
        float partialTick
    ) {
        return translucent
            ? RenderType.itemEntityTranslucentCull(texture)
            : RenderType.armorCutoutNoCull(texture);
    }

    public void prepare(
        @Nullable Entity entity,
        ItemStack stack,
        @Nullable EquipmentSlot slot,
        @Nullable HumanoidModel<?> baseModel
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

    public HumanoidModel<?> baseModel() {
        return baseModel;
    }

    public AzArmorBoneContext boneContext() {
        return boneContext;
    }

    public Entity currentEntity() {
        return currentEntity;
    }

    public EquipmentSlot currentSlot() {
        return currentSlot;
    }

    public ItemStack currentStack() {
        return currentStack;
    }
}
