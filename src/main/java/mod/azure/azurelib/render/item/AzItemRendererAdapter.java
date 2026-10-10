package mod.azure.azurelib.render.item;

import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

import mod.azure.azurelib.render.vertex.GlStateManager;

/**
 * Bridges Forge 1.7.10's {@link IItemRenderer} to AzureLib's {@link AzItemRenderer}. Registered automatically by
 * {@link AzItemRendererRegistry#register}.
 * <p>
 * 1.7.10 hands each render type a different GL coordinate space, so {@link AzItemRenderer#applyBaseTransform} first
 * maps it to the item space 1.12.2+ use (a 1x1x1 cube, model centered), then the item model json's {@code display}
 * transform for that context is applied, exactly like a {@code builtin/entity} item on modern versions.
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

        GlStateManager.pushMatrix();

        if (inventory) {
            GlStateManager.enableDepth();
            GlStateManager.enableRescaleNormal();
            RenderHelper.enableGUIStandardItemLighting();
        }

        AzItemDisplayTransforms transforms = AzItemDisplayTransforms.forItem(this.item);
        renderer.applyBaseTransform(context, transforms, stack);
        transforms.apply(context);
        GlStateManager.translate(-0.5F, -0.5F, -0.5F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        renderer.render(stack, context);

        if (inventory) {
            // Forge renders custom inventory items with lighting disabled; leave it the way we found it.
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL11.GL_LIGHTING);
        }

        GlStateManager.popMatrix();
    }
}
