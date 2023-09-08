package mod.azure.azurelib.client.widget;

import com.mojang.blaze3d.systems.RenderSystem;

import mod.azure.azurelib.config.value.BooleanValue;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class BooleanWidget extends AbstractWidget {

	public static final Component TRUE = Component.translatable("text.azurelib.value.true").withStyle(ChatFormatting.GREEN);
	public static final Component FALSE = Component.translatable("text.azurelib.value.false").withStyle(ChatFormatting.RED);
	private final BooleanValue value;

	public BooleanWidget(int x, int y, int w, int h, BooleanValue value) {
		super(x, y, w, h, CommonComponents.EMPTY);
		this.value = value;
		this.readState();
	}

	@Override
	public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		Minecraft minecraft = Minecraft.getInstance();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		graphics.blitNineSliced(WIDGETS_LOCATION, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, this.getTextureY());
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		this.renderString(graphics, minecraft.font, Mth.ceil(this.alpha * 255.0F) << 24);
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

	private void renderString(GuiGraphics graphics, Font font, int color) {
		this.renderScrollingString(graphics, font, 2, color);
	}

	@Override
	public void onClick(double x, double y) {
		this.setState(!this.value.get());
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
	}

	private void readState() {
		boolean value = this.value.get();
		this.setMessage(value ? TRUE : FALSE);
	}

	private void setState(boolean state) {
		this.value.set(state);
		this.readState();
	}
}
