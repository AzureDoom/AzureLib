/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.molang;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.core.math.Constant;
import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.MathBuilder;
import mod.azure.azurelib.core.math.Variable;
import mod.azure.azurelib.core.molang.expressions.MolangCompoundValue;
import mod.azure.azurelib.core.molang.expressions.MolangValue;
import mod.azure.azurelib.core.molang.expressions.MolangVariableHolder;
import mod.azure.azurelib.core.molang.functions.CosDegrees;
import mod.azure.azurelib.core.molang.functions.SinDegrees;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.DoubleSupplier;

/**
 * Utility class for parsing and utilising MoLang functions and expressions
 *
 * @see <a href="https://bedrock.dev/docs/1.19.0.0/1.19.30.23/Molang#Math%20Functions">Bedrock Dev - Molang</a>
 */
@SuppressWarnings("unused")
public class MolangParser extends MathBuilder {

    public static final Map<String, LazyVariable> VARIABLES = new Object2ObjectOpenHashMap<>();

    /**
     * Fallback returned for expressions that fail to parse. The failure is logged once at parse time; evaluating this
     * must stay silent, since it runs every frame for every keyframe that uses it.
     */
    public static final MolangVariableHolder ZERO = constantHolder(0);

    public static final MolangVariableHolder ONE = constantHolder(1);

    /**
     * Expression-scoped variables of the compound expression currently being parsed on this thread, so reads inside an
     * expression resolve to variables assigned earlier in it. Thread-local because animation files are parsed in
     * parallel. Only consulted at parse time.
     */
    private static final ThreadLocal<Map<String, LazyVariable>> PARSE_LOCALS = new ThreadLocal<>();

    /** Bumped by every {@link #register} call so {@link MolangVariableRef}s know to re-resolve. */
    private static volatile int registrationGeneration;

    public static final String RETURN = "return ";

    public static final MolangParser INSTANCE = new MolangParser();

    /**
     * Parsed expressions keyed by their source string, so identical keyframe values share one tree instead of being
     * reparsed. Concurrent because animation files are loaded in parallel on the background executor.
     * <p>
     * Sharing is safe because parsed trees are never mutated after parsing, and every tree is already shared by all
     * entities playing the animation. Cleared at the start of each animation reload.
     */
    private static final Map<String, MolangValue> EXPRESSION_CACHE = new ConcurrentHashMap<>();

    /**
     * One shared {@link MolangValue} per numeric keyframe value. Most keyframe values in animation files are plain JSON
     * numbers, which bypass {@link #EXPRESSION_CACHE}, so without this every occurrence of {@code 0} got its own
     * wrapper. Keyed bit-exactly by {@link Double}, like the {@link Constant} pool. Cleared with the expression cache.
     */
    private static final Map<Double, MolangValue> CONSTANT_VALUES = new ConcurrentHashMap<>();

    /** Same bound as the {@link Constant} pool, for the same reason. */
    private static final int MAX_CONSTANT_VALUES = 16_384;

    public MolangParser() {
        super();

        doCoreRemaps();
        registerAdditionalVariables();
    }

    public static MolangValue parseJson(JsonElement element) {
        if (!element.isJsonPrimitive())
            return ZERO;

        JsonPrimitive primitive = element.getAsJsonPrimitive();

        if (primitive.isNumber())
            return constantValue(primitive.getAsDouble());

        if (primitive.isString())
            return parseCached(primitive.getAsString());

        return ZERO;
    }

    /**
     * Parse a keyframe value string, reusing a previously parsed tree for an identical string.
     */
    public static MolangValue parseCached(String string) {
        MolangValue cached = EXPRESSION_CACHE.get(string);

        if (cached != null)
            return cached;

        MolangValue parsed = parseUncached(string);
        MolangValue raced = EXPRESSION_CACHE.putIfAbsent(string, parsed);

        return raced != null ? raced : parsed;
    }

    /**
     * Parse a keyframe value string without consulting or populating the expression cache: a numeric literal becomes a
     * constant, anything else goes through {@link #parseExpression(String)}.
     */
    public static MolangValue parseUncached(String string) {
        try {
            return constantValue(Double.parseDouble(string));
        } catch (NumberFormatException ex) {
            return parseExpression(string);
        }
    }

