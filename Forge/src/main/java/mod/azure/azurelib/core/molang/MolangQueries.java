package mod.azure.azurelib.core.molang;

/**
 * Holder class for the various builtin query string constants for the {@link MolangParser}.<br>
 * These do not constitute a definitive list of queries; merely the default ones
 */
public final class MolangQueries {

	private static final String QUERY_PREFIX = "query.";

	private static final String SHORT_PREFIX = "q.";

	public static final String ANIM_TIME = normalize("query.anim_time");

	public static final String LIFE_TIME = normalize("query.life_time");

	public static final String ACTOR_COUNT = normalize("query.actor_count");

	public static final String TIME_OF_DAY = normalize("query.time_of_day");

	public static final String MOON_PHASE = normalize("query.moon_phase");

	public static final String DISTANCE_FROM_CAMERA = normalize("query.distance_from_camera");

	public static final String IS_ON_GROUND = normalize("query.is_on_ground");

	public static final String IS_IN_WATER = normalize("query.is_in_water");

	public static final String IS_IN_WATER_OR_RAIN = normalize("query.is_in_water_or_rain");

	public static final String HEALTH = normalize("query.health");

	public static final String MAX_HEALTH = normalize("query.max_health");

	public static final String IS_ON_FIRE = normalize("query.is_on_fire");

	public static final String GROUND_SPEED = normalize("query.ground_speed");

	public static final String YAW_SPEED = normalize("query.yaw_speed");

	public static String normalize(String queryName) {
		if (queryName.startsWith(QUERY_PREFIX)) {
			return queryName;
		} else if (queryName.startsWith(SHORT_PREFIX)) {
			return QUERY_PREFIX + queryName.substring(SHORT_PREFIX.length());
		} else {
			throw new IllegalArgumentException("Invalid query name: " + queryName);
		}
	}
}
