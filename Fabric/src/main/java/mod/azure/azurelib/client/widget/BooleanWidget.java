package mod.azure.azurelib.client.widget;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import mod.azure.azurelib.config.value.BooleanValue;

public class BooleanWidget extends AbstractWidget {

    public static final Component TRUE = new TranslatableComponent("text.azurelib.value.true").withStyle(
        ChatFormatting.GREEN
    );

    public static final Component FALSE = new TranslatableComponent("text.azurelib.value.false").withStyle(
        ChatFormatting.RED
    );

    private final BooleanValue value;

    public BooleanWidget(int x, int y, int w, int h, BooleanValue value) {
        super(x, y, w, h, TextComponent.EMPTY);
        this.value = value;
        this.readState();
    }

    @Override
    public void onClick(double x, double y) {
        this.setState(!this.value.get());
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
