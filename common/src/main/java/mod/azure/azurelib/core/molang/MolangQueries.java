/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.molang;

/**
 * Holder class for the various builtin query string constants for the {@link MolangParser}.<br>
 * These do not constitute a definitive list of queries; merely the default ones
 */
public final class MolangQueries {

    private static final String QUERY_PREFIX = "query.";

    private static final String SHORT_PREFIX = "q.";

    public static final String ACTOR_COUNT = normalize("query.actor_count");

    public static final String ANIM_TIME = normalize("query.anim_time");

    public static final String BLOCKING = normalize("query.blocking");

    public static final String BODY_X_ROTATION = normalize("query.body_x_rotation");

    public static final String BODY_Y_ROTATION = normalize("query.body_y_rotation");

    public static final String CARDINAL_FACING_2D = normalize("query.cardinal_facing_2d");

    public static final String CLIENT_MAX_RENDER_DISTANCE = normalize("query.client_max_render_distance");

    public static final String DAY = normalize("query.day");

    public static final String DEATH_TICKS = normalize("query.death_ticks");

    public static final String DISTANCE_FROM_CAMERA = normalize("query.distance_from_camera");

    public static final String EQUIPMENT_COUNT = normalize("query.equipment_count");

    public static final String FRAME_ALPHA = normalize("query.frame_alpha");

    public static final String GROUND_SPEED = normalize("query.ground_speed");

    public static final String HAS_COLLISION = normalize("query.has_collision");

    public static final String HAS_GRAVITY = normalize("query.has_gravity");

    public static final String HAS_HEAD_GEAR = normalize("query.has_head_gear");

    public static final String HAS_RIDER = normalize("query.has_rider");

    public static final String HEAD_IS_IN_WATER = normalize("query.head_is_in_water");

    public static final String HEAD_PITCH = normalize("query.head_pitch");

    public static final String HEAD_X_ROTATION = normalize("query.head_x_rotation");

    public static final String HEAD_YAW = normalize("query.head_yaw");

    public static final String HEAD_Y_ROTATION = normalize("query.head_y_rotation");

    public static final String HEALTH = normalize("query.health");

    public static final String HURT_TIME = normalize("query.hurt_time");

    public static final String IN_AIR = normalize("query.in_air");

    public static final String IS_ALIVE = normalize("query.is_alive");

    public static final String IS_BABY = normalize("query.is_baby");

    public static final String IS_BLOCKING = normalize("query.is_blocking");

    public static final String IS_FIRE_IMMUNE = normalize("query.is_fire_immune");

    public static final String IS_FIRST_PERSON = normalize("query.is_first_person");

    public static final String IS_GLIDING = normalize("query.is_gliding");

    public static final String IS_INVISIBLE = normalize("query.is_invisible");

    public static final String IS_IN_LAVA = normalize("query.is_in_lava");

    public static final String IS_IN_WATER = normalize("query.is_in_water");

    public static final String IS_IN_WATER_OR_RAIN = normalize("query.is_in_water_or_rain");

    public static final String IS_LEASHED = normalize("query.is_leashed");

    public static final String IS_LOCAL_PLAYER = normalize("query.is_local_player");

    public static final String IS_MOVING = normalize("query.is_moving");

    public static final String IS_ONFIRE = normalize("query.is_onfire");

    public static final String IS_ON_FIRE = normalize("query.is_on_fire");

    public static final String IS_ON_GROUND = normalize("query.is_on_ground");

    public static final String IS_RIDING = normalize("query.is_riding");

    public static final String IS_SILENT = normalize("query.is_silent");

    public static final String IS_SITTING = normalize("query.is_sitting");

    public static final String IS_SLEEPING = normalize("query.is_sleeping");

    public static final String IS_SNEAKING = normalize("query.is_sneaking");

    public static final String IS_SPECTATOR = normalize("query.is_spectator");

    public static final String IS_SPRINTING = normalize("query.is_sprinting");

    public static final String IS_SWIMMING = normalize("query.is_swimming");

    public static final String IS_TAMED = normalize("query.is_tamed");

    public static final String IS_USING_ITEM = normalize("query.is_using_item");

    public static final String ITEM_CURRENT_DURABILITY = normalize("query.item_current_durability");

    public static final String ITEM_IN_USE_DURATION = normalize("query.item_in_use_duration");

    public static final String ITEM_IS_ENCHANTED = normalize("query.item_is_enchanted");

    public static final String ITEM_MAX_USE_DURATION = normalize("query.item_max_use_duration");

    public static final String ITEM_REMAINING_USE_DURATION = normalize("query.item_remaining_use_duration");

    public static final String LIFE_TIME = normalize("query.life_time");

    public static final String LIMB_SWING = normalize("query.limb_swing");

    public static final String LIMB_SWING_AMOUNT = normalize("query.limb_swing_amount");

    public static final String MAX_DURABILITY = normalize("query.max_durability");

    public static final String MAX_HEALTH = normalize("query.max_health");

    public static final String MODEL_SCALE = normalize("query.model_scale");

    public static final String MOON_BRIGHTNESS = normalize("query.moon_brightness");

    public static final String MOON_PHASE = normalize("query.moon_phase");

    public static final String PLAYER_LEVEL = normalize("query.player_level");

    public static final String REMAINING_DURABILITY = normalize("query.remaining_durability");

    public static final String SWIM_AMOUNT = normalize("query.swim_amount");

    public static final String TIME_OF_DAY = normalize("query.time_of_day");

    public static final String TIME_STAMP = normalize("query.time_stamp");

    public static final String VERTICAL_SPEED = normalize("query.vertical_speed");

    public static final String YAW_SPEED = normalize("query.yaw_speed");

    public static final String ABOVE_TOP_SOLID = normalize("query.above_top_solid");

    public static final String ALL = normalize("query.all");

    public static final String ANY = normalize("query.any");

    public static final String APPROX_EQ = normalize("query.approx_eq");

    public static final String ARMOR_DAMAGE_SLOT = normalize("query.armor_damage_slot");

    public static final String CAMERA_DISTANCE_RANGE_LERP = normalize("query.camera_distance_range_lerp");

    public static final String HAS_ARMOR_SLOT = normalize("query.has_armor_slot");

    public static final String HEIGHTMAP = normalize("query.heightmap");

    public static final String IN_RANGE = normalize("query.in_range");

    public static final String IS_ITEM_EQUIPPED = normalize("query.is_item_equipped");

    public static final String MOVEMENT_DIRECTION = normalize("query.movement_direction");

    public static final String POSITION = normalize("query.position");

    public static final String POSITION_DELTA = normalize("query.position_delta");

    public static String normalize(String queryName) {
        if (queryName.startsWith(QUERY_PREFIX)) {
            return queryName;
        } else if (queryName.startsWith(SHORT_PREFIX)) {
            return QUERY_PREFIX + queryName.substring(SHORT_PREFIX.length());
        } else {
            throw new IllegalArgumentException("Invalid query name: " + queryName);
        }
    }

    private MolangQueries() {
        throw new UnsupportedOperationException();
    }
}
