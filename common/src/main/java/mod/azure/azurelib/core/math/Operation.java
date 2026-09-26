/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.math;

import java.util.HashSet;
import java.util.Set;

/**
 * Operation enumeration This enumeration provides different hardcoded enumerations of default math operators such
 * addition, substraction, multiplication, division, modulo and power. TODO: maybe convert to classes (for the sake of
 * API)?
 */
public enum Operation {

    ADD("+", 5) {

        @Override
        public double calculate(double a, double b) {
            return a + b;
        }
    },
    SUB("-", 5) {

        @Override
        public double calculate(double a, double b) {
            return a - b;
        }
    },
    MUL("*", 6) {

        @Override
        public double calculate(double a, double b) {
            return a * b;
        }
    },
    DIV("/", 6) {

        @Override
        public double calculate(double a, double b) {
            /* To avoid any exceptions */
            return a / (b == 0 ? 1 : b);
        }
    },
    MOD("%", 6) {

        @Override
        public double calculate(double a, double b) {
            return a % b;
        }
    },
    POW("^", 7) {

        @Override
        public double calculate(double a, double b) {
            return Math.pow(a, b);
        }
    },
    AND("&&", 2) {

        @Override
        public double calculate(double a, double b) {
            return a != 0 && b != 0 ? 1 : 0;
        }
    },
    OR("||", 1) {

        @Override
        public double calculate(double a, double b) {
            return a != 0 || b != 0 ? 1 : 0;
        }
    },
    LESS("<", 4) {

        @Override
        public double calculate(double a, double b) {
            return a < b ? 1 : 0;
        }
    },
    LESS_THAN("<=", 4) {

        @Override
        public double calculate(double a, double b) {
            return a <= b ? 1 : 0;
        }
    },
    GREATER_THAN(">=", 4) {

        @Override
        public double calculate(double a, double b) {
            return a >= b ? 1 : 0;
        }
    },
    GREATER(">", 4) {

        @Override
        public double calculate(double a, double b) {
            return a > b ? 1 : 0;
        }
    },
    EQUALS("==", 3) {

        @Override
        public double calculate(double a, double b) {
            return equals(a, b) ? 1 : 0;
        }
    },
    NOT_EQUALS("!=", 3) {

        @Override
        public double calculate(double a, double b) {
            return !equals(a, b) ? 1 : 0;
        }
    };

    public final static Set<String> OPERATORS = new HashSet<>();

    static {
        for (Operation op : values()) {
            OPERATORS.add(op.sign);
        }
    }

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

    public static boolean equals(double a, double b) {
        return Math.abs(a - b) < 0.00001;
    }

    /**
     * Calculate the value based on given two doubles
     */
    public abstract double calculate(double a, double b);
}
