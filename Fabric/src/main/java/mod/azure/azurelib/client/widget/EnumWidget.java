package mod.azure.azurelib.client.widget;

import com.mojang.blaze3d.systems.RenderSystem;

import mod.azure.azurelib.config.value.EnumValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class EnumWidget<E extends Enum<E>> extends AbstractWidget {

    private final EnumValue<E> value;

    public EnumWidget(int x, int y, int w, int h, EnumValue<E> value) {
        super(x, y, w, h, CommonComponents.EMPTY);
        this.value = value;
        this.updateText();
    }
    

    @Override
    public void renderWidget(GuiGraphics stack, int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        stack.blitNineSliced(new ResourceLocation("minecraft"), this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, this.getTextureY());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderString(stack, minecraft.font, Mth.ceil(this.alpha * 255.0F) << 24);
    }

    private int getTextureY() {
        int i = 1;
        if (!this.active) {
            i = 0;
        } else if (this.isHoveredOrFocused()) {
            i = 2;
        }
        return 46 + i * 20;
    }

    private void renderString(GuiGraphics stack, Font font, int color) {
        this.renderScrollingString(stack, font, 2, color);
    }

    @Override
    public void onClick(double p_230982_1_, double p_230982_3_) {
        this.nextValue();
        this.updateText();
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
    
    private void nextValue() {
        E e = this.value.get();
        E[] values = e.getDeclaringClass().getEnumConstants();
        int i = e.ordinal();
        int j = (i + 1) % values.length;
        E next = values[j];
        this.value.set(next);
    }

    private void updateText() {
        E e = this.value.get();
        this.setMessage(Component.literal(e.name()));
    }
}
