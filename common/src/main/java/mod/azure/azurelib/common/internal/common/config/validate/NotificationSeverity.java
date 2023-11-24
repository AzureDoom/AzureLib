package mod.azure.azurelib.common.internal.common.config.validate;

import mod.azure.azurelib.common.internal.common.AzureLib;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;

public enum NotificationSeverity {

    INFO("", ChatFormatting.RESET, 0xF0030319, 0x502493E5, 0x502469E5),
    WARNING("warning", ChatFormatting.GOLD, 0xF0563900, 0x50FFB200, 0x509E6900),
    ERROR("error", ChatFormatting.RED, 0xF0270006, 0x50FF0000, 0x50880000);

    private final ResourceLocation icon;
    private final ChatFormatting extraFormatting;
    public final int background;
    public final int fadeMin;
    public final int fadeMax;

    NotificationSeverity(String iconName, ChatFormatting formatting, int background, int fadeMin, int fadeMax) {
        this.icon = AzureLib.modResource("textures/icons/" + iconName + ".png");
        this.extraFormatting = formatting;
        this.background = background;
        this.fadeMin = fadeMin;
        this.fadeMax = fadeMax;
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    public ChatFormatting getExtraFormatting() {
        return extraFormatting;
    }

    public boolean isOkStatus() {
        return this == INFO;
    }
}
