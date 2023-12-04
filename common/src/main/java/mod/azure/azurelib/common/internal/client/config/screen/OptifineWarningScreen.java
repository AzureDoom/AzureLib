package mod.azure.azurelib.common.internal.client.config.screen;

import mod.azure.azurelib.common.internal.mixins.AccessorWarningScreen;
import mod.azure.azurelib.common.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.multiplayer.WarningScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class OptifineWarningScreen extends WarningScreen {
    public OptifineWarningScreen() {
        super(HEADER, MESSAGE, CHECK_MESSAGE, NARRATED_TEXT);
    }

    @Override
    protected void initButtons(int yOffset) {
        addRenderableWidget(
                Button.builder(OPEN_MODS_FOLDER, buttonWidget -> Util.getPlatform().openFile(Services.PLATFORM.modsDir().toFile()))
                        .bounds(width / 2 - 155, 100 + yOffset, 150, 20)
                        .build()
        );

        addRenderableWidget(
                Button.builder(OPTIFINE_ALTERNATIVES, buttonWidget -> Util.getPlatform().openUri(
                                "https://prismlauncher.org/wiki/getting-started/install-of-alternatives/"
                        ))
                        .bounds(width / 2 - 155 + 160, 100 + yOffset, 150, 20)
                        .build()
        );

        addRenderableWidget(
                Button.builder(QUIT_GAME, buttonWidget -> this.minecraft.stop())
                        .bounds(width / 2 - 75, 130 + yOffset, 150, 20)
                        .build()
        );
    }

    @Override
    protected void init() {
        ((AccessorWarningScreen) this).setMessageText(MultiLineLabel.create(font, MESSAGE, width - 50));
        int yOffset = (((AccessorWarningScreen) this).getMessageText().getLineCount() + 1) * font.lineHeight * 2 - 20;
        initButtons(yOffset);
    }


    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private static final MutableComponent HEADER = Component.translatable("header.azurelib.optifine").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);
    private static final Component MESSAGE = Component.translatable("message.azurelib.optifine");
    private static final Component CHECK_MESSAGE = Component.translatable("multiplayerWarning.check");
    private static final Component NARRATED_TEXT = HEADER.copy().append("\n").append(MESSAGE);

    private static final Component OPEN_MODS_FOLDER = Component.translatable("label.azurelib.open_mods_folder");
    private static final Component OPTIFINE_ALTERNATIVES = Component.translatable("label.azurelib.optifine_alternatives");
    private static final Component QUIT_GAME = Component.translatable("menu.quit");
}
