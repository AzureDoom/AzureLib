/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.molang;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.core.math.Constant;
import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.MathBuilder;
import mod.azure.azurelib.core.math.Variable;
import mod.azure.azurelib.core.math.functions.Function;
import mod.azure.azurelib.core.molang.compile.MolangCompiler;
import mod.azure.azurelib.core.molang.compile.MolangSyntaxException;
import mod.azure.azurelib.core.molang.expressions.MolangCompoundValue;
import mod.azure.azurelib.core.molang.expressions.MolangValue;
import mod.azure.azurelib.core.molang.expressions.MolangVariableHolder;
import mod.azure.azurelib.core.molang.functions.*;
import mod.azure.azurelib.core.molang.functions.query.*;
import mod.azure.azurelib.core.molang.runtime.MolangArrayReference;

/**
 * Utility class for parsing and utilising MoLang functions and expressions
 *
 * @see <a href="https://bedrock.dev/docs/1.19.0.0/1.19.30.23/Molang#Math%20Functions">Bedrock Dev - Molang</a>
 */
@SuppressWarnings("unused")
public class MolangParser extends MathBuilder {

    /**
     * Global variables by normalized name. Concurrent because animation files are parsed in parallel, and parsing looks
     * variables up (and creates missing ones).
     */
    public static final Map<String, LazyVariable> VARIABLES = new ConcurrentHashMap<>();

    /** Full-name prefix of per-animatable variables; {@code v.} is expanded to it. */
    public static final String VARIABLE_PREFIX = "variable.";

    /**
     * Names that code registers or drives ({@link #register}, {@link #alias}, {@link #setValue},
     * {@link #setMemoizedValue}), as opposed to names that were only created by being looked up. A {@code variable.*}
     * name in here is read from the global variable; any other {@code variable.*} lives in the animatable's
     * {@link MolangScope}.
     */
    private static final Set<String> DRIVEN = ConcurrentHashMap.newKeySet();

    /** Bumped whenever a name is added to {@link #DRIVEN}, so scoped variables re-check their global fallback. */
    private static volatile int drivenGeneration;

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
        registerQueryFunctions();
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
     * Parse a Molang expression: one or more statements, with assignments, variables, conditionals, blocks and loops
     * (see {@link MolangCompiler} for the full grammar). An expression that fails to compile is retried with the older,
     * more limited parser, so nothing that loaded before stops loading; a warning names the problem.
     */
    public static MolangValue parseExpression(String expression) {
        // Not lower-cased here: the compiler lower-cases names itself, and string literals must keep their case.
        String source = expression.trim();

        if (source.isEmpty()) {
            AzureLib.LOGGER.error("Molang expression cannot be empty! Defaulted to 0");
            return ZERO;
        }

        IValue root;

        try {
            root = MolangCompiler.compile(INSTANCE, source);
        } catch (MolangSyntaxException e) {
            if (!e.isRetryable()) {
                AzureLib.LOGGER.error("Invalid Molang expression '{}': {}. Defaulted to 0", expression, e.getMessage());
                return ZERO;
            }

            AzureLib.LOGGER.warn(
                "Couldn't compile Molang expression '{}': {}. Falling back to the legacy parser.",
                expression,
                e.getMessage()
            );
            return parseExpressionLegacy(expression);
        } catch (RuntimeException e) {
            AzureLib.LOGGER.warn(
                "Couldn't compile Molang expression '{}': {}. Falling back to the legacy parser.",
                expression,
                e.getMessage()
            );
            return parseExpressionLegacy(expression);
        }

        MolangCompoundValue result = new MolangCompoundValue(new MolangValue(root));
        result.compact();

        return result;
    }

