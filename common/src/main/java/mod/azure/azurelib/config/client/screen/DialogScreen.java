/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
package mod.azure.azurelib.config.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public class DialogScreen extends Screen {

    public static final Component TEXT_CONFIRM = Component.translatable("text.azurelib.screen.dialog.confirm");

    public static final Component TEXT_CANCEL = Component.translatable("text.azurelib.screen.dialog.cancel");

    private final Screen background;

    protected final Component[] text;

    protected int dialogWidth;

    protected int dialogHeight;

    protected int dialogLeft;

    protected int dialogTop;

    private DialogRespondEvent onCancel;

    private DialogRespondEvent onConfirm;

    private List<FormattedCharSequence> splitText = new ArrayList<>();

    public DialogScreen(Component title, Component[] text, Screen background) {
        super(title);
        this.text = text;
        this.background = background;
        this.onCancel = screen -> this.displayPreviousScreen();
        this.onConfirm = screen -> this.displayPreviousScreen();
    }

    public void onCancelled(DialogRespondEvent cancelEvent) {
        this.onCancel = Objects.requireNonNull(cancelEvent);
    }

    public void onConfirmed(DialogRespondEvent confirmEvent) {
        this.onConfirm = Objects.requireNonNull(confirmEvent);
    }

    public void setDimensions(int dialogWidth, int dialogHeight) {
        this.dialogWidth = dialogWidth;
        this.dialogHeight = dialogHeight;
        this.dialogLeft = (this.width - this.dialogWidth) / 2;
        this.dialogTop = (this.height - this.dialogHeight) / 2;
        this.splitText = Arrays.stream(this.text)
            .map(line -> this.font.split(line, this.dialogWidth - 10))
            .flatMap(Collection::stream)
            .toList();
    }

    @Override
    protected void init() {
        this.background.init(this.width, this.height);
        this.setDimensions(140, 100);
        this.addDefaultDialogButtons();
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        var backgroundColor = 0xFF << 24;
        this.background.extractRenderState(graphics, -1, -1, partialTicks);
        graphics.nextStratum();
        graphics.fill(0, 0, this.width, this.height, 0xAA << 24);
        graphics.fill(
            this.dialogLeft - 1,
            this.dialogTop - 1,
            this.dialogLeft + this.dialogWidth + 1,
            this.dialogTop + this.dialogHeight + 1,
            0xFFFFFFFF
        );
        graphics.fill(
            this.dialogLeft,
            this.dialogTop,
            this.dialogLeft + this.dialogWidth,
            this.dialogTop + this.dialogHeight,
            backgroundColor
        );
        this.renderForeground(graphics, mouseX, mouseY, partialTicks);
        this.renderables.forEach(renderable -> renderable.extractRenderState(graphics, mouseX, mouseY, partialTicks));
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {
        if (this.allowKeyboardInteractions()) {
            if (event.isEscape()) {
                this.cancel();
                return true;
            } else if (event.isConfirmation()) {
                this.confirm();
                return true;
            }
        }
        return super.keyPressed(event);
    }

    protected void renderForeground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        var headerWidth = this.font.width(this.title);
        graphics.text(
            this.font,
            this.title,
            this.dialogLeft + (this.dialogWidth - headerWidth) / 2,
            this.dialogTop + 5,
            0xFFFFFFFF
        );
        var line = 0;
        for (var textLine : this.splitText) {
            graphics.text(this.font, textLine, this.dialogLeft + 5, this.dialogTop + 20 + line * 10, 0xFFFFFFFF);
            ++line;
        }
    }

    protected void addDefaultDialogButtons() {
        var useableWidth = this.dialogWidth - 15;
        var componentWidth = useableWidth / 2;
        var cancelX = this.dialogLeft + 5;
        var confirmX = this.dialogLeft + this.dialogWidth - 5 - componentWidth;
        var componentY = this.dialogTop + this.dialogHeight - 25;
        this.addRenderableWidget(
            Button.builder(TEXT_CANCEL, btn -> this.cancel()).pos(cancelX, componentY).size(componentWidth, 20).build()
        );
        this.addRenderableWidget(
            Button.builder(TEXT_CONFIRM, btn -> this.confirm())
                .pos(confirmX, componentY)
                .size(componentWidth, 20)
                .build()
        );
    }

    protected void confirm() {
        this.onConfirm.respond(this);
    }

    protected void cancel() {
        this.onCancel.respond(this);
    }

    public void displayPreviousScreen() {
        this.minecraft.setScreen(this.background);
    }

    protected boolean allowKeyboardInteractions() {
        return true;
    }

    @FunctionalInterface
    public interface DialogRespondEvent {

        void respond(DialogScreen screen);
    }
}
