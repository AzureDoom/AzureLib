package mod.azure.azurelib.animation.molang.functions;

import mod.azure.azurelib.core.molang.MolangParser;
import mod.azure.azurelib.core.molang.MolangQueries;

/**
 * Registers AzureLib's Minecraft-aware Molang query functions on {@link MolangParser#INSTANCE}. Called before animation
 * files are parsed; safe to call more than once.
 */
public final class AzMolangFunctions {

    private static boolean registered;

    private AzMolangFunctions() {
        throw new UnsupportedOperationException();
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }

        registered = true;

        MolangParser parser = MolangParser.INSTANCE;
        parser.registerFunction(MolangQueries.GET_EQUIPPED_ITEM_NAME, GetEquippedItemName.class);
        parser.registerFunction(MolangQueries.IS_ITEM_NAME_ANY, IsItemNameAny.class);
        parser.registerFunction(MolangQueries.EQUIPPED_ITEM_ANY_TAG, EquippedItemAnyTag.class);
        parser.registerFunction(MolangQueries.EQUIPPED_ITEM_ALL_TAGS, EquippedItemAllTags.class);
        parser.registerFunction(MolangQueries.IS_NAME_ANY, IsNameAny.class);
        parser.registerFunction(MolangQueries.GET_NEARBY_ENTITIES, GetNearbyEntities.class);
        parser.registerFunction(MolangQueries.GET_NEARBY_ENTITIES_EXCEPT_SELF, GetNearbyEntitiesExceptSelf.class);
    }
}
