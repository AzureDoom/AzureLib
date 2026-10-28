package mod.azure.azurelib.core.molang.compile;

/**
 * One lexical token of a Molang expression.
 */
final class MolangToken {

    private final Type type;

    private final String text;

    private final double number;

    private final int position;

    MolangToken(Type type, String text, double number, int position) {
        this.type = type;
        this.text = text;
        this.number = number;
        this.position = position;
    }

    enum Type {
        NUMBER,
        IDENTIFIER,
        STRING,
        OPERATOR,
        END
    }

    Type type() {
        return type;
    }

    String text() {
        return text;
    }

    double number() {
        return number;
    }

    int position() {
        return position;
    }

    boolean is(String operator) {
        return type == Type.OPERATOR && text.equals(operator);
    }

    boolean isIdentifier(String name) {
        return type == Type.IDENTIFIER && text.equals(name);
    }

    @Override
    public String toString() {
        switch (type) {
            case NUMBER:
                return Double.toString(number);

            case STRING:
                return "'" + text + "'";

            case END:
                return "end of expression";

            default:
                return "'" + text + "'";
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof MolangToken)) {
            return false;
        }

        MolangToken other = (MolangToken) obj;

        return Double.compare(number, other.number) == 0
            && position == other.position
            && java.util.Objects.equals(type, other.type)
            && java.util.Objects.equals(text, other.text);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(type, text, number, position);
    }
}