    /**
     * A shared constant {@link MolangValue} for {@code value}.
     */
    public static MolangValue constantValue(double value) {
        MolangValue shared = CONSTANT_VALUES.get(value);

        if (shared != null)
            return shared;

        if (CONSTANT_VALUES.size() >= MAX_CONSTANT_VALUES)
            return new MolangValue(Constant.of(value));

        return CONSTANT_VALUES.computeIfAbsent(value, key -> new MolangValue(Constant.of(key)));
    }

    /**
     * Drop every cached expression tree and pooled constant. Trees already held by baked animations are unaffected.
     */
    public static void clearExpressionCache() {
        EXPRESSION_CACHE.clear();
        CONSTANT_VALUES.clear();
        Constant.clearPool();
    }

    /**
     * @return The number of distinct expression strings currently cached
     */
    public static int expressionCacheSize() {
        return EXPRESSION_CACHE.size();
    }

    /**
     * Parse a molang expression
     */
    public static MolangValue parseExpression(String expression) {
        Map<String, LazyVariable> locals = new Object2ObjectOpenHashMap<>();
        Map<String, LazyVariable> outerLocals = PARSE_LOCALS.get();
        MolangCompoundValue result = null;

        PARSE_LOCALS.set(locals);

        try {
            for (String split : expression.toLowerCase(Locale.ROOT).trim().split(";")) {
                String trimmed = split.trim();

                if (!trimmed.isEmpty()) {
                    MolangValue statement = parseOneLine(trimmed, locals);

                    if (result == null) {
                        result = new MolangCompoundValue(statement);
                    } else {
                        result.values.add(statement);
                    }
                }
            }
        } finally {
            if (outerLocals == null) {
                PARSE_LOCALS.remove();
            } else {
                PARSE_LOCALS.set(outerLocals);
            }
        }

        if (result == null) {
            AzureLib.LOGGER.error("Molang expression cannot be null! Defaulted to 0");
            return ZERO;
        }

        result.locals.putAll(locals);
        result.compact();

        return result;
    }

    /**
     * Parse a single Molang statement
     *
     * @deprecated Kept for subclasses; {@link #parseExpression(String)} no longer uses it. Variables assigned here are
     *             only visible to later reads when called from within parseExpression.
     */
    @Deprecated
    protected static MolangValue parseOneLine(
        String expression,
        MolangCompoundValue currentStatement
    ) {
        Map<String, LazyVariable> locals = currentStatement != null
            ? currentStatement.locals
            : new Object2ObjectOpenHashMap<>();

        return parseOneLine(expression, locals);
    }

    /**
     * Parse a single Molang statement, putting new expression-scoped variables into {@code locals}.
     */
    private static MolangValue parseOneLine(String expression, Map<String, LazyVariable> locals) {
        if (expression.startsWith(RETURN)) {
            try {
                return new MolangValue(INSTANCE.parse(expression.substring(RETURN.length())), true);
            } catch (Exception e) {
                AzureLib.LOGGER.error("Couldn't parse return {} expression! Defaulted to 0", expression);
                return MolangParser.ZERO;
            }
        }

        try {
            List<Object> symbols = INSTANCE.breakdownChars(INSTANCE.breakdown(expression));

            if (
                symbols.size() >= 3 && symbols.get(0) instanceof String name && INSTANCE.isVariable(symbols.get(0))
                    && symbols.get(1).equals("=")
            ) {
                symbols = symbols.subList(2, symbols.size());
                String key = normalizeName(name);
                LazyVariable variable = locals.get(key);

                if (variable == null) {
                    if (isTemporary(key) || !VARIABLES.containsKey(key)) {
                        variable = new LazyVariable(key, 0);
                        locals.put(key, variable);
                    } else {
                        variable = INSTANCE.getVariable(key);
                    }
                }

                return new MolangVariableHolder(variable, INSTANCE.parseSymbolsMolang(symbols));
            }

            return new MolangValue(INSTANCE.parseSymbolsMolang(symbols));
        } catch (Exception e) {
            AzureLib.LOGGER.error("Couldn't parse {} expression! Defaulted to 0", expression);
            return MolangParser.ZERO;
        }
    }

