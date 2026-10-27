package mod.azure.azurelib.core.molang.compile;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits a Molang expression into {@link MolangToken}s. Names may contain dots ({@code query.anim_time},
 * {@code math.cos}, {@code v.x}); numbers may have a fraction, a leading dot and an exponent.
 */
final class MolangLexer {

    /** Longest first, so {@code ??} wins over {@code ?} and {@code ==} over {@code =}. */
    private static final String[] OPERATORS = {
        "??",
        "->",
        "&&",
        "||",
        "==",
        "!=",
        "<=",
        ">=",
        "+",
        "-",
        "*",
        "/",
        "%",
        "^",
        "!",
        "<",
        ">",
        "?",
        ":",
        "=",
        "(",
        ")",
        "{",
        "}",
        "[",
        "]",
        ",",
        ";" };

    private MolangLexer() {}

    static List<MolangToken> tokenize(String source) {
        ArrayList<MolangToken> tokens = new ArrayList<>();
        int length = source.length();
        int i = 0;

        while (i < length) {
            char c = source.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            int start = i;

            if (isDigit(c) || (c == '.' && i + 1 < length && isDigit(source.charAt(i + 1)))) {
                i = scanNumber(source, i);
                String text = source.substring(start, i);
                String numberText = text.endsWith("f") ? text.substring(0, text.length() - 1) : text;

                try {
                    tokens.add(new MolangToken(MolangToken.Type.NUMBER, text, Double.parseDouble(numberText), start));
                } catch (NumberFormatException e) {
                    throw new MolangSyntaxException("Malformed number '" + text + "'", start);
                }

                continue;
            }

            if (isNameStart(c)) {
                i++;

                while (i < length) {
                    char next = source.charAt(i);

                    if (isNamePart(next)) {
                        i++;
                    } else if (next == '.' && i + 1 < length && isNameStart(source.charAt(i + 1))) {
                        i += 2;
                    } else {
                        break;
                    }
                }

                tokens.add(new MolangToken(MolangToken.Type.IDENTIFIER, source.substring(start, i), 0, start));
                continue;
            }

            if (c == '\'' || c == '"') {
                int end = source.indexOf(c, i + 1);

                if (end < 0) {
                    throw new MolangSyntaxException("Unterminated string", start);
                }

                tokens.add(new MolangToken(MolangToken.Type.STRING, source.substring(i + 1, end), 0, start));
                i = end + 1;
                continue;
            }

            String operator = matchOperator(source, i);

            if (operator == null) {
                throw new MolangSyntaxException("Unexpected character '" + c + "'", start);
            }

            tokens.add(new MolangToken(MolangToken.Type.OPERATOR, operator, 0, start));
            i += operator.length();
        }

        tokens.add(new MolangToken(MolangToken.Type.END, "", 0, length));
        return tokens;
    }

    private static int scanNumber(String source, int i) {
        int length = source.length();

        while (i < length && isDigit(source.charAt(i))) {
            i++;
        }

        if (i < length && source.charAt(i) == '.') {
            i++;

            while (i < length && isDigit(source.charAt(i))) {
                i++;
            }
        }

        if (i < length && (source.charAt(i) == 'e' || source.charAt(i) == 'E')) {
            int exponent = i + 1;

            if (exponent < length && (source.charAt(exponent) == '+' || source.charAt(exponent) == '-')) {
                exponent++;
            }

            if (exponent < length && isDigit(source.charAt(exponent))) {
                i = exponent;

                while (i < length && isDigit(source.charAt(i))) {
                    i++;
                }
            }
        }

        // Bedrock content sometimes writes float literals C-style: 1.5f
        if (i < length && source.charAt(i) == 'f' && (i + 1 >= length || !isNamePart(source.charAt(i + 1)))) {
            i++;
        }

        return i;
    }

    private static String matchOperator(String source, int i) {
        for (String operator : OPERATORS) {
            if (source.startsWith(operator, i)) {
                return operator;
            }
        }

        return null;
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isNameStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private static boolean isNamePart(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
}
