package mod.azure.azurelib.core.molang;

/**
 * Supplies the game state behind Molang query <i>functions</i> (queries that take arguments, like
 * {@code query.position(0)}). Plain queries without arguments are bound as variables instead; see
 * {@link MolangQueries}.
 * <p>
 * The animator that is currently evaluating animations sets the active context with {@link #setCurrent} before
 * evaluating, so query functions read the animatable being animated. Every method defaults to {@code 0}, which is what
 * a query returns when it doesn't apply to the current animatable (e.g. armor queries on an item).
 * </p>
 */
public interface MolangQueryContext {

    MolangQueryContext EMPTY = new MolangQueryContext() {};

    /**
     * @return the context query functions should read from right now
     */
    static MolangQueryContext current() {
        return Holder.current;
    }

    /**
     * Sets the context query functions read from. {@code null} resets it to {@link #EMPTY}.
     */
    static void setCurrent(MolangQueryContext context) {
        Holder.current = context == null ? EMPTY : context;
    }

    /**
     * {@code query.position(axis)}: the entity's interpolated world position on the given axis (0 = x, 1 = y, 2 = z).
     */
    default double position(int axis) {
        return 0;
    }

    /** {@code query.position_delta(axis)}: the entity's movement this tick on the given axis. */
    default double positionDelta(int axis) {
        return 0;
    }

    /** {@code query.movement_direction(axis)}: the normalized movement direction on the given axis. */
    default double movementDirection(int axis) {
        return 0;
    }

    /** {@code query.has_armor_slot(slot)}: 1 if the armor slot (0 = head, 1 = chest, 2 = legs, 3 = feet) is filled. */
    default double hasArmorSlot(int slot) {
        return 0;
    }

    /** {@code query.armor_damage_slot(slot)}: the damage value of the item in the armor slot. */
    default double armorDamageSlot(int slot) {
        return 0;
    }

    /** {@code query.is_item_equipped(hand)}: 1 if the hand (0 = main hand, 1 = offhand) holds an item. */
    default double isItemEquipped(int hand) {
        return 0;
    }

    /** {@code query.heightmap(x, z)}: the height of the world surface at the given world position. */
    default double heightmap(double x, double z) {
        return 0;
    }

    /** {@code query.above_top_solid(x, z)}: the height just above the highest solid block at the given position. */
    default double aboveTopSolid(double x, double z) {
        return 0;
    }

    /** {@code query.distance_from_camera}, for query functions that need it. */
    default double distanceFromCamera() {
        return 0;
    }

    final class Holder {

        private static MolangQueryContext current = EMPTY;

        private Holder() {}
    }
}
