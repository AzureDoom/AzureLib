package mod.azure.azurelib.registry;

import net.minecraft.entity.Entity;

import java.util.Map;
import java.util.regex.Pattern;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.mixins.accessor.EntityRegistryAccessor;

/**
 * Registers entity classes with Beta's entity registry (MCP {@code EntityList.addMapping}), replacing StationAPI's
 * {@code EntityRegisterEvent}.
 * <p>
 * Call it on <em>both</em> sides, from your OSL {@code init} entrypoint, before any world loads:
 *
 * <pre>{@code
 * AzEntityRegistry.register(ExampleEntity.class, "yourmodid_example_entity", 120);
 * }</pre>
 * <ul>
 * <li>The <b>name</b> is what gets saved in the entity's NBT, so it must never change once worlds exist. Prefix it with
 * your mod id to avoid clashing with other mods. It is a legacy Beta entity name, not a namespaced id: OSL's Entities
 * API turns it into one, so it may only contain letters, digits, {@code _ . - /}. No {@code :} (use
 * {@code yourmodid_example_entity}, not {@code yourmodid:example_entity}).</li>
 * <li>The <b>numeric id</b> is what multiplayer mob spawn packets send. Beta writes it as a signed byte, so living
 * entities need an id from 1 to 127 or they can't spawn on multiplayer clients. Vanilla uses ids up to about 97, and
 * other mods pick from the same small range, so make it configurable if you can.</li>
 * <li>Beta's server only tracks (sends to clients) entities it knows about: players, vanilla projectiles and vehicles,
 * and anything implementing its mob/animal marker interfaces, which every vanilla mob base class does. Custom entities
 * that aren't mobs won't appear on multiplayer clients without your own tracker mixin.</li>
 * </ul>
 * Name and id clashes with vanilla or another mod throw instead of silently replacing the other entity.
 */
public final class AzEntityRegistry {

    /** Characters OSL accepts once it converts a legacy entity name to a namespaced identifier. */
    private static final Pattern LEGACY_NAME = Pattern.compile("[A-Za-z0-9_.\\-/]+");

    /** Largest id that survives Beta's signed-byte mob spawn packet. */
    public static final int MAX_NETWORK_SAFE_ID = 127;

    private AzEntityRegistry() {
        throw new UnsupportedOperationException();
    }

    public static synchronized void register(Class<? extends Entity> entityClass, String name, int id) {
        if (entityClass == null || name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Entity class and name are required");
        }
        if (!LEGACY_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException(
                "Entity name '" + name + "' may only contain letters, digits, '_', '.', '-' and '/' (OSL converts it "
                    + "to a namespaced id). Use something like 'yourmodid_entity_name' instead of 'yourmodid:entity_name'."
            );
        }
        if (id <= 0) {
            throw new IllegalArgumentException("Entity id must be positive, got " + id + " for " + name);
        }

        Map<?, ?> byName = EntityRegistryAccessor.azurelib$getNameToClass();
        Object existingByName = byName.get(name);
        if (existingByName != null && existingByName != entityClass) {
            throw new IllegalStateException(
                "Entity name '" + name + "' is already registered to " + ((Class<?>) existingByName).getName()
            );
        }
        Map<?, ?> byId = EntityRegistryAccessor.azurelib$getRawIdToClass();
        Object existingById = byId.get(id);
        if (existingById != null && existingById != entityClass) {
            throw new IllegalStateException(
                "Entity id " + id + " is already registered to " + ((Class<?>) existingById).getName()
            );
        }

        if (id > MAX_NETWORK_SAFE_ID) {
            AzureLib.LOGGER.warn(
                "Entity {} uses id {}; Beta's mob spawn packet only carries ids up to {}, so it won't spawn on "
                    + "multiplayer clients if it is a mob.",
                name,
                id,
                MAX_NETWORK_SAFE_ID
            );
        }

        EntityRegistryAccessor.azurelib$register(entityClass, name, id);
    }
}
