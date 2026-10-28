package mod.azure.azurelib.core.molang.compile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Splits a Molang expression into {@link MolangToken}s. Names may contain dots ({@code query.anim_time},
 * {@code math.cos}, {@code v.x}); numbers may have a fraction, a leading dot and an exponent.
 * <p>
 * Molang is case-insensitive except for strings, so names are lower-cased here and string contents are kept exactly as
 * written. Strings are delimited by single quotes as in Bedrock ({@code 'minecraft:zombie'}, {@code ''} is empty);
 * double quotes are accepted too. There are no escape sequences.
 * </p>
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
        var tokens = new ArrayList<MolangToken>();
        var length = source.length();
        var i = 0;

        while (i < length) {
            var c = source.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            var start = i;

            if (isDigit(c) || (c == '.' && i + 1 < length && isDigit(source.charAt(i + 1)))) {
                i = scanNumber(source, i);
                var text = source.substring(start, i);
                var numberText = text.endsWith("f") || text.endsWith("F")
                    ? text.substring(0, text.length() - 1)
                    : text;

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
                    var next = source.charAt(i);

                    if (isNamePart(next)) {
                        i++;
                    } else if (next == '.' && i + 1 < length && isNameStart(source.charAt(i + 1))) {
                        i += 2;
                    } else {
                        break;
                    }
                }

                var name = source.substring(start, i).toLowerCase(Locale.ROOT);
                tokens.add(new MolangToken(MolangToken.Type.IDENTIFIER, name, 0, start));
                continue;
            }

            if (c == '\'' || c == '"') {
                var end = source.indexOf(c, i + 1);

                if (end < 0) {
                    throw new MolangSyntaxException("Unterminated string", start);
                }

                tokens.add(new MolangToken(MolangToken.Type.STRING, source.substring(i + 1, end), 0, start));
                i = end + 1;
                continue;
            }

            var operator = matchOperator(source, i);

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
        var length = source.length();

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
            var exponent = i + 1;

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
        var suffix = i < length ? source.charAt(i) : 0;

        if ((suffix == 'f' || suffix == 'F') && (i + 1 >= length || !isNamePart(source.charAt(i + 1)))) {
            i++;
        }

        return i;
    }

    private static String matchOperator(String source, int i) {
        for (var operator : OPERATORS) {
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
