package mod.azure.azurelib.core.molang.compile;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import mod.azure.azurelib.core.math.*;
import mod.azure.azurelib.core.math.functions.Function;
import mod.azure.azurelib.core.molang.MolangParser;
import mod.azure.azurelib.core.molang.expressions.MolangLocalVariable;
import mod.azure.azurelib.core.molang.expressions.MolangObjectVariable;
import mod.azure.azurelib.core.molang.expressions.MolangScopedVariable;
import mod.azure.azurelib.core.molang.runtime.*;

/**
 * Compiles a Molang expression into an {@link IValue} tree. A recursive-descent (precedence climbing) parser over
 * {@link MolangLexer} tokens, supporting:
 * <ul>
 * <li>Arithmetic {@code + - * / % ^}, comparison {@code < <= > >= == !=}, short-circuiting {@code && ||}, unary
 * {@code - ! +}, parentheses, numbers and {@code true}/{@code false}.</li>
 * <li>Conditionals: {@code a ? b : c}, and the binary form {@code a ? b} (0 when {@code a} is false). Branches may be
 * blocks: {@code q.is_on_ground ? { v.air_time = 0; } : { v.air_time = v.air_time + 1; }}.</li>
 * <li>Null coalescing {@code v.x ?? 1}: the right side when the variable on the left has no value yet.</li>
 * <li>Assignment {@code name = value} anywhere an expression is allowed; it evaluates to the assigned value.</li>
 * <li>Multiple statements separated by {@code ;}, {@code {}} blocks, {@code return}, {@code loop(count, body)},
 * {@code for_each(variable, array, body)}, {@code break} and {@code continue}.</li>
 * <li>Strings in single quotes, {@code 'minecraft:zombie'}, which keep their case and support only {@code ==} and
 * {@code !=}. Using a string literal with any other operator is a compile error.</li>
 * <li>Arrays: {@code array.*} names, query results such as {@code q.get_nearby_entities(8)}, and indexing with
 * {@code [index]} (Bedrock rules: floored, negative reads 0, past-the-end wraps). Arrays are read-only references.</li>
 * <li>The arrow operator {@code entity->expression}, which evaluates the right side against another entity.</li>
 * <li>{@code this}: the current value of what the expression computes, supplied by the evaluation context (in an
 * animation keyframe, the bone channel's current value).</li>
 * <li>Variables: {@code variable.*}/{@code v.*} are stored per animatable (see
 * {@link mod.azure.azurelib.core.molang.MolangScope}); {@code temp.*}/{@code t.*} live for one evaluation of the
 * expression; both can hold numbers, strings, arrays or entity references. {@code query.*}/{@code q.*},
 * {@code context.*}/{@code c.*}, {@code math.*} and {@code array.*} are read-only.</li>
 * <li>Every function and constant registered on {@link MolangParser}.</li>
 * </ul>
 * Names are case-insensitive; string contents are not.
 * <p>
 * Each compiled expression gets its own control-flow frame and expression-scoped variables, so a tree can be shared by
 * every animatable that plays the animation, but must only be evaluated by one thread at a time.
 */
public final class MolangCompiler {

    private static final int PREC_LOWEST = 0;

    private static final int PREC_ASSIGN = 1;

    private static final int PREC_TERNARY = 2;

    private static final int PREC_COALESCE = 3;

    private static final String TEMP_PREFIX = "temp.";

    private static final String ARRAY_PREFIX = "array.";

    private final MolangParser parser;

    private final List<MolangToken> tokens;

    private final MolangFrame frame = new MolangFrame();

    private final Map<String, MolangLocalVariable> locals = new LinkedHashMap<>();

    private int index;

    private boolean usesFrame;

    private MolangCompiler(MolangParser parser, List<MolangToken> tokens) {
        this.parser = parser;
        this.tokens = tokens;
    }

    /**
     * Compiles {@code source}. Names are lower-cased by the lexer; string literals keep their case.
     *
     * @return the simplified expression tree
     * @throws MolangSyntaxException if the expression is malformed or uses something unsupported
     */
    public static IValue compile(MolangParser parser, String source) {
        MolangCompiler compiler = new MolangCompiler(parser, MolangLexer.tokenize(source));
        return compiler.compileProgram();
    }

