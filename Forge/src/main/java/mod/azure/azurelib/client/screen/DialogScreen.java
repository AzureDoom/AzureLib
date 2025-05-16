package mod.azure.azurelib.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.stream.Collectors;

public class DialogScreen extends Screen {

    public static final ITextComponent TEXT_CONFIRM = new TranslationTextComponent(
        "text.azurelib.screen.dialog.confirm"
    );

    public static final ITextComponent TEXT_CANCEL = new TranslationTextComponent("text.azurelib.screen.dialog.cancel");

    private final Screen background;

    private DialogRespondEvent onCancel;

    private DialogRespondEvent onConfirm;

    protected final ITextComponent[] text;

    protected int dialogWidth;

    protected int dialogHeight;

    protected int dialogLeft;

    protected int dialogTop;

    private List<IReorderingProcessor> splitText = new ArrayList<>();

    public DialogScreen(ITextComponent title, ITextComponent[] text, Screen background) {
        super(title);
        this.text = text;
        this.background = background;
        this.onCancel = this::displayPreviousScreen;
        this.onConfirm = this::displayPreviousScreen;
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
            .collect(Collectors.toList());
    }

    @Override
    protected void init() {
        this.background.init(minecraft, width, height);
        this.setDimensions(140, 100);
        this.addDefaultDialogButtons();
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        int backgroundColor = 0xFF << 24;
        this.background.render(stack, mouseX, mouseY, partialTicks);
        this.fillGradient(
            stack,
            this.dialogLeft - 1,
            this.dialogTop - 1,
            this.dialogLeft + this.dialogWidth + 1,
            this.dialogTop + this.dialogHeight + 1,
            0xFFFFFFFF,
            0xFFFFFFFF
        );
        this.fillGradient(
            stack,
            this.dialogLeft,
            this.dialogTop,
            this.dialogLeft + this.dialogWidth,
            this.dialogTop + this.dialogHeight,
            backgroundColor,
            backgroundColor
        );
        this.renderForeground(stack, mouseX, mouseY, partialTicks);
        super.render(stack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.allowKeyboardInteractions()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.cancel();
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                this.confirm();
                return true;
            }
            return false;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    protected void renderForeground(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        int headerWidth = this.font.width(this.title);
        this.font.draw(
            stack,
            this.title,
            this.dialogLeft + (this.dialogWidth - headerWidth) / 2.0F,
            this.dialogTop + 5,
            0xFFFFFF
        );
        int line = 0;
        for (IReorderingProcessor textLine : this.splitText) {
            this.font.draw(stack, textLine, this.dialogLeft + 5, this.dialogTop + 20 + line * 10, 0xFFFFFF);
            ++line;
        }
    }

    protected void addDefaultDialogButtons() {
        int useableWidth = this.dialogWidth - 15;
        int componentWidth = useableWidth / 2;
        int cancelX = this.dialogLeft + 5;
        int confirmX = this.dialogLeft + this.dialogWidth - 5 - componentWidth;
        int componentY = this.dialogTop + this.dialogHeight - 25;

        this.addButton(new Button(cancelX, componentY, componentWidth, 20, TEXT_CANCEL, btn -> cancel()));
        this.addButton(new Button(confirmX, componentY, componentWidth, 20, TEXT_CONFIRM, btn -> confirm()));
    }

    protected void confirm() {
        this.onConfirm.respond(this);
    }

    protected void cancel() {
        this.onCancel.respond(this);
    }

    public void displayPreviousScreen(DialogScreen screen) {
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
