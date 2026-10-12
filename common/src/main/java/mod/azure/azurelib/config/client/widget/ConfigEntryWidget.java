/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
package mod.azure.azurelib.config.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

import mod.azure.azurelib.config.client.WidgetAdder;
import mod.azure.azurelib.config.client.screen.WidgetPlacerHelper;
import mod.azure.azurelib.config.validate.NotificationSeverity;
import mod.azure.azurelib.config.validate.ValidationResult;
import mod.azure.azurelib.config.value.ConfigValue;

/**
 * One row of a config screen: renders the translated option label on the left and hosts the value widgets placed by a
 * {@link mod.azure.azurelib.config.client.adapter.DisplayAdapter} on the right.
 */
public class ConfigEntryWidget extends ContainerWidget implements WidgetAdder {

    public static final Component EDIT = Component.translatable("text.azurelib.value.edit");

    public static final Component BACK = Component.translatable("text.azurelib.value.back");

    public static final Component REVERT_DEFAULTS = Component.translatable("text.azurelib.value.revert.default");

    public static final Component REVERT_DEFAULTS_DIALOG_TEXT = Component.translatable(
        "text.azurelib.value.revert.default.dialog"
    );

    public static final Component REVERT_CHANGES = Component.translatable("text.azurelib.value.revert.changes");

    public static final Component REVERT_CHANGES_DIALOG_TEXT = Component.translatable(
        "text.azurelib.value.revert.changes.dialog"
    );

    private static final int HOVER_BACKGROUND = 0x44FFFFFF;

    private static final int LABEL_COLOR = 0xFFFFFFFF;

    private static final long DESCRIPTION_DELAY_MS = 750L;

    private final String configId;

    private final List<Component> description;

    private ValidationResult result = ValidationResult.ok();

    private IValidationRenderer renderer;

    private boolean lastHoverState;

    private long hoverTimeStart;

    public ConfigEntryWidget(int x, int y, int w, int h, ConfigValue<?> value, String configId) {
        this(x, y, w, h, Component.translatable("config." + configId + ".option." + value.getId()), value, configId);
    }

    public ConfigEntryWidget(int x, int y, int w, int h, Component label, ConfigValue<?> value, String configId) {
        super(x, y, w, h, label);
        this.configId = configId;
        List<Component> lines = new ArrayList<>();
        for (var line : value.getDescription()) {
            lines.add(Component.literal(line));
        }
        this.description = lines;
    }

    public void setDescriptionRenderer(IValidationRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public Component getComponentName() {
        return this.getMessage();
    }

    @Override
    public void updateWidgetNarration(@NonNull NarrationElementOutput output) {}

    @Override
    public void extractWidgetRenderState(
        @NonNull GuiGraphicsExtractor graphics,
        int mouseX,
        int mouseY,
        float partialTicks
    ) {
        var font = Minecraft.getInstance().font;
        if (this.isHovered) {
            if (!this.lastHoverState) {
                this.hoverTimeStart = System.currentTimeMillis();
            }
            graphics.fill(
                this.getX() - 30,
                this.getY() - 2,
                this.getRight() + 30,
                this.getBottom() + 2,
                HOVER_BACKGROUND
            );
        }

        var entryLeft = WidgetPlacerHelper.getLeft(this.getX(), this.width);
        this.renderScrollingString(
            this.getMessage(),
            entryLeft - 5,
            LABEL_COLOR,
            graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE)
        );

        super.extractWidgetRenderState(graphics, mouseX, mouseY, partialTicks);

        var hasProblem = !this.result.isOk();
        if (hasProblem && this.renderer != null) {
            this.renderer.drawIcon(graphics, this, this.result.severity());
        }
        if ((hasProblem || this.isHovered) && this.renderer != null) {
            var totalHoverTime = System.currentTimeMillis() - this.hoverTimeStart;
            if (hasProblem || totalHoverTime >= DESCRIPTION_DELAY_MS) {
                var messages = hasProblem
                    ? List.of(this.result.text().copy().withStyle(this.result.severity().getExtraFormatting()))
                    : this.description;
                var lines = messages.stream()
                    .flatMap(text -> font.split(text, this.width / 2).stream())
                    .toList();
                if (!lines.isEmpty()) {
                    this.renderer.drawDescription(graphics, this, lines, this.result.severity(), 0xFFFFFFFF);
                }
            }
        }
        this.lastHoverState = this.isHovered;
    }

    public void renderScrollingString(Component text, int right, int color, ActiveTextCollector textCollector) {
        var left = this.getX();
        var coloredText = text.copy().withStyle(style -> style.withColor(color));
        textCollector.acceptScrolling(
            coloredText,
            left,
            left,
            right,
            this.getY(),
            this.getBottom(),
            textCollector.defaultParameters()
        );
    }

    @Override
    public void setValidationResult(ValidationResult result) {
        this.result = result;
    }

    @Override
    public <W extends AbstractWidget> W addConfigWidget(ToWidgetFunction<W> function) {
        var widget = function.asWidget(this.getX(), this.getY(), this.width, this.height, this.configId);
        return this.addRenderableWidget(widget);
    }

    public interface IValidationRenderer {

        void drawIcon(GuiGraphicsExtractor graphics, AbstractWidget widget, NotificationSeverity severity);

        void drawDescription(
            GuiGraphicsExtractor graphics,
            AbstractWidget widget,
            List<FormattedCharSequence> text,
            NotificationSeverity severity,
            int textColor
        );
    }
}