    /**
     * The parser used before {@link MolangCompiler}: statements split on {@code ;}, an assignment only at the start of
     * a statement, no blocks or loops, and {@code variable.*} values shared by every animatable. Only used as a
     * fallback for expressions the compiler rejects.
     */
    public static MolangValue parseExpressionLegacy(String expression) {
        Map<String, LazyVariable> locals = new HashMap<>();
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
            : new HashMap<>();

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
                symbols.size() >= 3 && symbols.get(0) instanceof String && INSTANCE.isVariable(symbols.get(0))
                    && symbols.get(1).equals("=")
            ) {
                String key = normalizeName(((String) symbols.get(0)));
                symbols = symbols.subList(2, symbols.size());
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
        remap("copy_sign", "math.copy_sign");
        remap("sign", "math.sign");
        remap("inverse_lerp", "math.inverse_lerp");
        remap("min_angle", "math.min_angle");

        String[] easingCurves = {
            "quad",
            "cubic",
            "quart",
            "quint",
            "sine",
            "expo",
            "circ",
            "back",
            "elastic",
            "bounce" };

        for (String curve : easingCurves) {
            remap("ease_in_" + curve, "math.ease_in_" + curve);
            remap("ease_out_" + curve, "math.ease_out_" + curve);
            remap("ease_in_out_" + curve, "math.ease_in_out_" + curve);
        }

        // A constant, not a function, so "math.pi * 2" folds at parse time. This used to be remap("pi", "math.pi"),
        // but no "pi" function exists, so it registered a null function and math.pi read an unset variable (0).
        registerConstant("math.pi", Math.PI);
    }