    private IValue compileProgram() {
        List<IValue> statements = parseStatements(false);
        expectEnd();

        if (statements.isEmpty()) {
            throw new MolangSyntaxException("Empty expression", 0);
        }

        IValue root;

        if (statements.size() == 1 && locals.isEmpty() && !usesFrame) {
            root = statements.getFirst();
        } else {
            root = new MolangProgram(statements, new ArrayList<>(locals.values()), frame);
        }

        return root.simplify();
    }

    private List<IValue> parseStatements(boolean inBlock) {
        ArrayList<IValue> statements = new ArrayList<>();

        while (true) {
            while (match(";")) {
                // Empty statements are allowed.
            }

            if (atStatementListEnd(inBlock)) {
                return statements;
            }

            statements.add(parseExpression(PREC_LOWEST));

            if (match(";")) {
                continue;
            }

            if (atStatementListEnd(inBlock)) {
                return statements;
            }

            throw error("Expected ';' between statements but found " + peek());
        }
    }

    private boolean atStatementListEnd(boolean inBlock) {
        MolangToken token = peek();
        return token.type() == MolangToken.Type.END || (inBlock && token.is("}"));
    }

    private IValue parseExpression(int minPrecedence) {
        IValue left = parseUnary();

        while (true) {
            MolangToken token = peek();

            if (token.type() != MolangToken.Type.OPERATOR) {
                return left;
            }

            String operator = token.text();
            int precedence = infixPrecedence(operator);

            if (precedence < 0 || precedence < minPrecedence) {
                return left;
            }

            advance();

            left = switch (operator) {
                case "?" -> {
                    IValue ifTrue = parseExpression(PREC_ASSIGN);
                    IValue ifFalse = match(":") ? parseExpression(PREC_ASSIGN) : Constant.of(0);
                    yield ifTrue instanceof MolangObjectValue || ifFalse instanceof MolangObjectValue
                        ? new MolangConditional(left, ifTrue, ifFalse)
                        : new Ternary(left, ifTrue, ifFalse);
                }
                case "??" -> new MolangCoalesce(left, parseExpression(PREC_COALESCE));
                case "&&" -> MolangLogical.and(left, parseExpression(precedence + 1));
                case "||" -> MolangLogical.or(left, parseExpression(precedence + 1));
                case "=" -> throw left instanceof MolangIndex
                    ? rejected("Array elements are read-only", token)
                    : error("Only variables can be assigned to", token);
                case "->" -> new MolangArrow(left, parseExpression(precedence + 1));
                default -> parseBinary(Operation.fromSign(operator), left, parseExpression(precedence + 1), token);
            };
        }
    }

    private static IValue parseBinary(Operation operation, IValue left, IValue right, MolangToken token) {
        boolean objectOperand = left instanceof MolangObjectValue || right instanceof MolangObjectValue;

        if (objectOperand && (operation == Operation.EQUALS || operation == Operation.NOT_EQUALS)) {
            return new MolangEquality(left, right, operation == Operation.NOT_EQUALS);
        }

        if (left instanceof MolangString || right instanceof MolangString) {
            throw rejected("Strings only support '==' and '!=', not '" + operation.sign + "'", token);
        }

        return new Operator(operation, left, right);
    }

    private static int infixPrecedence(String operator) {
        return switch (operator) {
            case "=" -> PREC_ASSIGN;
            case "?" -> PREC_TERNARY;
            case "??" -> PREC_COALESCE;
            case "||", "&&", "==", "!=", "<", "<=", ">", ">=", "+", "-", "*", "/", "%", "^" -> 1 + Operation
                .fromSign(operator).value + PREC_COALESCE;
            case "->" -> 100;
            default -> -1;
        };
    }

    private IValue parseUnary() {
        if (peek().is("-")) {
            MolangToken token = advance();
            IValue operand = parseUnary();

            if (operand instanceof MolangString) {
                throw rejected("A string can't be negated", token);
            }

            return new Negative(operand);
        }

        if (match("!")) {
            return new Negate(parseUnary());
        }

        if (match("+")) {
            return parseUnary();
        }

        return parsePrimary();
    }