    private void doCoreRemaps() {
        this.functions.put("cos", CosDegrees.class);
        this.functions.put("sin", SinDegrees.class);

        remap("abs", "math.abs");
        remap("acos", "math.acos");
        remap("asin", "math.asin");
        remap("atan", "math.atan");
        remap("atan2", "math.atan2");
        remap("ceil", "math.ceil");
        remap("clamp", "math.clamp");
        remap("cos", "math.cos");
        remap("die_roll", "math.die_roll");
        remap("die_roll_integer", "math.die_roll_integer");
        remap("exp", "math.exp");
        remap("floor", "math.floor");
        remap("hermite_blend", "math.hermite_blend");
        remap("lerp", "math.lerp");
        remap("lerprotate", "math.lerprotate");
        remap("ln", "math.ln");
        remap("max", "math.max");
        remap("min", "math.min");
        remap("mod", "math.mod");
        remap("pow", "math.pow");
        remap("random", "math.random");
        remap("random_integer", "math.random_integer");
        remap("round", "math.round");
        remap("sin", "math.sin");
        remap("sqrt", "math.sqrt");
        remap("trunc", "math.trunc");

        // A constant, not a function, so "math.pi * 2" folds at parse time. This used to be remap("pi", "math.pi"),
        // but no "pi" function exists, so it registered a null function and math.pi read an unset variable (0).
        registerConstant("math.pi", Math.PI);
    }

    private void registerAdditionalVariables() {
        register(new LazyVariable(MolangQueries.ANIM_TIME, 0));
        register(new LazyVariable(MolangQueries.LIFE_TIME, 0));
        register(new LazyVariable(MolangQueries.ACTOR_COUNT, 0));
        register(new LazyVariable(MolangQueries.HEALTH, 0));
        register(new LazyVariable(MolangQueries.MAX_HEALTH, 0));
        register(new LazyVariable(MolangQueries.DISTANCE_FROM_CAMERA, 0));
        register(new LazyVariable(MolangQueries.YAW_SPEED, 0));
        register(new LazyVariable(MolangQueries.IS_IN_WATER_OR_RAIN, 0));
        register(new LazyVariable(MolangQueries.IS_IN_WATER, 0));
        register(new LazyVariable(MolangQueries.IS_ON_GROUND, 0));
        register(new LazyVariable(MolangQueries.TIME_OF_DAY, 0));
        register(new LazyVariable(MolangQueries.IS_ON_FIRE, 0));
        register(new LazyVariable(MolangQueries.GROUND_SPEED, 0));
        register(new LazyVariable(MolangQueries.HEAD_YAW, 0));
        register(new LazyVariable(MolangQueries.HEAD_PITCH, 0));
        register(new LazyVariable(MolangQueries.HURT_TIME, 0));
        register(new LazyVariable(MolangQueries.IN_AIR, 0));
        register(new LazyVariable(MolangQueries.IS_BABY, 0));
        register(new LazyVariable(MolangQueries.IS_BLOCKING, 0));
        register(new LazyVariable(MolangQueries.IS_USING_ITEM, 0));
        register(new LazyVariable(MolangQueries.ITEM_CURRENT_DURABILITY, 0));
        register(new LazyVariable(MolangQueries.ITEM_IS_ENCHANTED, 0));
        register(new LazyVariable(MolangQueries.LIMB_SWING, 0));
        register(new LazyVariable(MolangQueries.LIMB_SWING_AMOUNT, 0));
    }

    /**
     * Register a new {@link Variable} with the {@code MolangParser}.<br>
     * Ideally should be called from the mod constructor.
     */
    @Override
    public void register(Variable variable) {
        if (!(variable instanceof LazyVariable))
            variable = LazyVariable.from(variable);

        VARIABLES.put(normalizeName(variable.getName()), (LazyVariable) variable);
        registrationGeneration++;
    }

    static int registrationGeneration() {
        return registrationGeneration;
    }