    private void registerAdditionalVariables() {
        register(new LazyVariable(MolangQueries.ACTOR_COUNT, 0));
        register(new LazyVariable(MolangQueries.ANIM_TIME, 0));
        register(new LazyVariable(MolangQueries.BODY_X_ROTATION, 0));
        register(new LazyVariable(MolangQueries.BODY_Y_ROTATION, 0));
        register(new LazyVariable(MolangQueries.CARDINAL_FACING_2D, 0));
        register(new LazyVariable(MolangQueries.CLIENT_MAX_RENDER_DISTANCE, 0));
        register(new LazyVariable(MolangQueries.DAY, 0));
        register(new LazyVariable(MolangQueries.DEATH_TICKS, 0));
        register(new LazyVariable(MolangQueries.DISTANCE_FROM_CAMERA, 0));
        register(new LazyVariable(MolangQueries.EQUIPMENT_COUNT, 0));
        register(new LazyVariable(MolangQueries.FRAME_ALPHA, 0));
        register(new LazyVariable(MolangQueries.GROUND_SPEED, 0));
        register(new LazyVariable(MolangQueries.HAS_COLLISION, 0));
        register(new LazyVariable(MolangQueries.HAS_GRAVITY, 0));
        register(new LazyVariable(MolangQueries.HAS_HEAD_GEAR, 0));
        register(new LazyVariable(MolangQueries.HAS_RIDER, 0));
        register(new LazyVariable(MolangQueries.HAS_TARGET, 0));
        register(new LazyVariable(MolangQueries.HEAD_IS_IN_WATER, 0));
        register(new LazyVariable(MolangQueries.HEAD_PITCH, 0));
        register(new LazyVariable(MolangQueries.HEAD_YAW, 0));
        register(new LazyVariable(MolangQueries.HEALTH, 0));
        register(new LazyVariable(MolangQueries.HURT_TIME, 0));
        register(new LazyVariable(MolangQueries.IN_AIR, 0));
        register(new LazyVariable(MolangQueries.IS_ALIVE, 0));
        register(new LazyVariable(MolangQueries.IS_BABY, 0));
        register(new LazyVariable(MolangQueries.IS_BLOCKING, 0));
        register(new LazyVariable(MolangQueries.IS_FIRE_IMMUNE, 0));
        register(new LazyVariable(MolangQueries.IS_FIRST_PERSON, 0));
        register(new LazyVariable(MolangQueries.IS_GLIDING, 0));
        register(new LazyVariable(MolangQueries.IS_INVISIBLE, 0));
        register(new LazyVariable(MolangQueries.IS_IN_LAVA, 0));
        register(new LazyVariable(MolangQueries.IS_IN_WATER, 0));
        register(new LazyVariable(MolangQueries.IS_IN_WATER_OR_RAIN, 0));
        register(new LazyVariable(MolangQueries.IS_LEASHED, 0));
        register(new LazyVariable(MolangQueries.IS_LOCAL_PLAYER, 0));
        register(new LazyVariable(MolangQueries.IS_MOVING, 0));
        register(new LazyVariable(MolangQueries.IS_ON_FIRE, 0));
        register(new LazyVariable(MolangQueries.IS_ON_GROUND, 0));
        register(new LazyVariable(MolangQueries.IS_RIDING, 0));
        register(new LazyVariable(MolangQueries.IS_SILENT, 0));
        register(new LazyVariable(MolangQueries.IS_SITTING, 0));
        register(new LazyVariable(MolangQueries.IS_SLEEPING, 0));
        register(new LazyVariable(MolangQueries.IS_SNEAKING, 0));
        register(new LazyVariable(MolangQueries.IS_SPECTATOR, 0));
        register(new LazyVariable(MolangQueries.IS_SPRINTING, 0));
        register(new LazyVariable(MolangQueries.IS_SWIMMING, 0));
        register(new LazyVariable(MolangQueries.IS_TAMED, 0));
        register(new LazyVariable(MolangQueries.IS_USING_ITEM, 0));
        register(new LazyVariable(MolangQueries.ITEM_CURRENT_DURABILITY, 0));
        register(new LazyVariable(MolangQueries.ITEM_IN_USE_DURATION, 0));
        register(new LazyVariable(MolangQueries.ITEM_IS_ENCHANTED, 0));
        register(new LazyVariable(MolangQueries.ITEM_MAX_USE_DURATION, 0));
        register(new LazyVariable(MolangQueries.ITEM_REMAINING_USE_DURATION, 0));
        register(new LazyVariable(MolangQueries.LIFE_TIME, 0));
        register(new LazyVariable(MolangQueries.LIMB_SWING, 0));
        register(new LazyVariable(MolangQueries.LIMB_SWING_AMOUNT, 0));
        register(new LazyVariable(MolangQueries.MAX_DURABILITY, 0));
        register(new LazyVariable(MolangQueries.MAX_HEALTH, 0));
        register(new LazyVariable(MolangQueries.MODEL_SCALE, 0));
        register(new LazyVariable(MolangQueries.MOON_BRIGHTNESS, 0));
        register(new LazyVariable(MolangQueries.MOON_PHASE, 0));
        register(new LazyVariable(MolangQueries.PLAYER_LEVEL, 0));
        register(new LazyVariable(MolangQueries.REMAINING_DURABILITY, 0));
        register(new LazyVariable(MolangQueries.SWIM_AMOUNT, 0));
        register(new LazyVariable(MolangQueries.TARGET_X_ROTATION, 0));
        register(new LazyVariable(MolangQueries.TARGET_Y_ROTATION, 0));
        register(new LazyVariable(MolangQueries.TIME_OF_DAY, 0));
        register(new LazyVariable(MolangQueries.TIME_STAMP, 0));
        register(new LazyVariable(MolangQueries.VERTICAL_SPEED, 0));
        register(new LazyVariable(MolangQueries.YAW_SPEED, 0));

        alias(MolangQueries.BLOCKING, MolangQueries.IS_BLOCKING);
        alias(MolangQueries.HEAD_X_ROTATION, MolangQueries.HEAD_PITCH);
        alias(MolangQueries.HEAD_Y_ROTATION, MolangQueries.HEAD_YAW);
        alias(MolangQueries.IS_ONFIRE, MolangQueries.IS_ON_FIRE);
    }

    private void registerQueryFunctions() {
        registerFunction(MolangQueries.ALL, All.class);
        registerFunction(MolangQueries.ANY, Any.class);
        registerFunction(MolangQueries.APPROX_EQ, ApproxEq.class);
        registerFunction(MolangQueries.IN_RANGE, InRange.class);

        registerFunction(MolangQueries.ABOVE_TOP_SOLID, AboveTopSolid.class);
        registerFunction(MolangQueries.ARMOR_DAMAGE_SLOT, ArmorDamageSlot.class);
        registerFunction(MolangQueries.CAMERA_DISTANCE_RANGE_LERP, CameraDistanceRangeLerp.class);
        registerFunction(MolangQueries.COUNT, Count.class);
        registerFunction(MolangQueries.HAS_ARMOR_SLOT, HasArmorSlot.class);
        registerFunction(MolangQueries.HEIGHTMAP, Heightmap.class);
        registerFunction(MolangQueries.IS_ITEM_EQUIPPED, IsItemEquipped.class);
        registerFunction(MolangQueries.MOVEMENT_DIRECTION, MovementDirection.class);
        registerFunction(MolangQueries.POSITION, Position.class);
        registerFunction(MolangQueries.POSITION_DELTA, PositionDelta.class);
    }

