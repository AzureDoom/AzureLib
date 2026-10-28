package mod.azure.azurelib.render.item;

/**
 * Implemented on {@code ItemStackRenderState} by mixin. Carries the id of the living entity holding the item from the
 * extract phase, where {@code ItemModelResolver} knows the owner, to the submit phase, where AzureLib animates the item
 * but the owner is no longer passed along.
 * <p>
 * An entity id rather than the entity itself, so render states that renderers keep between frames never hold an entity
 * (and through it, a level) alive.
 * </p>
 */
public interface AzItemHolderCarrier {

    int NO_HOLDER = -1;

    void azurelib$setHolderId(int entityId);

    int azurelib$getHolderId();
}
