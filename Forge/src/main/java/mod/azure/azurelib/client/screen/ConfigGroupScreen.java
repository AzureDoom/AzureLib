package mod.azure.azurelib.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

import mod.azure.azurelib.client.DisplayAdapter;
import mod.azure.azurelib.client.widget.ConfigEntryWidget;
import mod.azure.azurelib.config.ConfigHolder;

import static mod.azure.azurelib.client.screen.AbstractConfigScreen.FOOTER_HEIGHT;
import static mod.azure.azurelib.client.screen.AbstractConfigScreen.HEADER_HEIGHT;

public class ConfigGroupScreen extends Screen {

    protected final Screen last;

    protected final String groupId;

    protected final List<ConfigHolder<?>> configHolders;

    protected int index;

    protected int pageSize;

    public ConfigGroupScreen(Screen last, String groupId, List<ConfigHolder<?>> configHolders) {
        super(new TranslationTextComponent("text.azurelib.screen.select_config"));
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
            this.addButton(
                new LeftAlignedLabel(
                    posX,
                    y,
                    componentWidth,
                    20,
                    new TranslationTextComponent("config.screen." + configId),
                    this.font
                )
            );
            this.addButton(
                new Button(
                    DisplayAdapter.getValueX(posX, componentWidth),
                    y,
                    DisplayAdapter.getValueWidth(componentWidth),
                    20,
                    ConfigEntryWidget.EDIT,
                    btn -> {
                        ConfigScreen screen = new ConfigScreen(configId, configId, value.getValueMap(), this);
                        minecraft.setScreen(screen);
                    }
                )
            );
        }
        initFooter();
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(stack);
        // HEADER
        int titleWidth = this.font.width(this.title);
        font.draw(
            stack,
            this.title,
            (this.width - titleWidth) / 2.0F,
            (HEADER_HEIGHT - this.font.lineHeight) / 2.0F,
            0xFFFFFF
        );
        fill(stack, 0, HEADER_HEIGHT, width, height - FOOTER_HEIGHT, 0x99 << 24);
        AbstractConfigScreen.renderScrollbar(
            stack,
            width - 5,
            HEADER_HEIGHT,
            5,
            height - FOOTER_HEIGHT - HEADER_HEIGHT,
            index,
            configHolders.size(),
            pageSize
        );
        super.render(stack, mouseX, mouseY, partialTicks);
    }

    protected void initFooter() {
        int centerY = this.height - FOOTER_HEIGHT + (FOOTER_HEIGHT - 20) / 2;
        addButton(new Button(20, centerY, 50, 20, ConfigEntryWidget.BACK, btn -> minecraft.setScreen(this.last)));
    }

    protected void correctScrollingIndex(int count) {
        if (index + pageSize > count) {
            index = Math.max(count - pageSize, 0);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int scale = (int) -amount;
        int next = this.index + scale;
        if (next >= 0 && next + this.pageSize <= this.configHolders.size()) {
            this.index = next;
            this.init(minecraft, width, height);
            return true;
        }
        return false;
    }

    protected static final class LeftAlignedLabel extends Widget {

        private final FontRenderer font;

        public LeftAlignedLabel(int x, int y, int width, int height, ITextComponent label, FontRenderer font) {
            super(x, y, width, height, label);
            this.font = font;
        }

        @Override
        public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
            this.font.draw(
                stack,
                this.getMessage(),
                this.x,
                this.y + (this.height - this.font.lineHeight) / 2.0F,
                0xAAAAAA
            );
        }

        @Override
        protected boolean isValidClickButton(int p_230987_1_) {
            return false;
        }
    }
}
