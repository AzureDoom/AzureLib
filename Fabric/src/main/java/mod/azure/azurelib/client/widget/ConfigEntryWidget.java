package mod.azure.azurelib.client.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import mod.azure.azurelib.client.WidgetAdder;
import mod.azure.azurelib.config.validate.NotificationSeverity;
import mod.azure.azurelib.config.validate.ValidationResult;
import mod.azure.azurelib.config.value.ConfigValue;

public class ConfigEntryWidget extends ContainerWidget implements WidgetAdder {

    public static final Component EDIT = new TranslatableComponent("text.azurelib.value.edit");

    public static final Component BACK = new TranslatableComponent("text.azurelib.value.back");

    public static final Component REVERT_DEFAULTS = new TranslatableComponent("text.azurelib.value.revert.default");

    public static final Component REVERT_DEFAULTS_DIALOG_TEXT = new TranslatableComponent(
        "text.azurelib.value.revert.default.dialog"
    );

    public static final Component REVERT_CHANGES = new TranslatableComponent("text.azurelib.value.revert.changes");

    public static final Component REVERT_CHANGES_DIALOG_TEXT = new TranslatableComponent(
        "text.azurelib.value.revert.changes.dialog"
    );

    private final String configId;

    private final List<Component> description;

    private ValidationResult result = ValidationResult.ok();

    private IDescriptionRenderer renderer;

    private boolean lastHoverState;

    private long hoverTimeStart;

    public ConfigEntryWidget(int x, int y, int w, int h, ConfigValue<?> value, String configId) {
        super(x, y, w, h, new TranslatableComponent("config." + configId + ".option." + value.getId()));
        this.configId = configId;
        this.description = Arrays.stream(value.getDescription())
            .map(text -> new TextComponent(text).withStyle(ChatFormatting.GRAY))
            .collect(Collectors.toList());
    }

    public void setDescriptionRenderer(IDescriptionRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public Component getComponentName() {
        return this.getMessage();
    }

    @Override
    public void renderButton(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        Font font = Minecraft.getInstance().font;
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
                MutableComponent textComponent = this.result.getText().withStyle(severity.getExtraFormatting());
                List<Component> desc = isError ? Collections.singletonList(textComponent) : this.description;
                List<FormattedCharSequence> split = desc.stream()
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
    public <W extends AbstractWidget> W addConfigWidget(ToWidgetFunction<W> function) {
        W widget = function.asWidget(this.x, this.y, this.width, this.height, this.configId);
        return this.addRenderableWidget(widget);
    }

    @FunctionalInterface
    public interface IDescriptionRenderer {

        void drawDescription(
            PoseStack stack,
            AbstractWidget widget,
            NotificationSeverity severity,
            List<FormattedCharSequence> text
        );
    }
}
