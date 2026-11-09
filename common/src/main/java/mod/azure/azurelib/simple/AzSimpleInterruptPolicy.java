package mod.azure.azurelib.simple;

public enum AzSimpleInterruptPolicy {

    ALWAYS,
    SAME_OR_HIGHER_PRIORITY,
    HIGHER_PRIORITY,
    NEVER;

    public boolean allows(int currentPriority, int incomingPriority) {
        return switch (this) {
            case ALWAYS -> true;
            case SAME_OR_HIGHER_PRIORITY -> incomingPriority >= currentPriority;
            case HIGHER_PRIORITY -> incomingPriority > currentPriority;
            case NEVER -> false;
        };
    }
}