    private IValue parsePrimary() {
        MolangToken token = advance();

        IValue primary = switch (token.type()) {
            case NUMBER -> Constant.of(token.number());
            case IDENTIFIER -> parseName(token);
            case STRING -> new MolangString(token.text());
            case END -> throw error("Expression ended early", token);
            case OPERATOR -> switch (token.text()) {
                case "(" -> {
                    IValue inner = parseExpression(PREC_LOWEST);
                    expect(")");
                    // Keep object-producing nodes visible to the operators around them.
                    yield inner instanceof MolangObjectValue ? inner : new Group(inner);
                }
                case "{" -> {
                    usesFrame = true;
                    List<IValue> statements = parseStatements(true);
                    expect("}");
                    yield new MolangBlock(statements, frame);
                }
                case "[" -> throw rejected(
                    "Molang has no array literals; arrays come from array.* names and queries",
                    token
                );
                default -> throw error("Unexpected " + token, token);
            };
        };

        return parsePostfix(primary);
    }

    /**
     * Indexing, {@code value[index]}, which may repeat: {@code array.grid[1][2]}.
     */
    private IValue parsePostfix(IValue primary) {
        while (peek().is("[")) {
            advance();
            IValue index = parseExpression(PREC_LOWEST);
            expect("]");
            primary = new MolangIndex(primary, index);
        }

        return primary;
    }

    private IValue parseName(MolangToken token) {
        String name = token.text();

        switch (name) {
            case "return" -> {
                usesFrame = true;
                IValue value = startsExpression(peek()) ? parseExpression(PREC_ASSIGN) : Constant.of(0);
                return MolangControlFlow.returning(value, frame);
            }
            case "break" -> {
                usesFrame = true;
                return MolangControlFlow.breaking(frame);
            }
            case "continue" -> {
                usesFrame = true;
                return MolangControlFlow.continuing(frame);
            }
            case "true" -> {
                return Constant.of(1);
            }
            case "false" -> {
                return Constant.of(0);
            }
            case "loop" -> {
                expect("(");
                IValue count = parseExpression(PREC_LOWEST);
                expect(",");
                IValue body = parseExpression(PREC_LOWEST);
                expect(")");
                usesFrame = true;
                return new MolangLoop(count, body, frame);
            }
            case "for_each" -> {
                return parseForEach(token);
            }
            case "this" -> {
                return MolangThis.INSTANCE;
            }
            default -> {}
        }

        if (match("(")) {
            return parseFunctionCall(token);
        }

        if (peek().is("=")) {
            advance();
            Variable target = resolveAssignmentTarget(token);
            return new MolangAssignment(target, parseExpression(PREC_ASSIGN));
        }

        return resolveRead(name);
    }

    /**
     * {@code for_each(variable, array, body)}. The variable must be a {@code temp.*} or {@code variable.*}, since the
     * elements are usually entity references.
     */
    private IValue parseForEach(MolangToken keyword) {
        expect("(");
        MolangToken variableToken = advance();

        if (variableToken.type() != MolangToken.Type.IDENTIFIER) {
            throw error(
                "for_each needs a variable to assign each element to, but found " + variableToken,
                variableToken
            );
        }

        Variable target = resolveAssignmentTarget(variableToken);

        if (!(target instanceof MolangObjectVariable variable)) {
            throw rejected(
                "for_each needs a temp.* or variable.* to iterate with, not '" + variableToken.text() + "'",
                variableToken
            );
        }

        expect(",");
        IValue array = parseExpression(PREC_LOWEST);
        expect(",");
        IValue body = parseExpression(PREC_LOWEST);
        expect(")");
        usesFrame = true;

        return new MolangForEach(variable, array, body, frame);
    }

