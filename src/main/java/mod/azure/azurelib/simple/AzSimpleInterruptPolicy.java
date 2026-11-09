package mod.azure.azurelib.simple;

public enum AzSimpleInterruptPolicy {

    ALWAYS,
    SAME_OR_HIGHER_PRIORITY,
    HIGHER_PRIORITY,
    NEVER;

    public boolean allows(int currentPriority, int incomingPriority) {
        switch (this) {
            case ALWAYS:
                return true;
            case SAME_OR_HIGHER_PRIORITY:
                return incomingPriority >= currentPriority;
            case HIGHER_PRIORITY:
                return incomingPriority > currentPriority;
            default:
                return false;
        }
    }
}
