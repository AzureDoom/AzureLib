package mod.azure.azurelib.client.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import mod.azure.azurelib.client.WidgetAdder;
import mod.azure.azurelib.config.validate.NotificationSeverity;
import mod.azure.azurelib.config.validate.ValidationResult;
import mod.azure.azurelib.config.value.ConfigValue;

public class ConfigEntryWidget extends ContainerWidget implements WidgetAdder {

    public static final ITextComponent EDIT = new TranslationTextComponent("text.azurelib.value.edit");

    public static final ITextComponent BACK = new TranslationTextComponent("text.azurelib.value.back");

    public static final ITextComponent REVERT_DEFAULTS = new TranslationTextComponent(
        "text.azurelib.value.revert.default"
    );

    public static final ITextComponent REVERT_DEFAULTS_DIALOG_TEXT = new TranslationTextComponent(
        "text.azurelib.value.revert.default.dialog"
    );

    public static final ITextComponent REVERT_CHANGES = new TranslationTextComponent(
        "text.azurelib.value.revert.changes"
    );

    public static final ITextComponent REVERT_CHANGES_DIALOG_TEXT = new TranslationTextComponent(
        "text.azurelib.value.revert.changes.dialog"
    );

    private final String configId;

    private final List<ITextComponent> description;

    private ValidationResult result = ValidationResult.ok();

    private IDescriptionRenderer renderer;

    private boolean lastHoverState;

    private long hoverTimeStart;

    public ConfigEntryWidget(int x, int y, int w, int h, ConfigValue<?> value, String configId) {
        super(x, y, w, h, new TranslationTextComponent("config." + configId + ".option." + value.getId()));
        this.configId = configId;
        this.description = Arrays.stream(value.getDescription())
            .map(
                text -> new StringTextComponent(text).withStyle(
                    TextFormatting.GRAY
                )
            )
            .collect(Collectors.toList());
    }

    public void setDescriptionRenderer(IDescriptionRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public ITextComponent getComponentName() {
        return this.getMessage();
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        FontRenderer font = Minecraft.getInstance().font;
        if (!lastHoverState && isHovered) {
            hoverTimeStart = System.currentTimeMillis();
        }
        boolean isError = !this.result.isOk();
        font.draw(stack, this.getMessage(), this.x, this.y + (this.height - font.lineHeight) / 2.0F, 0xAAAAAA);
        super.renderButton(stack, mouseX, mouseY, partialTicks);
        if ((isError || isHovered) && renderer != null) {
            long totalHoverTime = System.currentTimeMillis() - hoverTimeStart;
            if (isError || totalHoverTime >= 750L) {
                NotificationSeverity severity = this.result.getSeverity();
                IFormattableTextComponent textComponent = this.result.getText()
                    .withStyle(severity.getExtraFormatting());
                List<ITextComponent> desc = isError ? Collections.singletonList(textComponent) : this.description;
                List<IReorderingProcessor> split = desc.stream()
                    .flatMap(text -> font.split(text, this.width / 2).stream())
                    .collect(Collectors.toList());
                renderer.drawDescription(stack, this, severity, split);
            }
        }
        this.lastHoverState = isHovered;
    }

    @Override
    public void setValidationResult(ValidationResult result) {
        this.result = result;
    }

    @Override
    public <W extends Widget> W addConfigWidget(ToWidgetFunction<W> function) {
        W widget = function.asWidget(this.x, this.y, this.width, this.height, this.configId);
        return this.addWidget(widget);
    }

    @FunctionalInterface
    public interface IDescriptionRenderer {

        void drawDescription(
            MatrixStack stack,
            Widget widget,
            NotificationSeverity severity,
            List<IReorderingProcessor> text
        );
    }
}
