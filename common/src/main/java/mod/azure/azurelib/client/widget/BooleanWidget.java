package mod.azure.azurelib.client.widget;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import mod.azure.azurelib.config.value.BooleanValue;

public class BooleanWidget extends AbstractWidget {

    public static final Component TRUE = Component.translatable("text.azurelib.value.true")
        .withStyle(ChatFormatting.GREEN);

    public static final Component FALSE = Component.translatable("text.azurelib.value.false")
        .withStyle(ChatFormatting.RED);

    private final BooleanValue value;

    public BooleanWidget(int x, int y, int w, int h, BooleanValue value) {
        super(x, y, w, h, CommonComponents.EMPTY);
        this.value = value;
        this.readState();
    }

    @Override
    public void onClick(double x, double y) {
        this.setState(!this.value.get());
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {}

    private void readState() {
        boolean value = this.value.get();
        this.setMessage(value ? TRUE : FALSE);
    }

    private void setState(boolean state) {
        this.value.set(state);
        this.readState();
    }
}
