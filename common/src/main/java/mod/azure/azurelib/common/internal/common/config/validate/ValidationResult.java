package mod.azure.azurelib.common.internal.common.config.validate;

import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;

public record ValidationResult(NotificationSeverity severity, MutableComponent text) {

    private static final ValidationResult OK = new ValidationResult(NotificationSeverity.INFO, (MutableComponent) CommonComponents.EMPTY);

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
