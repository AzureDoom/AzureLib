package mod.azure.azurelib.core.molang.compile;

/**
 * Thrown by {@link MolangCompiler} for an expression it can't parse. Carries the position in the source it gave up at.
 */
public final class MolangSyntaxException extends RuntimeException {

    private final int position;

    private final boolean retryable;

    public MolangSyntaxException(String message, int position) {
        this(message, position, true);
    }

    /**
     * @param retryable whether the older parser may be tried instead; {@code false} for expressions that parse fine but
     *                  are rejected on purpose (assigning to a query, say), which the older parser would accept
     */
    public MolangSyntaxException(String message, int position, boolean retryable) {
        super(message + " (at character " + position + ")");
        this.position = position;
        this.retryable = retryable;
    }

    public int position() {
        return position;
    }

    /**
     * @return whether the expression may still be handed to the older parser
     */
    public boolean isRetryable() {
        return retryable;
    }
}
