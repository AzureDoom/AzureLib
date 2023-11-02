package mod.azure.azurelib.common.internal.client.config.screen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class DialogScreen extends Screen {

	public static final Component TEXT_CONFIRM = Component.translatable("text.azurelib.screen.dialog.confirm");
	public static final Component TEXT_CANCEL = Component.translatable("text.azurelib.screen.dialog.cancel");

	private final Screen background;
	private DialogRespondEvent onCancel;
	private DialogRespondEvent onConfirm;
	protected final Component[] text;
	protected int dialogWidth;
	protected int dialogHeight;
	protected int dialogLeft;
	protected int dialogTop;
	private List<FormattedCharSequence> splitText = new ArrayList<>();

	public DialogScreen(Component title, Component[] text, Screen background) {
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
		this.splitText = Arrays.stream(this.text).map(line -> this.font.split(line, this.dialogWidth - 10)).flatMap(Collection::stream).toList();
	}

	@Override
	protected void init() {
		this.background.init(minecraft, width, height);
		this.setDimensions(140, 100);
		this.addDefaultDialogButtons();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		int backgroundColor = 0xFF << 24;
		this.background.render(graphics, mouseX, mouseY, partialTicks);
		graphics.pose().pushPose();
		graphics.pose().translate(0, 0, 400);
		graphics.fillGradient(this.dialogLeft - 1, this.dialogTop - 1, this.dialogLeft + this.dialogWidth + 1, this.dialogTop + this.dialogHeight + 1, 0xFFFFFFFF, 0xFFFFFFFF);
		graphics.fillGradient(this.dialogLeft, this.dialogTop, this.dialogLeft + this.dialogWidth, this.dialogTop + this.dialogHeight, backgroundColor, backgroundColor);
		this.renderForeground(graphics, mouseX, mouseY, partialTicks);
		super.render(graphics, mouseX, mouseY, partialTicks);
		graphics.pose().popPose();
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

	protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		int headerWidth = this.font.width(this.title);
		graphics.drawString(this.font, this.title, (int) (this.dialogLeft + (this.dialogWidth - headerWidth) / 2.0F), this.dialogTop + 5, 0xFFFFFF, true);
		int line = 0;
		for (FormattedCharSequence textLine : this.splitText) {
			graphics.drawString(this.font, textLine, this.dialogLeft + 5, this.dialogTop + 20 + line * 10, 0xFFFFFF, true);
			++line;
		}
	}

	protected void addDefaultDialogButtons() {
		int useableWidth = this.dialogWidth - 15;
		int componentWidth = useableWidth / 2;
		int cancelX = this.dialogLeft + 5;
		int confirmX = this.dialogLeft + this.dialogWidth - 5 - componentWidth;
		int componentY = this.dialogTop + this.dialogHeight - 25;

		this.addRenderableWidget(Button.builder(TEXT_CANCEL, btn -> cancel()).pos(cancelX, componentY).size(componentWidth, 20).build());
		this.addRenderableWidget(Button.builder(TEXT_CONFIRM, btn -> confirm()).pos(confirmX, componentY).size(componentWidth, 20).build());
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