    private IValue parseFunctionCall(MolangToken nameToken) {
        ArrayList<IValue> arguments = new ArrayList<>();

        if (!match(")")) {
            do {
                arguments.add(parseExpression(PREC_LOWEST));
            } while (match(","));

            expect(")");
        }

        String name = MolangParser.normalizeName(nameToken.text());
        Class<? extends Function> function = parser.functions.get(name);

        if (function == null) {
            throw error("Unknown function '" + nameToken.text() + "'", nameToken);
        }

        try {
            Constructor<? extends Function> constructor = function.getConstructor(IValue[].class, String.class);
            return constructor.newInstance(arguments.toArray(new IValue[0]), name);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw error("Can't call '" + name + "': " + cause.getMessage(), nameToken);
        } catch (ReflectiveOperationException e) {
            throw error("Function '" + name + "' can't be created: " + e, nameToken);
        }
    }

    private IValue resolveRead(String rawName) {
        String name = MolangParser.normalizeName(rawName);
        MolangLocalVariable local = locals.get(name);

        if (local != null) {
            return local;
        }

        Double constant = constant(name);

        if (constant != null) {
            return Constant.of(constant);
        }

        if (name.startsWith(TEMP_PREFIX)) {
            // Read before any assignment: 0, but still the same variable a later assignment writes.
            return local(name);
        }

        if (name.startsWith(ARRAY_PREFIX)) {
            return MolangArrayReference.of(name);
        }

        if (name.startsWith(MolangParser.VARIABLE_PREFIX)) {
            return parser.isDriven(name) ? parser.getVariable(name) : MolangScopedVariable.of(name);
        }

        return parser.getVariable(name);
    }

    private Variable resolveAssignmentTarget(MolangToken token) {
        String name = MolangParser.normalizeName(token.text());

        if (constant(name) != null) {
            throw new MolangSyntaxException(
                "'" + token.text() + "' is a constant and can't be assigned to",
                token.position(),
                false
            );
        }

        if (
            name.startsWith("query.") || name.startsWith("context.") || name.startsWith("math.")
                || name.startsWith(ARRAY_PREFIX)
        ) {
            throw new MolangSyntaxException("'" + token.text() + "' is read-only", token.position(), false);
        }

        MolangLocalVariable local = locals.get(name);

        if (local != null) {
            return local;
        }

        if (name.startsWith(TEMP_PREFIX)) {
            return local(name);
        }

        if (name.startsWith(MolangParser.VARIABLE_PREFIX)) {
            return parser.isDriven(name) ? parser.getVariable(name) : MolangScopedVariable.of(name);
        }

        // Unprefixed names: a variable code registered keeps working; anything else is local to this expression.
        return parser.isDriven(name) ? parser.getVariable(name) : local(name);
    }

    private MolangLocalVariable local(String name) {
        return locals.computeIfAbsent(name, MolangLocalVariable::new);
    }

    private Double constant(String name) {
        Double constant = parser.constants.get(name);

        if (constant == null && name.indexOf('.') < 0) {
            // MathBuilder registers PI and E in upper case, but expressions are lower-cased before parsing.
            constant = parser.constants.get(name.toUpperCase(Locale.ROOT));
        }

        return constant;
    }

    private static boolean startsExpression(MolangToken token) {
        if (token.type() == MolangToken.Type.END) {
            return false;
        }

        return !(token.is(";") || token.is("}") || token.is(")") || token.is(",") || token.is(":"));
    }

    private MolangToken peek() {
        return tokens.get(index);
    }

    private MolangToken advance() {
        MolangToken token = tokens.get(index);

        if (token.type() != MolangToken.Type.END) {
            index++;
        }

        return token;
    }

    private boolean match(String operator) {
        if (peek().is(operator)) {
            index++;
            return true;
        }

        return false;
    }

    private void expect(String operator) {
        if (!match(operator)) {
            throw error("Expected '" + operator + "' but found " + peek());
        }
    }

    private void expectEnd() {
        if (peek().type() != MolangToken.Type.END) {
            throw error("Unexpected " + peek());
        }
    }

    private MolangSyntaxException error(String message) {
        return error(message, peek());
    }

    private static MolangSyntaxException error(String message, MolangToken token) {
        return new MolangSyntaxException(message, token.position());
    }

    /**
     * An error for an expression that parses but is invalid Molang. Not retried with the legacy parser, which would
     * silently produce a different result.
     */
    private static MolangSyntaxException rejected(String message, MolangToken token) {
        return new MolangSyntaxException(message, token.position(), false);
    }
}
