package mod.azure.azurelib.render.item;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

import mod.azure.azurelib.animation.molang.AzMolangQueryContext;
import mod.azure.azurelib.render.AzLighting;
import mod.azure.azurelib.render.vertex.GlStateManager;

public class AzItemRendererAdapter {

    public enum ItemRenderType {
        ENTITY,
        EQUIPPED,
        EQUIPPED_FIRST_PERSON,
        INVENTORY
    }

    private final Item item;

    public AzItemRendererAdapter(Item item) {
        this.item = item;
    }

    public boolean handles(ItemRenderType type) {
        AzItemRenderer renderer = AzItemRendererRegistry.getOrNull(this.item);
        return renderer != null && renderer.config().rendersInContext(AzItemDisplayContext.from(type));
    }

    public void renderItem(ItemRenderType type, ItemStack stack) {
        renderItem(type, stack, null);
    }

    /**
     * Renders {@code stack} for {@code type}.
     *
     * @param holder the entity holding the item, for {@code EQUIPPED} and {@code EQUIPPED_FIRST_PERSON}; recorded on
     *               {@link AzMolangQueryContext} for the duration of the render so item animations answer entity
     *               queries for the holder, like Bedrock attachables. {@code null} where there is none.
     */
    public void renderItem(ItemRenderType type, ItemStack stack, @Nullable MobEntity holder) {
        AzItemRenderer renderer = AzItemRendererRegistry.getOrNull(this.item);

        if (renderer == null || stack == null) {
            return;
        }

        AzItemDisplayContext context = AzItemDisplayContext.from(type);
        boolean inventory = type == ItemRenderType.INVENTORY;
        AzMolangQueryContext.INSTANCE.pushItemHolder(holder);

        GlStateManager.pushMatrix();

        if (inventory) {
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LIGHTING_BIT);
            GlStateManager.enableDepth();
            GlStateManager.enableRescaleNormal();
            AzLighting.enableGUIStandardItemLighting();
        } else if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
            GlStateManager.translate(-0.5F, -0.5F, -0.5F);
        }

        try {
            AzItemDisplayTransforms transforms = AzItemDisplayTransforms.forItem(this.item);
            renderer.applyBaseTransform(context, transforms, stack);
            transforms.apply(context);
            GlStateManager.translate(-0.5F, -0.5F, -0.5F);
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
     * Resets the depth buffer over a GUI slot after AzureLib drew a 3D model in it.
     * <p>
     * Vanilla GUI items are flat at the slot's depth, so tooltips and dragged items drawn later at the same depth cover
     * them. An AzureLib model has real depth and sticks out towards the viewer, so without this it would cover the
     * tooltip. Drawing an invisible quad at the far plane with {@code GL_ALWAYS} leaves the color untouched and pushes
     * the slot's depth to the back, so anything drawn afterward lands on top, as with vanilla items.
     *
     * @param x the slot's left edge in GUI pixels
     * @param y the slot's top edge in GUI pixels
     */
    public static void resetGuiDepth(int x, int y) {
        boolean texture = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        int depthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        GL11.glColorMask(false, false, false, false);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(true);
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        float z = -999.0F;
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(x - 8, y - 8, z);
        GL11.glVertex3f(x - 8, y + 24, z);
        GL11.glVertex3f(x + 24, y + 24, z);
        GL11.glVertex3f(x + 24, y - 8, z);
        GL11.glEnd();
        GL11.glDepthFunc(depthFunc);
        if (texture)
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColorMask(true, true, true, true);
    }
}
