/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.molang;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.DoubleSupplier;

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

/**
 * Utility class for parsing and utilising MoLang functions and expressions
 *
 * @see <a href="https://bedrock.dev/docs/1.19.0.0/1.19.30.23/Molang#Math%20Functions">Bedrock Dev - Molang</a>
 */
public class MolangParser extends MathBuilder {

    // Replace base variables map
    public static final Map<String, LazyVariable> VARIABLES = new Object2ObjectOpenHashMap<>();

    public static final MolangVariableHolder ZERO = new MolangVariableHolder(null, new Constant(0));

    public static final MolangVariableHolder ONE = new MolangVariableHolder(null, new Constant(1));

    public static final String RETURN = "return ";

    public static final MolangParser INSTANCE = new MolangParser();

    public MolangParser() {
        super();

        // Remap functions to be intact with Molang specification
        doCoreRemaps();
        registerAdditionalVariables();
    }

    public static MolangValue parseJson(JsonElement element) {
        if (!element.isJsonPrimitive())
            return ZERO;

        JsonPrimitive primitive = element.getAsJsonPrimitive();

        if (primitive.isNumber())
            return new MolangValue(new Constant(primitive.getAsDouble()));

        if (primitive.isString()) {
            String string = primitive.getAsString();

            try {
                return new MolangValue(new Constant(Double.parseDouble(string)));
            } catch (NumberFormatException ex) {
                return parseExpression(string);
            }
        }

        return ZERO;
    }

    /**
     * Parse a molang expression
     */
    public static MolangValue parseExpression(String expression) {
        MolangCompoundValue result = null;

        for (String split : expression.toLowerCase(Locale.ROOT).trim().split(";")) {
            String trimmed = split.trim();

            if (!trimmed.isEmpty()) {
                if (result == null) {
                    result = new MolangCompoundValue(parseOneLine(trimmed, result));

                    continue;
                }

                result.values.add(parseOneLine(trimmed, result));
            }
        }

        if (result == null) {
            AzureLib.LOGGER.error("Molang expression cannot be null! Defaulted to 0");
            return ZERO;
        }

        return result;
    }

    /**
     * Parse a single Molang statement
     */
    protected static MolangValue parseOneLine(
        String expression,
        MolangCompoundValue currentStatement
    ) {
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
                LazyVariable variable;

                if (!VARIABLES.containsKey(name) && !currentStatement.locals.containsKey(name)) {
                    currentStatement.locals.put(name, (variable = new LazyVariable(name, 0)));
                } else {
                    variable = INSTANCE.getVariable(name, currentStatement);
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
        // Replace radian based sin and cos with degree-based functions
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
        remap("pi", "math.pi");
        remap("pow", "math.pow");
        remap("random", "math.random");
        remap("random_integer", "math.random_integer");
        remap("round", "math.round");
        remap("sin", "math.sin");
        remap("sqrt", "math.sqrt");
        remap("trunc", "math.trunc");
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

        VARIABLES.put(variable.getName(), (LazyVariable) variable);
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
        var variable = getVariable(name);

        variable.set(new DoubleSupplier() {

            private boolean computed = false;

            private double cachedValue;

            @Override
            public double getAsDouble() {
                if (!computed) {
                    cachedValue = value.getAsDouble();
                    computed = true;
                }
                return cachedValue;
            }
        });
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
        return VARIABLES.computeIfAbsent(name, key -> new LazyVariable(key, 0));
    }

    public LazyVariable getVariable(String name, MolangCompoundValue currentStatement) {
        LazyVariable variable;

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
            return this.parseSymbols(symbols);
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
