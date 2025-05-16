package mod.azure.azurelib.config.validate;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

public record ValidationResult(
    NotificationSeverity severity,
    MutableComponent text
) {

    private static final ValidationResult OK = new ValidationResult(
        NotificationSeverity.INFO,
        (MutableComponent) TextComponent.EMPTY
    );

    public static ValidationResult ok() {
        return OK;
    }

    public static ValidationResult warn(MutableComponent text) {
        return new ValidationResult(NotificationSeverity.WARNING, text);
    }

    public static ValidationResult error(MutableComponent text) {
        return new ValidationResult(NotificationSeverity.ERROR, text);
    }

    public boolean isOk() {
        return this.severity.isOkStatus();
    }
}
