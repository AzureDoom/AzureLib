/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.math;

import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * The binary operators supported in math and Molang expressions: arithmetic ({@code + - * / % ^}), logical
 * ({@code && ||}) and comparison ({@code < <= > >= == !=}).
 */
public enum Operation {

    ADD("+", 5),
    SUB("-", 5),
    MUL("*", 6),
    DIV("/", 6),
    MOD("%", 6),
    POW("^", 7),
    AND("&&", 2),
    OR("||", 1),
    LESS("<", 4),
    LESS_THAN("<=", 4),
    GREATER_THAN(">=", 4),
    GREATER(">", 4),
    EQUALS("==", 3),
    NOT_EQUALS("!=", 3);

    private static final Map<String, Operation> BY_SIGN = new HashMap<>();

    static {
        for (Operation op : values()) {
            BY_SIGN.put(op.sign, op);
        }
    }

    /**
     * Every operator sign. Unmodifiable: adding a sign here would not make it parseable (see the class docs).
     */
    public static final Set<String> OPERATORS = ImmutableSet.copyOf(BY_SIGN.keySet());

    /**
     * String-ified name of this operation
     */
    public final String sign;

    /**
     * Precedence of this operation; higher binds tighter. Follows Molang/C ordering, lowest first: {@code ||},
     * {@code &&}, {@code == !=}, {@code < <= > >=}, {@code + -}, {@code * / %}, {@code ^}.
     */
    public final int value;

    Operation(String sign, int value) {
        this.sign = sign;
        this.value = value;
    }

    /**
     * @return the operation for {@code sign}, or {@code null} if it isn't an operator
     */
    public static @Nullable Operation fromSign(String sign) {
        return BY_SIGN.get(sign);
    }

    public static boolean equals(double a, double b) {
        return Math.abs(a - b) < 0.00001;
    }

    /**
     * Calculate the value based on given two doubles. The only implementation of each operator: {@link Operator}
     * evaluates through here and constant folding uses it too, so the two can't drift apart.
     */
    public double calculate(double a, double b) {
        switch ((this)) {
            case ADD:
                return a + b;
            case SUB:
                return a - b;
            case MUL:
                return a * b;
            case DIV:
                return a / (b == 0 ? 1 : b);
            case MOD:
                return a % b;
            case POW:
                return Math.pow(a, b);
            case AND:
                return a != 0 && b != 0 ? 1 : 0;
            case OR:
                return a != 0 || b != 0 ? 1 : 0;
            case LESS:
                return a < b ? 1 : 0;
            case LESS_THAN:
                return a <= b ? 1 : 0;
            case GREATER_THAN:
                return a >= b ? 1 : 0;
            case GREATER:
                return a > b ? 1 : 0;
            case EQUALS:
                return equals(a, b) ? 1 : 0;
            case NOT_EQUALS:
                return !equals(a, b) ? 1 : 0;
            default:
                throw new IllegalStateException();
        }
    }
}
