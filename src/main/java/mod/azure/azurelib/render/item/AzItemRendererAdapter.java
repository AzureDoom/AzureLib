package mod.azure.azurelib.render.item;

import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

import mod.azure.azurelib.animation.molang.AzMolangQueryContext;
import mod.azure.azurelib.render.vertex.GlStateManager;

/**
 * Bridges Forge 1.7.10's {@link IItemRenderer} to AzureLib's {@link AzItemRenderer}. Registered automatically by
 * {@link AzItemRendererRegistry#register}.
 * <p>
 * 1.7.10 hands each render type a different GL coordinate space, so {@link AzItemRenderer#applyBaseTransform} first
 * maps it to the item space 1.12.2+ use (a 1x1x1 cube, model centered), then the item model json's {@code display}
 * transform for that context is applied, exactly like a {@code builtin/entity} item on modern versions.
 * <p>
 * For held items Forge also passes the holding entity, which is recorded on {@link AzMolangQueryContext} for the
 * duration of the render so item animations answer entity queries for the holder, like Bedrock attachables. This is
 * 1.7.10's counterpart to the {@code ItemRenderer#renderStatic} mixin on 1.21.1; no mixin is needed.
 * </p>
 */
public class AzItemRendererAdapter implements IItemRenderer {

    private final Item item;

    public AzItemRendererAdapter(Item item) {
        this.item = item;
    }

    @Override
    public boolean handleRenderType(ItemStack stack, ItemRenderType type) {
        return type != ItemRenderType.FIRST_PERSON_MAP && AzItemRendererRegistry.getOrNull(this.item) != null;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack stack, ItemRendererHelper helper) {
        switch (type) {
            case ENTITY:
                // Let Forge bob and spin dropped items.
                return helper == ItemRendererHelper.ENTITY_BOBBING || helper == ItemRendererHelper.ENTITY_ROTATION;
            case EQUIPPED:
            case EQUIPPED_FIRST_PERSON:
                // Forge then only adds a (-0.5, -0.5, -0.5) offset to vanilla's hand transforms, which
                // AzItemRenderer#applyBaseTransform knows how to undo exactly.
                return helper == ItemRendererHelper.EQUIPPED_BLOCK;
            default:
                return false;
        }
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
        AzItemRenderer renderer = AzItemRendererRegistry.getOrNull(this.item);

        if (renderer == null || stack == null) {
            return;
        }

        AzItemDisplayContext context = AzItemDisplayContext.from(type);
        boolean inventory = type == ItemRenderType.INVENTORY;
        AzMolangQueryContext.INSTANCE.pushItemHolder(holderOf(type, data));

        GlStateManager.pushMatrix();

        if (inventory) {
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LIGHTING_BIT);
            GlStateManager.enableDepth();
            GlStateManager.enableRescaleNormal();
            RenderHelper.enableGUIStandardItemLighting();
        }

        try {
            AzItemDisplayTransforms transforms = AzItemDisplayTransforms.forItem(this.item);
            renderer.applyBaseTransform(context, transforms, stack);
            transforms.apply(context);
            GlStateManager.translate(-0.5F, -0.5F, -0.5F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableRescaleNormal();
            renderer.render(stack, context);
        } finally {
            if (inventory) {
                GL11.glPopAttrib();
            }
            GlStateManager.popMatrix();
            AzMolangQueryContext.INSTANCE.popItemHolder();
        }
    }

    /**
     * Forge passes {@code (RenderBlocks, EntityLivingBase)} as the data of {@code EQUIPPED} and
     * {@code EQUIPPED_FIRST_PERSON} renders. Other types have no holder ({@code ENTITY} passes the dropped
     * {@code EntityItem}, which isn't one).
     *
     * @return the entity holding the item, or {@code null}
     */
    private static EntityLivingBase holderOf(ItemRenderType type, Object[] data) {
        if (type != ItemRenderType.EQUIPPED && type != ItemRenderType.EQUIPPED_FIRST_PERSON) {
            return null;
        }

        return data.length > 1 && data[1] instanceof EntityLivingBase ? (EntityLivingBase) data[1] : null;
    }
}