    /**
     * Remap a function to a new name, maintaining the actual functionality and removing the old registration entry
     */
    public void remap(String old, String newName) {
        this.functions.put(newName, this.functions.remove(old));
    }

    /**
     * Set the value supplier for a variable.<br>
     * Consider using {@link MolangParser#setMemoizedValue} instead of you don't need per-call dynamic results
     *
     * @param name  The name of the variable to set the value for
     * @param value The value supplier to set
     */
    public void setValue(String name, DoubleSupplier value) {
        LazyVariable variable = getVariable(name);

        if (variable != null)
            variable.set(value);
    }

    /**
     * Sets a memoized value supplier for a variable.<br>
     * This prevents re-calculation on successive calls, improving efficiency.<br>
     * This should be used wherever per-call accuracy is not needed.
     */
    public void setMemoizedValue(String name, DoubleSupplier value) {
        getVariable(name).setMemoized(value);
    }

    /**
     * Get the registered {@link LazyVariable} for the given name
     *
     * @param name The name of the variable to get
     * @return The registered {@code LazyVariable} instance, or a newly registered instance if one wasn't registered
     *         previously
     */
    @Override
    public LazyVariable getVariable(String name) {
        return VARIABLES.computeIfAbsent(normalizeName(name), key -> new LazyVariable(key, 0));
    }

    /**
     * Parse-time lookup: a variable assigned earlier in the expression being parsed wins over the global one.
     */
    @Override
    protected LazyVariable resolveVariable(String name) {
        Map<String, LazyVariable> locals = PARSE_LOCALS.get();

        if (locals != null) {
            LazyVariable local = locals.get(normalizeName(name));

            if (local != null)
                return local;
        }

        return getVariable(name);
    }

    /**
     * Expands Molang's short prefixes so both spellings of a name reach the same variable: {@code q.} = {@code query.},
     * {@code v.} = {@code variable.}, {@code t.} = {@code temp.}, {@code c.} = {@code context.}.
     */
    static String normalizeName(String name) {
        if (name.length() > 2 && name.charAt(1) == '.') {
            switch (name.charAt(0)) {
                case 'q':
                    return "query." + name.substring(2);
                case 'v':
                    return "variable." + name.substring(2);
                case 't':
                    return "temp." + name.substring(2);
                case 'c':
                    return "context." + name.substring(2);
                default:
                    break;
            }
        }

        return name;
    }

    private static boolean isTemporary(String normalizedName) {
        return normalizedName.startsWith("temp.");
    }

    private static MolangVariableHolder constantHolder(double value) {
        return new MolangVariableHolder(null, new Constant(value)) {

            @Override
            public double get() {
                return value;
            }

            @Override
            public String toString() {
                return String.valueOf(value);
            }
        };
    }

    public LazyVariable getVariable(String name, MolangCompoundValue currentStatement) {
        LazyVariable variable;

        name = normalizeName(name);

        if (currentStatement != null) {
            variable = currentStatement.locals.get(name);

            if (variable != null)
                return variable;
        }

        return getVariable(name);
    }

    /**
     * Parses a list of symbols in the Molang context and converts them into an {@link IValue} representation. This
     * method extends the functionality of {@code parseSymbols} to handle scenarios unique to Molang expressions. If an
     * error occurs during parsing, an error message is logged and a default value of {@code ZERO} is returned.
     *
     * @param symbols A list of objects representing the symbols to be parsed into an {@link IValue}.
     * @return The parsed {@link IValue} object corresponding to the provided symbols. Returns {@code ZERO} in case of a
     *         parsing failure.
     */
    private IValue parseSymbolsMolang(List<Object> symbols) {
        try {
            return this.parseSymbols(symbols).simplify();
        } catch (Exception e) {
            AzureLib.LOGGER.error("Couldn't parse an expression! Defaulted to 0");
            return ZERO;
        }
    }

    /**
     * Extend this method to allow {@link #breakdownChars(String[])} to capture "=" as an operator, so it was easier to
     * parse assignment statements
     */
    @Override
    protected boolean isOperator(String s) {
        return super.isOperator(s) || s.equals("=");
    }
}
