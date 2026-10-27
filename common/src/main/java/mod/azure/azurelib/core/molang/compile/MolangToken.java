package mod.azure.azurelib.core.molang.compile;

/**
 * One lexical token of a Molang expression.
 *
 * @param type     what kind of token this is
 * @param text     the source text: the name, operator or string contents
 * @param number   the value, for {@link Type#NUMBER}
 * @param position offset in the source, for error messages
 */
record MolangToken(
    Type type,
    String text,
    double number,
    int position
) {

    enum Type {
        NUMBER,
        IDENTIFIER,
        STRING,
        OPERATOR,
        END
    }

    boolean is(String operator) {
        return type == Type.OPERATOR && text.equals(operator);
    }

    boolean isIdentifier(String name) {
        return type == Type.IDENTIFIER && text.equals(name);
    }

    @Override
    public String toString() {
        return switch (type) {
            case NUMBER -> Double.toString(number);
            case STRING -> "'" + text + "'";
            case END -> "end of expression";
            default -> "'" + text + "'";
        };
    }
}
