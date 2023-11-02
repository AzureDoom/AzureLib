package mod.azure.azurelib.common.internal.client.config.screen;

import static mod.azure.azurelib.common.internal.client.config.screen.AbstractConfigScreen.FOOTER_HEIGHT;
import static mod.azure.azurelib.common.internal.client.config.screen.AbstractConfigScreen.HEADER_HEIGHT;

import java.util.List;

import mod.azure.azurelib.common.internal.client.config.DisplayAdapter;
import mod.azure.azurelib.common.internal.client.config.widget.ConfigEntryWidget;
import mod.azure.azurelib.common.internal.common.config.ConfigHolder;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigGroupScreen extends Screen {

	protected final Screen last;
	protected final String groupId;
	protected final List<ConfigHolder<?>> configHolders;
	protected int index;
	protected int pageSize;

	public ConfigGroupScreen(Screen last, String groupId, List<ConfigHolder<?>> configHolders) {
		super(Component.translatable("text.azurelib.screen.select_config"));
		this.last = last;
		this.groupId = groupId;
		this.configHolders = configHolders;
	}

	@Override
	protected void init() {
		final int viewportMin = HEADER_HEIGHT;
		final int viewportHeight = this.height - viewportMin - FOOTER_HEIGHT;
		this.pageSize = (viewportHeight - 20) / 25;
		this.correctScrollingIndex(this.configHolders.size());
		int errorOffset = (viewportHeight - 20) - (this.pageSize * 25 - 5);
		int offset = 0;
		int posX = 30;
		int componentWidth = this.width - 2 * posX;
		for (int i = this.index; i < this.index + this.pageSize; i++) {
			int j = i - this.index;
			if (i >= configHolders.size())
				break;
			int correct = errorOffset / (this.pageSize - j);
			errorOffset -= correct;
			offset += correct;
			ConfigHolder<?> value = configHolders.get(i);
			int y = viewportMin + 10 + j * 25 + offset;
			String configId = value.getConfigId();
			this.addRenderableWidget(new LeftAlignedLabel(posX, y, componentWidth, 20, Component.translatable("config.screen." + configId), this.font));
			this.addRenderableWidget(Button.builder(ConfigEntryWidget.EDIT, btn -> {
				ConfigScreen screen = new ConfigScreen(configId, configId, value.getValueMap(), this);
				minecraft.setScreen(screen);
			}).pos(DisplayAdapter.getValueX(posX, componentWidth), y).size(DisplayAdapter.getValueWidth(componentWidth), 20).build());
		}
		initFooter();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(graphics, mouseY, mouseY, partialTicks);
		// HEADER
		int titleWidth = this.font.width(this.title);
		graphics.drawString(this.font, this.title, (int) ((this.width - titleWidth) / 2.0F), (int) ((HEADER_HEIGHT - this.font.lineHeight) / 2.0F), 0xFFFFFF, true);
		graphics.fill(0, HEADER_HEIGHT, width, height - FOOTER_HEIGHT, 0x99 << 24);
		AbstractConfigScreen.renderScrollbar(graphics, width - 5, HEADER_HEIGHT, 5, height - FOOTER_HEIGHT - HEADER_HEIGHT, index, configHolders.size(), pageSize);
		super.render(graphics, mouseX, mouseY, partialTicks);
	}

	protected void initFooter() {
		int centerY = this.height - FOOTER_HEIGHT + (FOOTER_HEIGHT - 20) / 2;
		addRenderableWidget(Button.builder(ConfigEntryWidget.BACK, btn -> minecraft.setScreen(last)).pos(20, centerY).size(50, 20).build());
	}

	protected void correctScrollingIndex(int count) {
		if (index + pageSize > count) {
			index = Math.max(count - pageSize, 0);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount, double g) {
		int scale = (int) -amount;
		int next = this.index + scale;
		if (next >= 0 && next + this.pageSize <= this.configHolders.size()) {
			this.index = next;
			this.init(minecraft, width, height);
			return true;
		}
		return false;
	}

	protected static final class LeftAlignedLabel extends AbstractWidget {

		private final Font font;

		public LeftAlignedLabel(int x, int y, int width, int height, Component label, Font font) {
			super(x, y, width, height, label);
			this.font = font;
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
			graphics.drawString(font, this.getMessage(), this.getX(), this.getY() + (this.height - this.font.lineHeight) / 2, 0xAAAAAA);
		}

		@Override
		protected boolean isValidClickButton(int p_230987_1_) {
			return false;
		}
	}
}
