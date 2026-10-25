package mod.azure.azurelib.render.armor;

import net.minecraft.client.render.model.ModelPart;
import net.minecraft.client.render.model.entity.HumanoidModel;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

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
import mod.azure.azurelib.util.ResourceLocation;

public class AzArmorRendererPipelineContext extends AzRendererPipelineContext<UUID, ItemStack> {

    private final AzArmorBoneContext boneContext;

    private HumanoidModel baseModel;

    private AzEquipmentSlot currentSlot;

    private ItemStack currentStack;

    private boolean translucent = false;

    @Nullable
    private ModelPart visiblePartOverride;

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
        @Nullable HumanoidModel baseModel
    ) {
        this.baseModel = baseModel;
        this.currentEntity = entity;
        this.currentStack = stack;
        this.animatable = stack;
        this.currentSlot = slot;
        this.visiblePartOverride = null;
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
        // Beta leather armor can't be dyed, so there is never a tint.
        return Color.WHITE;
    }

    public HumanoidModel baseModel() {
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

    public void setVisiblePartOverride(@Nullable ModelPart part) {
        this.visiblePartOverride = part;
    }

    public @Nullable ModelPart visiblePartOverride() {
        return visiblePartOverride;
    }
}