    /**
     * Registers a Molang function, such as a query that takes arguments. The name is normalized, so
     * {@code "q.my_query"} and {@code "query.my_query"} register the same function, and animations can call it with
     * either prefix.
     * <p>
     * Functions that read game state should extend {@link ContextQueryFunction} or otherwise live outside the
     * {@code mod.azure.azurelib.core.molang.functions} package, so they are not folded to a constant at load time.
     * Register during mod initialization, before resources load.
     * </p>
     */
    public void registerFunction(String name, Class<? extends Function> function) {
        this.functions.put(normalizeName(name), function);
    }

    /**
     * Registers a read-only {@code array.*} name for Molang to read and index ({@code array.skins[v.variant]}). The
     * supplier is called on the render thread each time the array is read, so it may depend on the animatable being
     * animated. Unregistered {@code array.*} names read as an empty array.
     * <p>
     * For arrays that belong to one animatable, a {@code variable.*} holding a {@link MolangArray} is often simpler:
     * {@code context.molangScope().setObject("v.skins", MolangArray.of(...))}.
     * </p>
     *
     * @param name {@code "array.skins"} or just {@code "skins"}
     */
    public void registerArray(String name, Supplier<MolangArray> supplier) {
        String normalized = name.toLowerCase(Locale.ROOT);
        MolangArrayReference.register(normalized.startsWith("array.") ? normalized : "array." + normalized, supplier);
    }

    /**
     * Makes {@code alias} read and write the same variable as {@code target}. Call after {@code target} is registered;
     * re-registering {@code target} later does not update the alias.
     */
    public void alias(String alias, String target) {
        String name = normalizeName(alias);
        VARIABLES.put(name, getVariable(target));
        markDriven(name);
        registrationGeneration++;
    }

    /**
     * Normalizes the function name so query functions can be called with the short {@code q.} prefix.
     */
    @Override
    protected IValue createFunction(String first, List<Object> args) throws Exception {
        return super.createFunction(normalizeName(first), args);
    }

    /**
     * Register a new {@link Variable} with the {@code MolangParser}.<br>
     * Ideally should be called from the mod constructor.
     */
    @Override
    public void register(Variable variable) {
        if (!(variable instanceof LazyVariable))
            variable = LazyVariable.from(variable);

        String name = normalizeName(variable.getName());
        VARIABLES.put(name, (LazyVariable) variable);
        markDriven(name);
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

        if (variable != null) {
            variable.set(value);
            markDriven(variable.getName());
        }
    }

    /**
     * Sets a memoized value supplier for a variable.<br>
     * This prevents re-calculation on successive calls, improving efficiency.<br>
     * This should be used wherever per-call accuracy is not needed.
     */
    public void setMemoizedValue(String name, DoubleSupplier value) {
        LazyVariable variable = getVariable(name);
        variable.setMemoized(value);
        markDriven(variable.getName());
    }

    /**
     * Whether code registers or drives this variable globally, rather than it only existing because something looked it
     * up. See {@link MolangScope} for how that decides where {@code variable.*} values live.
     */
    public boolean isDriven(String name) {
        return DRIVEN.contains(normalizeName(name));
    }

    /**
     * @return the global variable code drives under this (normalized) name, or {@code null}
     */
    public static LazyVariable drivenVariableOrNull(String normalizedName) {
        return DRIVEN.contains(normalizedName) ? VARIABLES.get(normalizedName) : null;
    }

    /**
     * Changes whenever a variable becomes driven; see {@link #drivenVariableOrNull}.
     */
    public static int drivenGeneration() {
        return drivenGeneration;
    }

    /**
     * The {@link MolangScope} slot for a normalized {@code variable.*} name.
     */
    public static int scopeSlot(String normalizedName) {
        return MolangScope.slot(normalizedName);
    }

    private static void markDriven(String normalizedName) {
        if (DRIVEN.add(normalizedName)) {
            drivenGeneration++;
        }
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
    public static String normalizeName(String name) {
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
