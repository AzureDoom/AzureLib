package mod.azure.azurelib.render.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import mod.azure.azurelib.core.object.Color;
import mod.azure.azurelib.render.AzBufferSource;
import mod.azure.azurelib.render.AzRendererPipeline;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.armor.bone.AzArmorBoneContext;

public class AzArmorRendererPipelineContext extends AzRendererPipelineContext<UUID, ItemStack> {

    private final AzArmorBoneContext boneContext;

    private @Nullable HumanoidModel<?> baseModel;

    private @Nullable HumanoidRenderState renderState;

    private @Nullable EquipmentSlot currentSlot;

    private ItemStack currentStack = ItemStack.EMPTY;

    private boolean translucent;

    private boolean setupBaseModel;

    @Nullable
    private ModelPart modelPartOverride;

    public AzArmorRendererPipelineContext(AzRendererPipeline<UUID, ItemStack> rendererPipeline) {
        super(rendererPipeline);
        this.boneContext = new AzArmorBoneContext();
    }

    @Override
    public RenderType getDefaultRenderType(
        ItemStack animatable,
        Identifier texture,
        @Nullable AzBufferSource bufferSource,
        float partialTick,
        RenderType defaultRenderType,
        float alpha
    ) {
        return translucent ? RenderTypes.entityTranslucent(texture) : defaultRenderType;
    }

    public void prepare(
        Entity entity,
        HumanoidRenderState renderState,
        ItemStack stack,
        EquipmentSlot slot,
        HumanoidModel<?> baseModel,
        boolean setupBaseModel,
        @Nullable ModelPart modelPartOverride
    ) {
        this.baseModel = baseModel;
        this.renderState = renderState;
        this.currentEntity = entity;
        this.currentStack = stack;
        this.animatable = stack;
        this.currentSlot = slot;
        this.setupBaseModel = setupBaseModel;
        this.modelPartOverride = modelPartOverride;
        this.translucent = false;
    }

    public void setTranslucent(boolean translucent) {
        this.translucent = translucent;
    }

    @Override
    public Color getRenderColor(ItemStack animatable, float partialTick, int packedLight) {
        return Color.WHITE;
    }

    public HumanoidModel<?> baseModel() {
        if (baseModel == null) {
            throw new IllegalStateException("Armor renderer context has not been prepared with a base model");
        }

        return baseModel;
    }

    public HumanoidRenderState renderState() {
        if (renderState == null) {
            throw new IllegalStateException("Armor renderer context has not been prepared with a render state");
        }

        return renderState;
    }

    public AzArmorBoneContext boneContext() {
        return boneContext;
    }

    @Override
    public @Nullable Entity currentEntity() {
        return currentEntity;
    }

    public EquipmentSlot currentSlot() {
        if (currentSlot == null) {
            throw new IllegalStateException("Armor renderer context has not been prepared with an equipment slot");
        }

        return currentSlot;
    }

    public ItemStack currentStack() {
        return currentStack;
    }

    public boolean setupBaseModel() {
        return setupBaseModel;
    }

    public @Nullable ModelPart modelPartOverride() {
        return modelPartOverride;
    }
}
